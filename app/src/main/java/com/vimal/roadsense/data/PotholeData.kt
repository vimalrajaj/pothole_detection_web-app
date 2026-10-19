package com.vimal.roadsense.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ServerTimestamp

/**
 * Data class representing a detected pothole
 */
data class PotholeData(
    @DocumentId
    val id: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val severity: Int = 1,  // 1 = Low, 2 = Medium, 3 = High
    val accelerationDelta: Float = 0f,
    val speed: Float = 0f,  // km/h at detection
    val userId: String = "",
    @ServerTimestamp
    val timestamp: Timestamp? = null
) {
    // Convert to Map for Firestore
    fun toMap(): Map<String, Any?> = mapOf(
        "latitude" to latitude,
        "longitude" to longitude,
        "severity" to severity,
        "accelerationDelta" to accelerationDelta,
        "speed" to speed,
        "userId" to userId,
        "timestamp" to timestamp
    )
    
    // Severity label
    val severityLabel: String
        get() = when (severity) {
            1 -> "Low"
            2 -> "Medium"
            3 -> "High"
            else -> "Unknown"
        }
}

/**
 * Detection state for UI
 */
data class DetectionState(
    val isDetecting: Boolean = false,
    val potholeCount: Int = 0,
    val currentSpeed: Float = 0f,
    val lastAcceleration: Float = 9.8f,
    val recentPotholes: List<PotholeData> = emptyList()
)

/**
 * Sensor reading for algorithm
 */
data class SensorReading(
    val x: Float,
    val y: Float,
    val z: Float,
    val timestamp: Long
)
