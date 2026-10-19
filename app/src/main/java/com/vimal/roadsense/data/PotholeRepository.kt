package com.vimal.roadsense.data

import android.os.Build
import android.util.Log
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.PersistentCacheSettings
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.Source
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Repository for Firestore operations with real-time sync
 */
class PotholeRepository {
    
    private val firestore = FirebaseFirestore.getInstance().apply {
        // Enable offline persistence for better real-time experience
        firestoreSettings = FirebaseFirestoreSettings.Builder()
            .setLocalCacheSettings(
                PersistentCacheSettings.newBuilder()
                    .setSizeBytes(FirebaseFirestoreSettings.CACHE_SIZE_UNLIMITED)
                    .build()
            )
            .build()
    }
    
    private val potholesCollection = firestore.collection("potholes")
    private val usersCollection = firestore.collection("users")
    
    // Unique device ID for this user
    val deviceId: String = "device_${Build.ID}_${Build.MODEL}".replace(" ", "_")
    
    // User ID (will be set after auth)
    private var _userId: String = deviceId
    val userId: String get() = _userId
    
    // Connection state
    private val _isOnline = MutableStateFlow(true)
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()
    
    // Pending uploads count (for offline mode)
    private val _pendingUploads = MutableStateFlow(0)
    val pendingUploads: StateFlow<Int> = _pendingUploads.asStateFlow()
    
    // Real-time listener registration
    private var realtimeListener: ListenerRegistration? = null
    
    companion object {
        private const val TAG = "PotholeRepository"
    }
    
    init {
        // Monitor connection state
        firestore.enableNetwork()
        monitorConnection()
    }
    
    /**
     * Set user ID after authentication
     */
    fun setUserId(uid: String) {
        _userId = uid
        Log.d(TAG, "User ID set to: $uid")
    }
    
    /**
     * Monitor Firestore connection state
     */
    private fun monitorConnection() {
        firestore.collection("_connection_test")
            .document("ping")
            .addSnapshotListener { _, error ->
                _isOnline.value = error == null
            }
    }
    
    /**
     * Save a pothole detection to Firestore (works offline too!)
     */
    suspend fun savePothole(pothole: PotholeData): Result<String> {
        return try {
            // Create document with auto-generated ID
            val docRef = potholesCollection.document()
            val potholeWithId = pothole.copy(id = docRef.id)
            
            // Save to Firestore (will sync when online)
            docRef.set(potholeWithId.toMap()).await()
            
            Log.d(TAG, "✅ Pothole saved: ${docRef.id} | Lat: ${pothole.latitude}, Lon: ${pothole.longitude}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error saving pothole", e)
            _pendingUploads.value++
            Result.failure(e)
        }
    }
    
    /**
     * Save pothole with immediate sync attempt
     */
    suspend fun savePotholeImmediate(pothole: PotholeData): Result<String> {
        return try {
            val docRef = potholesCollection.document()
            val potholeWithId = pothole.copy(id = docRef.id)
            
            // Force server write (will fail if offline)
            docRef.set(potholeWithId.toMap(), com.google.firebase.firestore.SetOptions.merge())
            
            // Wait for server confirmation
            firestore.waitForPendingWrites().await()
            
            Log.d(TAG, "✅ Pothole synced to server: ${docRef.id}")
            Result.success(docRef.id)
        } catch (e: Exception) {
            Log.e(TAG, "⚠️ Pothole saved locally, will sync later", e)
            savePothole(pothole)
        }
    }
    
