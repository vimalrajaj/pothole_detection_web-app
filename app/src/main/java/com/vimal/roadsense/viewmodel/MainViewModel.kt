package com.vimal.roadsense.viewmodel

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vimal.roadsense.data.DetectionState
import com.vimal.roadsense.data.GlobalStats
import com.vimal.roadsense.data.LeaderboardEntry
import com.vimal.roadsense.data.PotholeData
import com.vimal.roadsense.data.PotholeRepository
import com.vimal.roadsense.data.TripData
import com.vimal.roadsense.data.VehicleThresholds
import com.vimal.roadsense.service.PotholeDetectionService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = PotholeRepository()
    
    private val _detectionState = MutableStateFlow(DetectionState())
    val detectionState: StateFlow<DetectionState> = _detectionState.asStateFlow()
    
    private val _userPotholes = MutableStateFlow<List<PotholeData>>(emptyList())
    val userPotholes: StateFlow<List<PotholeData>> = _userPotholes.asStateFlow()
    
    // Real-time sync status
    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()
    
    private val _pendingUploads = MutableStateFlow(0)
    val pendingUploads: StateFlow<Int> = _pendingUploads.asStateFlow()
    
    // Global stats
    private val _globalStats = MutableStateFlow(GlobalStats())
    val globalStats: StateFlow<GlobalStats> = _globalStats.asStateFlow()
    
    // All potholes for map
    private val _allPotholes = MutableStateFlow<List<PotholeData>>(emptyList())
    val allPotholes: StateFlow<List<PotholeData>> = _allPotholes.asStateFlow()
    
    // Trip history
    private val _trips = MutableStateFlow<List<TripData>>(emptyList())
    val trips: StateFlow<List<TripData>> = _trips.asStateFlow()
    
    // Leaderboard
    private val _leaderboard = MutableStateFlow<List<LeaderboardEntry>>(emptyList())
    val leaderboard: StateFlow<List<LeaderboardEntry>> = _leaderboard.asStateFlow()
    
    private var detectionService: PotholeDetectionService? = null
    private var isBound = false
    private var currentUserId: String? = null
    
    companion object {
        private const val TAG = "MainViewModel"
    }
    
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as PotholeDetectionService.LocalBinder
            detectionService = binder.getService()
            currentUserId?.let { uid -> detectionService?.setUserId(uid) }
            isBound = true
            
            // Observe service state
            viewModelScope.launch {
                detectionService?.let { svc ->
                    launch {
                        svc.detectionCount.collectLatest { count ->
                            _detectionState.value = _detectionState.value.copy(potholeCount = count)
                        }
                    }
                    launch {
                        svc.currentSpeed.collectLatest { speed ->
                            _detectionState.value = _detectionState.value.copy(currentSpeed = speed)
                        }
                    }
                    launch {
                        svc.lastAcceleration.collectLatest { accel ->
                            _detectionState.value = _detectionState.value.copy(lastAcceleration = accel)
                        }
                    }
                    launch {
                        svc.isRunning.collectLatest { running ->
                            _detectionState.value = _detectionState.value.copy(isDetecting = running)
                        }
                    }
                }
            }
        }
        
        override fun onServiceDisconnected(name: ComponentName?) {
            detectionService = null
            isBound = false
        }
    }
    
    init {
        Log.d(TAG, "🚀 MainViewModel initialized")
        
        // Monitor connection status
        viewModelScope.launch {
            repository.isOnline.collectLatest { online ->
                _isOnline.value = online
                Log.d(TAG, "📶 Connection status: ${if (online) "ONLINE" else "OFFLINE"}")
            }
        }
        
        // Monitor pending uploads
        viewModelScope.launch {
            repository.pendingUploads.collectLatest { pending ->
                _pendingUploads.value = pending
            }
        }
        
        // Load user's pothole history (REAL-TIME)
        viewModelScope.launch {
            Log.d(TAG, "📡 Starting real-time pothole listener")
            repository.getUserPotholes().collectLatest { potholes ->
                Log.d(TAG, "📊 Received ${potholes.size} potholes in real-time")
                _userPotholes.value = potholes
                _detectionState.value = _detectionState.value.copy(
                    recentPotholes = potholes.take(5),
                    potholeCount = potholes.size
                )
            }
        }
        
        // Load all potholes for map view (REAL-TIME)
        viewModelScope.launch {
            repository.getAllPotholesRealtime(500).collectLatest { potholes ->
                Log.d(TAG, "🗺️ Global potholes updated: ${potholes.size}")
                _allPotholes.value = potholes
            }
        }
        
        // Load global stats
        loadGlobalStats()
        
        // Load sample trips and leaderboard data
        loadSampleData()
    }
    
    /**
     * Load sample data for demo purposes
     */
    private fun loadSampleData() {
        // Sample trips
        _trips.value = listOf(
            TripData(
                id = "trip1",
                userId = currentUserId ?: "",
                startTime = System.currentTimeMillis() - 86400000, // Yesterday
                endTime = System.currentTimeMillis() - 86400000 + 1800000, // 30 min trip
                duration = 1800000,
                distance = 5.2f,
                potholesFound = 8,
                averageSpeed = 25f,
                maxSpeed = 45f,
                vehicleType = "BIKE",
                startLatitude = 12.9716,
                startLongitude = 77.5946
            ),
            TripData(
                id = "trip2",
                userId = currentUserId ?: "",
                startTime = System.currentTimeMillis() - 172800000, // 2 days ago
                endTime = System.currentTimeMillis() - 172800000 + 3600000, // 1 hour trip
                duration = 3600000,
                distance = 12.5f,
                potholesFound = 15,
                averageSpeed = 30f,
                maxSpeed = 50f,
                vehicleType = "BIKE",
                startLatitude = 12.9716,
                startLongitude = 77.5946
            ),
            TripData(
                id = "trip3",
                userId = currentUserId ?: "",
                startTime = System.currentTimeMillis() - 259200000, // 3 days ago
                endTime = System.currentTimeMillis() - 259200000 + 2400000, // 40 min
                duration = 2400000,
                distance = 8.0f,
                potholesFound = 12,
                averageSpeed = 28f,
                maxSpeed = 42f,
                vehicleType = "BIKE",
                startLatitude = 12.9716,
                startLongitude = 77.5946
            )
        )
        
        // Sample leaderboard
        _leaderboard.value = listOf(
            LeaderboardEntry(
                rank = 1,
                userId = "user_champion",
                displayName = "Road Guardian",
                totalPotholes = 247,
                totalTrips = 89,
                totalDistance = 520f,
                city = "Bangalore"
            ),
            LeaderboardEntry(
                rank = 2,
                userId = "user_pro",
                displayName = "Pothole Hunter",
                totalPotholes = 198,
                totalTrips = 65,
                totalDistance = 380f,
                city = "Chennai"
            ),
            LeaderboardEntry(
                rank = 3,
                userId = "user_expert",
                displayName = "City Fixer",
                totalPotholes = 156,
                totalTrips = 52,
                totalDistance = 290f,
                city = "Mumbai"
            ),
            LeaderboardEntry(
                rank = 4,
                userId = currentUserId ?: "user_you",
                displayName = "You",
                totalPotholes = _allPotholes.value.size,
                totalTrips = _trips.value.size,
                totalDistance = _trips.value.sumOf { it.distance.toDouble() }.toFloat(),
                city = "Your City"
            ),
            LeaderboardEntry(
                rank = 5,
                userId = "user_5",
                displayName = "Road Warrior",
                totalPotholes = 89,
                totalTrips = 34,
                totalDistance = 145f,
                city = "Delhi"
            ),
            LeaderboardEntry(
                rank = 6,
                userId = "user_6",
                displayName = "Street Scout",
                totalPotholes = 67,
                totalTrips = 28,
                totalDistance = 110f,
                city = "Hyderabad"
            ),
            LeaderboardEntry(
                rank = 7,
                userId = "user_7",
                displayName = "Path Finder",
                totalPotholes = 54,
                totalTrips = 22,
                totalDistance = 95f,
                city = "Pune"
            ),
            LeaderboardEntry(
                rank = 8,
                userId = "user_8",
                displayName = "Urban Explorer",
                totalPotholes = 42,
                totalTrips = 18,
                totalDistance = 78f,
                city = "Kolkata"
            )
        )
    }
    
    /**
     * Set user ID for repository (called after auth)
     */
    fun setUserId(uid: String) {
        currentUserId = uid
        repository.setUserId(uid)
        Log.d(TAG, "👤 User ID set: $uid")
    }
    
    /**
     * Load global statistics
     */
    fun loadGlobalStats() {
        viewModelScope.launch {
            val stats = repository.getGlobalStats()
            _globalStats.value = stats
            Log.d(TAG, "📈 Global stats: ${stats.totalPotholes} total, ${stats.activeUsers} users")
        }
    }
    
    /**
     * Force sync pending uploads
     */
    fun syncPendingUploads() {
        viewModelScope.launch {
            repository.syncPendingWrites()
        }
    }
    
    fun startDetection(vehicleThresholds: VehicleThresholds? = null) {
        val context = getApplication<Application>()
        val intent = Intent(context, PotholeDetectionService::class.java).apply {
            // Pass vehicle thresholds to service
            vehicleThresholds?.let { thresholds ->
                putExtra("threshold_drop", thresholds.dropThreshold)
                putExtra("threshold_spike", thresholds.spikeThreshold)
                putExtra("threshold_delta", thresholds.minDelta)
                putExtra("min_speed", thresholds.minSpeed)
            }
            currentUserId?.let { putExtra("user_id", it) }
        }
        
        Log.d(TAG, "▶️ Starting detection service")
        
        // Start as foreground service
        ContextCompat.startForegroundService(context, intent)
        
        // Bind to get updates
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        
        _detectionState.value = _detectionState.value.copy(isDetecting = true)
    }
    
    fun stopDetection() {
        val context = getApplication<Application>()
        
        Log.d(TAG, "⏹️ Stopping detection service")
        
        // Unbind first
        if (isBound) {
            context.unbindService(serviceConnection)
            isBound = false
        }
        
        // Stop service
        val intent = Intent(context, PotholeDetectionService::class.java)
        context.stopService(intent)
        
        detectionService = null
        _detectionState.value = _detectionState.value.copy(isDetecting = false)
        
        // Sync any pending uploads
        syncPendingUploads()
    }
    
    fun bindToService() {
        val context = getApplication<Application>()
        val intent = Intent(context, PotholeDetectionService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }
    
    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "🧹 ViewModel cleared")
        if (isBound) {
            getApplication<Application>().unbindService(serviceConnection)
        }
        repository.cleanup()
    }
}
