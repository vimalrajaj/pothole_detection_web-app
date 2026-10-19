package com.vimal.roadsense.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.vimal.roadsense.MainActivity
import com.vimal.roadsense.R
import com.vimal.roadsense.data.PotholeData
import com.vimal.roadsense.data.PotholeRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.LinkedList
import kotlin.math.abs

class PotholeDetectionService : Service(), SensorEventListener {
    
    companion object {
        private const val TAG = "PotholeDetection"
        private const val CHANNEL_ID = "pothole_detection_channel"
        private const val NOTIFICATION_ID = 1001
        
        // Detection thresholds (default, can be overridden by vehicle type)
        private const val WINDOW_SIZE = 10  // ~500ms at 20Hz
        private const val DEBOUNCE_MS = 3000L  // 3 seconds between detections
    }
    
    // Dynamic thresholds based on vehicle type
    private var dropThreshold = 5.0f
    private var spikeThreshold = 14.0f
    private var minDelta = 9.0f
    private var minSpeedKmh = 10f
    
    private val binder = LocalBinder()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val repository = PotholeRepository()
    
    private lateinit var sensorManager: SensorManager
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var accelerometer: Sensor? = null
    
    // Location updates for continuous speed monitoring
    private var locationCallback: LocationCallback? = null
    private var locationRequest: LocationRequest? = null
    
    // Detection state
    private val zWindow = LinkedList<Float>()
    private var lastDetectionTime = 0L
    private var lastLatitude = 0.0
    private var lastLongitude = 0.0
    
    // Observable state for UI
    private val _detectionCount = MutableStateFlow(0)
    val detectionCount: StateFlow<Int> = _detectionCount.asStateFlow()
    
    private val _currentSpeed = MutableStateFlow(0f)
    val currentSpeed: StateFlow<Float> = _currentSpeed.asStateFlow()
    
    private val _lastAcceleration = MutableStateFlow(9.8f)
    val lastAcceleration: StateFlow<Float> = _lastAcceleration.asStateFlow()
    
    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()
    
    inner class LocalBinder : Binder() {
        fun getService(): PotholeDetectionService = this@PotholeDetectionService
    }