    /**
     * Get user's potholes as Flow (REAL-TIME updates)
     */
    fun getUserPotholes(): Flow<List<PotholeData>> = callbackFlow {
        Log.d(TAG, "📡 Starting real-time listener for user: $userId")
        
        val listener = potholesCollection
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "❌ Real-time listener error", error)
                    return@addSnapshotListener
                }
                
                // Log changes for debugging
                snapshot?.documentChanges?.forEach { change ->
                    when (change.type) {
                        DocumentChange.Type.ADDED -> Log.d(TAG, "📥 New pothole: ${change.document.id}")
                        DocumentChange.Type.MODIFIED -> Log.d(TAG, "📝 Updated: ${change.document.id}")
                        DocumentChange.Type.REMOVED -> Log.d(TAG, "🗑️ Removed: ${change.document.id}")
                    }
                }
                
                val potholes = snapshot?.documents?.mapNotNull { doc ->
                    parsePotholeDocument(doc)
                } ?: emptyList()
                
                Log.d(TAG, "📊 Total potholes received: ${potholes.size}")
                trySend(potholes)
            }
        
        awaitClose { 
            Log.d(TAG, "🔌 Stopping real-time listener")
            listener.remove() 
        }
    }
    
    /**
     * Get ALL potholes (for map view - REAL-TIME)
     */
    fun getAllPotholesRealtime(limit: Long = 500): Flow<List<PotholeData>> = callbackFlow {
        Log.d(TAG, "🗺️ Starting global real-time listener (limit: $limit)")
        
        val listener = potholesCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(limit)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "❌ Global listener error", error)
                    return@addSnapshotListener
                }
                
                val potholes = snapshot?.documents?.mapNotNull { doc ->
                    parsePotholeDocument(doc)
                } ?: emptyList()
                
                Log.d(TAG, "🌍 Global potholes: ${potholes.size}")
                trySend(potholes)
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Get potholes near a location (for nearby alerts)
     */
    fun getPotholesNearby(
        latitude: Double,
        longitude: Double,
        radiusKm: Double = 1.0
    ): Flow<List<PotholeData>> = callbackFlow {
        // Approximate bounding box
        val latDelta = radiusKm / 111.0  // ~111km per degree latitude
        val lonDelta = radiusKm / (111.0 * Math.cos(Math.toRadians(latitude)))
        
        val listener = potholesCollection
            .whereGreaterThan("latitude", latitude - latDelta)
            .whereLessThan("latitude", latitude + latDelta)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e(TAG, "Nearby query error", error)
                    return@addSnapshotListener
                }
                
                // Filter by longitude in memory (Firestore limitation)
                val potholes = snapshot?.documents?.mapNotNull { doc ->
                    parsePotholeDocument(doc)
                }?.filter { pothole ->
                    pothole.longitude > longitude - lonDelta &&
                    pothole.longitude < longitude + lonDelta
                } ?: emptyList()
                
                trySend(potholes)
            }
        
        awaitClose { listener.remove() }
    }
    
    /**
     * Get total pothole count for user
     */
    suspend fun getUserPotholeCount(): Int {
        return try {
            val snapshot = potholesCollection
                .whereEqualTo("userId", userId)
                .get(Source.SERVER)  // Force server query for accurate count
                .await()
            snapshot.size()
        } catch (e: Exception) {
            // Fallback to cache if offline
            try {
                val cacheSnapshot = potholesCollection
                    .whereEqualTo("userId", userId)
                    .get(Source.CACHE)
                    .await()
                cacheSnapshot.size()
            } catch (e2: Exception) {
                Log.e(TAG, "Error getting pothole count", e2)
                0
            }
        }
    }
    
    /**
     * Get global statistics
     */
    suspend fun getGlobalStats(): GlobalStats {
        return try {
            val snapshot = potholesCollection.get(Source.SERVER).await()
            
            var totalPotholes = 0
            var highSeverity = 0
            var mediumSeverity = 0
            var lowSeverity = 0
            val uniqueUsers = mutableSetOf<String>()
            
            snapshot.documents.forEach { doc ->
                totalPotholes++
                when (doc.getLong("severity")?.toInt() ?: 1) {
                    3 -> highSeverity++
                    2 -> mediumSeverity++
                    else -> lowSeverity++
                }
                doc.getString("userId")?.let { uniqueUsers.add(it) }
            }
            
            GlobalStats(
                totalPotholes = totalPotholes,
                highSeverity = highSeverity,
                mediumSeverity = mediumSeverity,
                lowSeverity = lowSeverity,
                activeUsers = uniqueUsers.size
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting global stats", e)
            GlobalStats()
        }
    }
    
    /**
     * Update user stats after detection
     */
    suspend fun incrementUserPotholeCount() {
        try {
            usersCollection.document(userId)
                .update("totalPotholesReported", com.google.firebase.firestore.FieldValue.increment(1))
                .await()
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user stats", e)
        }
    }
    
    /**
     * Parse Firestore document to PotholeData
     */
    private fun parsePotholeDocument(doc: com.google.firebase.firestore.DocumentSnapshot): PotholeData? {
        return try {
            PotholeData(
                id = doc.id,
                latitude = doc.getDouble("latitude") ?: 0.0,
                longitude = doc.getDouble("longitude") ?: 0.0,
                severity = (doc.getLong("severity") ?: 1).toInt(),
                accelerationDelta = (doc.getDouble("accelerationDelta") ?: 0.0).toFloat(),
                speed = (doc.getDouble("speed") ?: 0.0).toFloat(),
                userId = doc.getString("userId") ?: "",
                timestamp = doc.getTimestamp("timestamp")
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing document ${doc.id}", e)
            null
        }
    }
    
    /**
     * Force sync pending writes
     */
    suspend fun syncPendingWrites() {
        try {
            firestore.waitForPendingWrites().await()
            _pendingUploads.value = 0
            Log.d(TAG, "✅ All pending writes synced")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing pending writes", e)
        }
    }
    
    /**
     * Clean up listeners
     */
    fun cleanup() {
        realtimeListener?.remove()
        realtimeListener = null
    }
}
