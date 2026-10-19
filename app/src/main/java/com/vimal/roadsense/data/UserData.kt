package com.vimal.roadsense.data

/**
 * Detection thresholds for a specific vehicle type
 */
data class VehicleThresholds(
    val dropThreshold: Float,
    val spikeThreshold: Float,
    val minDelta: Float,
    val minSpeed: Float
)

/**
 * User-configurable settings stored locally
 */
data class UserSettings(
    val detectionSensitivity: Float = 1.0f,   // 1.0 = default, <1 more relaxed, >1 more aggressive
    val notificationsEnabled: Boolean = true,
    val autoSync: Boolean = true,
    val wifiOnlySync: Boolean = false
)

/**
 * Vehicle types with their specific detection thresholds
 * Different vehicles have different suspension and vibration patterns
 */
enum class VehicleType(
    val displayName: String,
    val emoji: String,
    val description: String,
    val thresholds: VehicleThresholds
) {
    BIKE(
        displayName = "Motorcycle/Scooter",
        emoji = "🏍️",
        description = "Two-wheeler with less suspension",
        thresholds = VehicleThresholds(
            dropThreshold = 6.0f,      // Bikes feel bumps more
            spikeThreshold = 13.0f,
            minDelta = 7.0f,
            minSpeed = 8f
        )
    ),
    
    BICYCLE(
        displayName = "Bicycle",
        emoji = "🚲",
        description = "Manual cycle, very sensitive",
        thresholds = VehicleThresholds(
            dropThreshold = 6.5f,      // Very sensitive to bumps
            spikeThreshold = 12.5f,
            minDelta = 6.0f,
            minSpeed = 5f
        )
    ),
    
    CAR(
        displayName = "Car/SUV",
        emoji = "🚗",
        description = "Four-wheeler with suspension",
        thresholds = VehicleThresholds(
            dropThreshold = 4.5f,      // Cars have good suspension
            spikeThreshold = 15.0f,
            minDelta = 10.0f,
            minSpeed = 12f
        )
    ),
    
    AUTO(
        displayName = "Auto Rickshaw",
        emoji = "🛺",
        description = "Three-wheeler, common in India",
        thresholds = VehicleThresholds(
            dropThreshold = 5.5f,      // Moderate suspension
            spikeThreshold = 14.0f,
            minDelta = 8.5f,
            minSpeed = 8f
        )
    ),
    
    BUS(
        displayName = "Bus/Truck",
        emoji = "🚌",
        description = "Heavy commercial vehicle",
        thresholds = VehicleThresholds(
            dropThreshold = 4.0f,      // Heavy vehicles feel less
            spikeThreshold = 16.0f,
            minDelta = 12.0f,
            minSpeed = 15f
        )
    );
    
    companion object {
        fun fromName(name: String): VehicleType {
            return try {
                valueOf(name)
            } catch (e: Exception) {
                BIKE
            }
        }
    }
}

/**
 * Apply a sensitivity factor to detection thresholds
 * Higher sensitivity lowers thresholds; lower sensitivity raises them
 */
fun VehicleThresholds.adjustForSensitivity(sensitivity: Float): VehicleThresholds {
    val factor = sensitivity.coerceIn(0.6f, 1.4f)
    val scale = 1f / factor
    return VehicleThresholds(
        dropThreshold = dropThreshold * scale,
        spikeThreshold = spikeThreshold * scale,
        minDelta = minDelta * scale,
        minSpeed = minSpeed
    )
}

/**
 * User profile data stored in Firestore
 */
data class UserProfile(
    val odId: String = "",
    val phoneNumber: String = "",
    val displayName: String = "",
    val vehicleType: String = VehicleType.BIKE.name,
    val totalPotholesReported: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val lastActiveAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any> = mapOf(
        "phoneNumber" to phoneNumber,
        "displayName" to displayName,
        "vehicleType" to vehicleType,
        "totalPotholesReported" to totalPotholesReported,
        "createdAt" to createdAt,
        "lastActiveAt" to lastActiveAt
    )
    
    val vehicle: VehicleType
        get() = try {
            VehicleType.valueOf(vehicleType)
        } catch (e: Exception) {
            VehicleType.BIKE
        }
}

/**
 * Authentication state
 */
sealed class AuthState {
    object Loading : AuthState()
    object NotAuthenticated : AuthState()
    object NeedsProfile : AuthState()
    data class Authenticated(val userId: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

/**
 * OTP verification state
 */
sealed class OtpState {
    object Idle : OtpState()
    object Sending : OtpState()
    object Sent : OtpState()
    object Verifying : OtpState()
    object Verified : OtpState()
    data class Error(val message: String) : OtpState()
}
