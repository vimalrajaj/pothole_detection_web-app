package com.vimal.roadsense.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

/**
 * Data class representing a trip/detection session
 */
data class TripData(
    @DocumentId
    val id: String = "",
    val userId: String = "",
    val startTime: Long = System.currentTimeMillis(),
    val endTime: Long = System.currentTimeMillis(),
    val duration: Long = 0, // in milliseconds
    val distance: Float = 0f, // in km
    val potholesFound: Int = 0,
    val startLatitude: Double = 0.0,
    val startLongitude: Double = 0.0,
    val endLatitude: Double = 0.0,
    val endLongitude: Double = 0.0,
    val averageSpeed: Float = 0f,
    val maxSpeed: Float = 0f,
    val vehicleType: String = VehicleType.BIKE.name,
    @ServerTimestamp
    val timestamp: Timestamp? = null
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "userId" to userId,
        "startTime" to startTime,
        "endTime" to endTime,
        "duration" to duration,
        "distance" to distance,
        "potholesFound" to potholesFound,
        "startLatitude" to startLatitude,
        "startLongitude" to startLongitude,
        "endLatitude" to endLatitude,
        "endLongitude" to endLongitude,
        "averageSpeed" to averageSpeed,
        "maxSpeed" to maxSpeed,
        "vehicleType" to vehicleType,
        "timestamp" to timestamp
    )
    
    val durationFormatted: String
        get() {
            val seconds = duration / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            return when {
                hours > 0 -> "${hours}h ${minutes % 60}m"
                minutes > 0 -> "${minutes}m ${seconds % 60}s"
                else -> "${seconds}s"
            }
        }
    
    val distanceFormatted: String
        get() = when {
            distance >= 1f -> String.format("%.1f km", distance)
            else -> String.format("%.0f m", distance * 1000)
        }
}

/**
 * Leaderboard entry for community features
 */
data class LeaderboardEntry(
    val rank: Int = 0,
    val userId: String = "",
    val displayName: String = "",
    val totalPotholes: Int = 0,
    val totalTrips: Int = 0,
    val totalDistance: Float = 0f,
    val city: String = "",
    val badges: List<String> = emptyList(),
    val avatarUrl: String? = null
)

/**
 * Global statistics for the app
 */
data class GlobalStats(
    val totalPotholes: Int = 0,
    val highSeverity: Int = 0,
    val mediumSeverity: Int = 0,
    val lowSeverity: Int = 0,
    val activeUsers: Int = 0,
    val totalUsers: Int = 0,
    val activeCities: Int = 0,
    val totalTrips: Int = 0,
    val totalDistance: Float = 0f
)

/**
 * Municipality report data
 */
data class MunicipalityReport(
    val potholeId: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val severity: Int = 1,
    val address: String = "",
    val description: String = "",
    val reportedBy: String = "",
    val reportedAt: Long = System.currentTimeMillis(),
    val status: ReportStatus = ReportStatus.PENDING
)

enum class ReportStatus {
    PENDING,
    SUBMITTED,
    ACKNOWLEDGED,
    IN_PROGRESS,
    RESOLVED
}

/**
 * App theme mode
 */
enum class ThemeMode {
    LIGHT,
    DARK,
    SYSTEM
}

/**
 * Supported languages
 */
enum class AppLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String
) {
    ENGLISH("en", "English", "English"),
    HINDI("hi", "Hindi", "हिन्दी"),
    TAMIL("ta", "Tamil", "தமிழ்"),
    TELUGU("te", "Telugu", "తెలుగు"),
    KANNADA("kn", "Kannada", "ಕನ್ನಡ"),
    MARATHI("mr", "Marathi", "मराठी"),
    BENGALI("bn", "Bengali", "বাংলা"),
    GUJARATI("gu", "Gujarati", "ગુજરાતી")
}

/**
 * Extended user settings with new features
 */
data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.DARK,
    val language: AppLanguage = AppLanguage.ENGLISH,
    val detectionSensitivity: Float = 1.0f,
    val notificationsEnabled: Boolean = true,
    val voiceAlertsEnabled: Boolean = true,
    val autoSync: Boolean = true,
    val wifiOnlySync: Boolean = false,
    val showHeatmap: Boolean = true,
    val trackTrips: Boolean = true
)