    fun setUserId(uid: String) {
        repository.setUserId(uid)
    }
    
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "Service created")
        
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "Service started")
        
        // Load vehicle-specific thresholds from intent
        intent?.let {
            dropThreshold = it.getFloatExtra("threshold_drop", 5.0f)
            spikeThreshold = it.getFloatExtra("threshold_spike", 14.0f)
            minDelta = it.getFloatExtra("threshold_delta", 9.0f)
            minSpeedKmh = it.getFloatExtra("min_speed", 10f)
            val userId = it.getStringExtra("user_id")
            userId?.let { uid ->
                repository.setUserId(uid)
                Log.d(TAG, "User set for detection service: $uid")
            }
            Log.d(TAG, "Thresholds: drop=$dropThreshold, spike=$spikeThreshold, delta=$minDelta, minSpeed=$minSpeedKmh")
        }
        
        val notification = createNotification()
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION)
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
        
        // Start sensor listening
        accelerometer?.let {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_GAME  // ~50Hz for better detection
            )
        }
        
        _isRunning.value = true
        
        // Load initial count
        scope.launch {
            _detectionCount.value = repository.getUserPotholeCount()
        }
        
        // Start continuous speed monitoring
        startSpeedMonitoring()
        
        return START_STICKY
    }
    
    private fun startSpeedMonitoring() {
        try {
            locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
                .setMinUpdateIntervalMillis(500L)
                .setWaitForAccurateLocation(false)
                .build()
            
            locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.lastLocation?.let { location ->
                        // Update speed (convert m/s to km/h)
                        val speedKmh = if (location.hasSpeed()) {
                            location.speed * 3.6f
                        } else {
                            0f
                        }
                        _currentSpeed.value = speedKmh
                        
                        // Store location for pothole reporting
                        lastLatitude = location.latitude
                        lastLongitude = location.longitude
                        
                        Log.d(TAG, "📍 Speed: ${String.format("%.1f", speedKmh)} km/h, Location: ${location.latitude}, ${location.longitude}")
                    }
                }
            }
            
            fusedLocationClient.requestLocationUpdates(
                locationRequest!!,
                locationCallback!!,
                mainLooper
            )
            Log.d(TAG, "📡 Started continuous location updates")
        } catch (e: SecurityException) {
            Log.e(TAG, "Location permission denied", e)
        }
    }
    
    private fun stopSpeedMonitoring() {
        locationCallback?.let {
            fusedLocationClient.removeLocationUpdates(it)
            Log.d(TAG, "📡 Stopped location updates")
        }
        locationCallback = null
        locationRequest = null
    }
    
    override fun onBind(intent: Intent?): IBinder = binder
    
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type != Sensor.TYPE_ACCELEROMETER) return
        
        val z = event.values[2]  // Vertical axis
        _lastAcceleration.value = abs(z)  // Use absolute value for better display
        
        // Add to sliding window
        zWindow.add(z)
        if (zWindow.size > WINDOW_SIZE) zWindow.removeFirst()
        
        // Check for pothole pattern
        if (zWindow.size == WINDOW_SIZE) {
            analyzeWindow()
        }
    }
    
    private fun analyzeWindow() {
        val minZ = zWindow.minOrNull() ?: return
        val maxZ = zWindow.maxOrNull() ?: return
        val delta = maxZ - minZ
        
        // Pothole pattern: sudden drop then spike (using vehicle-specific thresholds)
        if (minZ < dropThreshold && maxZ > spikeThreshold && delta > minDelta) {
            val now = System.currentTimeMillis()
            
            // Debounce check
            if (now - lastDetectionTime > DEBOUNCE_MS) {
                lastDetectionTime = now
                Log.d(TAG, "Potential pothole detected! Delta: $delta")
                
                // Get location and validate speed before reporting
                getLocationAndReport(delta)
            }
        }
    }
    
    private fun getLocationAndReport(delta: Float) {
        // Use cached location if available (faster response)
        if (lastLatitude != 0.0 && lastLongitude != 0.0) {
            val speedKmh = _currentSpeed.value
            
            // Only report if moving fast enough
            if (speedKmh < minSpeedKmh) {
                Log.d(TAG, "Speed too low ($speedKmh km/h), ignoring detection")
                return
            }
            
            reportPothole(lastLatitude, lastLongitude, speedKmh, delta)
            return
        }
        
        // Fallback to fresh location request
        try {
            val cancellationToken = CancellationTokenSource()
            
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationToken.token
            ).addOnSuccessListener { location ->
                if (location == null) {
                    Log.w(TAG, "Location is null")
                    return@addOnSuccessListener
                }
                
                val speedKmh = location.speed * 3.6f  // Convert m/s to km/h
                _currentSpeed.value = speedKmh
                
                // Only report if moving fast enough (using vehicle-specific minimum speed)
                if (speedKmh < minSpeedKmh) {
                    Log.d(TAG, "Speed too low ($speedKmh km/h), ignoring detection")
                    return@addOnSuccessListener
                }
                
                reportPothole(location.latitude, location.longitude, speedKmh, delta)
            }.addOnFailureListener { e ->
                Log.e(TAG, "Failed to get location", e)
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "Location permission denied", e)
        }
    }
    
    private fun reportPothole(latitude: Double, longitude: Double, speedKmh: Float, delta: Float) {
        // Calculate severity based on delta
        val severity = when {
            delta > 15f -> 3  // High
            delta > 11f -> 2  // Medium
            else -> 1         // Low
        }
        
        val pothole = PotholeData(
            latitude = latitude,
            longitude = longitude,
            severity = severity,
            accelerationDelta = delta,
            speed = speedKmh,
            userId = repository.userId
        )
        
        // Save to Firestore
        scope.launch {
            val result = repository.savePothole(pothole)
            if (result.isSuccess) {
                _detectionCount.value++
                updateNotification()
                Log.d(TAG, "Pothole reported! Total: ${_detectionCount.value}")
            }
        }
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Pothole Detection",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows when pothole detection is active"
                setShowBadge(false)
            }
            
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }
    
    private fun createNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🛣️ Road-Sense Active")
            .setContentText("Detected ${_detectionCount.value} potholes")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setOngoing(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .build()
    }
    
    private fun updateNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, createNotification())
    }
    
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    
    override fun onDestroy() {
        Log.d(TAG, "Service destroyed")
        stopSpeedMonitoring()  // Stop GPS updates
        sensorManager.unregisterListener(this)
        _isRunning.value = false
        scope.cancel()
        super.onDestroy()
    }
}
