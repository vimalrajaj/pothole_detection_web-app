package com.vimal.roadsense.data

import android.app.Activity
import android.util.Log
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class AuthRepository {
    
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private val usersCollection = firestore.collection("users")
    
    private var verificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    
    companion object {
        private const val TAG = "AuthRepository"
    }
    
    val currentUser get() = auth.currentUser
    val isLoggedIn get() = auth.currentUser != null
    
    /**
     * Send OTP to phone number
     */
    fun sendOtp(
        phoneNumber: String,
        activity: Activity,
        onCodeSent: () -> Unit,
        onVerificationCompleted: (PhoneAuthCredential) -> Unit,
        onError: (String) -> Unit
    ) {
        val formattedNumber = if (phoneNumber.startsWith("+")) phoneNumber else "+91$phoneNumber"
        
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(TAG, "Auto verification completed")
                onVerificationCompleted(credential)
            }
            
            override fun onVerificationFailed(e: FirebaseException) {
                Log.e(TAG, "Verification failed", e)
                onError(e.message ?: "Verification failed")
            }
            
            override fun onCodeSent(
                verId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                Log.d(TAG, "OTP sent successfully")
                verificationId = verId
                resendToken = token
                onCodeSent()
            }
        }
        
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(formattedNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        
        PhoneAuthProvider.verifyPhoneNumber(options)
    }
    
    /**
     * Verify OTP code
     */
    suspend fun verifyOtp(code: String): Result<String> {
        return try {
            val verId = verificationId ?: return Result.failure(Exception("No verification ID"))
            val credential = PhoneAuthProvider.getCredential(verId, code)
            val result = auth.signInWithCredential(credential).await()
            val uid = result.user?.uid ?: return Result.failure(Exception("No user ID"))
            Log.d(TAG, "OTP verified, user: $uid")
            Result.success(uid)
        } catch (e: Exception) {
            Log.e(TAG, "OTP verification failed", e)
            Result.failure(e)
        }
    }
    
    /**
     * Sign in with credential (for auto-verification)
     */
    suspend fun signInWithCredential(credential: PhoneAuthCredential): Result<String> {
        return try {
            val result = auth.signInWithCredential(credential).await()
            val uid = result.user?.uid ?: return Result.failure(Exception("No user ID"))
            Result.success(uid)
        } catch (e: Exception) {
            Log.e(TAG, "Sign in failed", e)
            Result.failure(e)
        }
    }
    
    /**
     * Get user profile from Firestore
     */
    suspend fun getUserProfile(uid: String): UserProfile? {
        return try {
            val doc = usersCollection.document(uid).get().await()
            if (doc.exists()) {
                UserProfile(
                    odId = uid,
                    phoneNumber = doc.getString("phoneNumber") ?: "",
                    displayName = doc.getString("displayName") ?: "",
                    vehicleType = doc.getString("vehicleType") ?: VehicleType.BIKE.name,
                    totalPotholesReported = (doc.getLong("totalPotholesReported") ?: 0).toInt(),
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                    lastActiveAt = doc.getLong("lastActiveAt") ?: System.currentTimeMillis()
                )
            } else null
        } catch (e: Exception) {
            Log.e(TAG, "Error getting user profile", e)
            null
        }
    }
    
    /**
     * Create or update user profile
     */
    suspend fun saveUserProfile(profile: UserProfile): Result<Unit> {
        return try {
            // Use profile's odId directly (works without Firebase Auth)
            val uid = profile.odId
            Log.d(TAG, "Saving profile for user: $uid")
            usersCollection.document(uid).set(profile.toMap()).await()
            Log.d(TAG, "Profile saved successfully")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving user profile", e)
            Result.failure(e)
        }
    }
    
    // Store current user ID locally for operations without Firebase Auth
    private var localUserId: String? = null
    
    fun setLocalUserId(uid: String) {
        localUserId = uid
    }
    
    /**
     * Update vehicle type
     */
    suspend fun updateVehicleType(vehicleType: VehicleType): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: localUserId ?: return Result.failure(Exception("No user ID"))
            usersCollection.document(uid).update("vehicleType", vehicleType.name).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating vehicle", e)
            Result.failure(e)
        }
    }
    
    /**
     * Check if user has completed profile (has vehicle selected)
     */
    suspend fun hasCompletedProfile(): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        val profile = getUserProfile(uid)
        return profile != null && profile.vehicleType.isNotEmpty()
    }
    
    /**
     * Sign out
     */
    fun signOut() {
        auth.signOut()
    }
    
    /**
     * Observe auth state changes
     */
    fun observeAuthState(): Flow<AuthState> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            if (user == null) {
                trySend(AuthState.NotAuthenticated)
            } else {
                trySend(AuthState.Loading)
            }
        }
        
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }
}
