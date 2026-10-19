package com.vimal.roadsense.viewmodel

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vimal.roadsense.data.AppLanguage
import com.vimal.roadsense.data.AppSettings
import com.vimal.roadsense.data.AuthRepository
import com.vimal.roadsense.data.AuthState
import com.vimal.roadsense.data.ThemeMode
import com.vimal.roadsense.data.UserProfile
import com.vimal.roadsense.data.UserSettings
import com.vimal.roadsense.data.VehicleType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "roadsense_prefs")

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    
    private val authRepository = AuthRepository()
    private val dataStore = application.dataStore
    
    companion object {
        private const val TAG = "AuthViewModel"
        val KEY_VEHICLE_TYPE = stringPreferencesKey("vehicle_type")
        val KEY_PHONE_NUMBER = stringPreferencesKey("phone_number")
        val KEY_USER_NAME = stringPreferencesKey("user_name")
        val KEY_USER_ID = stringPreferencesKey("user_id")
        val KEY_SENSITIVITY = floatPreferencesKey("detection_sensitivity")
        val KEY_NOTIFICATIONS = booleanPreferencesKey("notifications_enabled")
        val KEY_AUTO_SYNC = booleanPreferencesKey("auto_sync")
        val KEY_WIFI_ONLY = booleanPreferencesKey("wifi_only_sync")
        // New settings keys
        val KEY_THEME_MODE = stringPreferencesKey("theme_mode")
        val KEY_LANGUAGE = stringPreferencesKey("language")
        val KEY_VOICE_ALERTS = booleanPreferencesKey("voice_alerts")
        val KEY_SHOW_HEATMAP = booleanPreferencesKey("show_heatmap")
        val KEY_TRACK_TRIPS = booleanPreferencesKey("track_trips")
    }
    
    // Auth state
    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState = _authState.asStateFlow()
    
    // User profile
    private val _userProfile = MutableStateFlow<UserProfile?>(null)
    val userProfile = _userProfile.asStateFlow()
    
    // Selected vehicle
    private val _selectedVehicle = MutableStateFlow(VehicleType.BIKE)
    val selectedVehicle = _selectedVehicle.asStateFlow()
    
    // Display name
    private val _displayName = MutableStateFlow("")
    val displayName = _displayName.asStateFlow()
    
    // Phone number
    private val _phoneNumber = MutableStateFlow("")
    val phoneNumber = _phoneNumber.asStateFlow()
    
    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()
    
    // Error message
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    // User settings (legacy)
    private val _userSettings = MutableStateFlow(UserSettings())
    val userSettings = _userSettings.asStateFlow()
    
    // App settings (new - includes theme, language, etc.)
    private val _appSettings = MutableStateFlow(AppSettings())
    val appSettings = _appSettings.asStateFlow()
    
    init {
        checkAuthStatus()
        loadSettings()
        loadAppSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            val prefs = dataStore.data.first()
            val settings = UserSettings(
                detectionSensitivity = prefs[KEY_SENSITIVITY] ?: 1.0f,
                notificationsEnabled = prefs[KEY_NOTIFICATIONS] ?: true,
                autoSync = prefs[KEY_AUTO_SYNC] ?: true,
                wifiOnlySync = prefs[KEY_WIFI_ONLY] ?: false
            )
            _userSettings.value = settings
        }
    }
    
    private fun loadAppSettings() {
        viewModelScope.launch {
            val prefs = dataStore.data.first()
            val settings = AppSettings(
                themeMode = prefs[KEY_THEME_MODE]?.let { 
                    try { ThemeMode.valueOf(it) } catch (e: Exception) { ThemeMode.DARK }
                } ?: ThemeMode.DARK,
                language = prefs[KEY_LANGUAGE]?.let {
                    try { AppLanguage.valueOf(it) } catch (e: Exception) { AppLanguage.ENGLISH }
                } ?: AppLanguage.ENGLISH,
                detectionSensitivity = prefs[KEY_SENSITIVITY] ?: 1.0f,
                notificationsEnabled = prefs[KEY_NOTIFICATIONS] ?: true,
                voiceAlertsEnabled = prefs[KEY_VOICE_ALERTS] ?: true,
                autoSync = prefs[KEY_AUTO_SYNC] ?: true,
                wifiOnlySync = prefs[KEY_WIFI_ONLY] ?: false,
                showHeatmap = prefs[KEY_SHOW_HEATMAP] ?: true,
                trackTrips = prefs[KEY_TRACK_TRIPS] ?: true
            )
            _appSettings.value = settings
        }
    }
    
    private fun checkAuthStatus() {
        viewModelScope.launch {
            try {
                // Check for saved local login
                val savedPhone = dataStore.data.map { prefs ->
                    prefs[KEY_PHONE_NUMBER]
                }.first()
                
                val savedName = dataStore.data.map { prefs ->
                    prefs[KEY_USER_NAME]
                }.first()
                
                val savedVehicle = dataStore.data.map { prefs ->
                    prefs[KEY_VEHICLE_TYPE]
                }.first()
                
                Log.d(TAG, "📱 Checking auth: phone=$savedPhone, name=$savedName, vehicle=$savedVehicle")
                
                if (!savedPhone.isNullOrEmpty() && !savedName.isNullOrEmpty()) {
                    val uid = "user_$savedPhone"
                    _phoneNumber.value = savedPhone
                    _displayName.value = savedName
                    
                    if (savedVehicle != null) {
                        _selectedVehicle.value = VehicleType.fromName(savedVehicle)
                    }
                    
                    // Check if profile exists in Firestore
                    val profile = authRepository.getUserProfile(uid)
                    if (profile != null) {
                        _userProfile.value = profile
                        _selectedVehicle.value = VehicleType.fromName(profile.vehicleType)
                        _displayName.value = profile.displayName
                    }
                    
                    Log.d(TAG, "✅ User authenticated: $uid")
                    _authState.value = AuthState.Authenticated(uid)
                } else {
                    Log.d(TAG, "🆕 New user - needs onboarding")
                    _authState.value = AuthState.NotAuthenticated
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error checking auth", e)
                _authState.value = AuthState.NotAuthenticated
            }
        }
    }
    
    /**
     * Complete onboarding and create profile
     */
    fun completeOnboarding(name: String, phone: String, vehicle: VehicleType) {
        Log.d(TAG, "📝 Completing onboarding: name=$name, phone=$phone, vehicle=$vehicle")
        
        _isLoading.value = true
        
        viewModelScope.launch {
            try {
                val uid = "user_$phone"
                
                val profile = UserProfile(
                    odId = uid,
                    phoneNumber = phone,
                    displayName = name,
                    vehicleType = vehicle.name,
                    totalPotholesReported = 0,
                    createdAt = System.currentTimeMillis(),
                    lastActiveAt = System.currentTimeMillis()
                )
                
                Log.d(TAG, "💾 Saving profile to Firestore...")
                val result = authRepository.saveUserProfile(profile)
                
                result.fold(
                    onSuccess = {
                        Log.d(TAG, "✅ Profile saved!")
                        _userProfile.value = profile
                        _displayName.value = name
                        _phoneNumber.value = phone
                        _selectedVehicle.value = vehicle
                        
                        // Save locally
                        dataStore.edit { prefs ->
                            prefs[KEY_USER_ID] = uid
                            prefs[KEY_PHONE_NUMBER] = phone
                            prefs[KEY_USER_NAME] = name
                            prefs[KEY_VEHICLE_TYPE] = vehicle.name
                            prefs[KEY_SENSITIVITY] = _userSettings.value.detectionSensitivity
                            prefs[KEY_NOTIFICATIONS] = _userSettings.value.notificationsEnabled
                            prefs[KEY_AUTO_SYNC] = _userSettings.value.autoSync
                            prefs[KEY_WIFI_ONLY] = _userSettings.value.wifiOnlySync
                        }
                        
                        _authState.value = AuthState.Authenticated(uid)
                    },
                    onFailure = { e ->
                        Log.e(TAG, "❌ Failed to save profile", e)
                        // Still authenticate locally even if Firestore fails
                        _userProfile.value = profile
                        _displayName.value = name
                        _phoneNumber.value = phone
                        _selectedVehicle.value = vehicle
                        
                        dataStore.edit { prefs ->
                            prefs[KEY_USER_ID] = uid
                            prefs[KEY_PHONE_NUMBER] = phone
                            prefs[KEY_USER_NAME] = name
                            prefs[KEY_VEHICLE_TYPE] = vehicle.name
                            prefs[KEY_SENSITIVITY] = _userSettings.value.detectionSensitivity
                            prefs[KEY_NOTIFICATIONS] = _userSettings.value.notificationsEnabled
                            prefs[KEY_AUTO_SYNC] = _userSettings.value.autoSync
                            prefs[KEY_WIFI_ONLY] = _userSettings.value.wifiOnlySync
                        }
                        
                        _authState.value = AuthState.Authenticated(uid)
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error during onboarding", e)
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Update vehicle type
     */
    fun updateVehicle(vehicle: VehicleType) {
        Log.d(TAG, "🚗 Updating vehicle to: $vehicle")
        _selectedVehicle.value = vehicle
        
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[KEY_VEHICLE_TYPE] = vehicle.name
            }
            
            // Update in Firestore
            _userProfile.value?.let { profile ->
                val updatedProfile = profile.copy(vehicleType = vehicle.name)
                authRepository.saveUserProfile(updatedProfile)
                _userProfile.value = updatedProfile
            }
        }
    }
    
    /**
     * Sign out
     */
    fun signOut() {
        Log.d(TAG, "👋 Signing out")
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs.remove(KEY_USER_ID)
                prefs.remove(KEY_PHONE_NUMBER)
                prefs.remove(KEY_USER_NAME)
            }
            
            _userProfile.value = null
            _displayName.value = ""
            _phoneNumber.value = ""
            _authState.value = AuthState.NotAuthenticated
        }
    }

    /**
     * Update user settings and persist
     */
    fun updateSettings(settings: UserSettings) {
        _userSettings.value = settings
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[KEY_SENSITIVITY] = settings.detectionSensitivity
                prefs[KEY_NOTIFICATIONS] = settings.notificationsEnabled
                prefs[KEY_AUTO_SYNC] = settings.autoSync
                prefs[KEY_WIFI_ONLY] = settings.wifiOnlySync
            }
        }
    }
    
    /**
     * Update app settings (theme, language, etc.)
     */
    fun updateAppSettings(settings: AppSettings) {
        _appSettings.value = settings
        // Also update legacy user settings for compatibility
        _userSettings.value = UserSettings(
            detectionSensitivity = settings.detectionSensitivity,
            notificationsEnabled = settings.notificationsEnabled,
            autoSync = settings.autoSync,
            wifiOnlySync = settings.wifiOnlySync
        )
        
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[KEY_THEME_MODE] = settings.themeMode.name
                prefs[KEY_LANGUAGE] = settings.language.name
                prefs[KEY_SENSITIVITY] = settings.detectionSensitivity
                prefs[KEY_NOTIFICATIONS] = settings.notificationsEnabled
                prefs[KEY_VOICE_ALERTS] = settings.voiceAlertsEnabled
                prefs[KEY_AUTO_SYNC] = settings.autoSync
                prefs[KEY_WIFI_ONLY] = settings.wifiOnlySync
                prefs[KEY_SHOW_HEATMAP] = settings.showHeatmap
                prefs[KEY_TRACK_TRIPS] = settings.trackTrips
            }
        }
    }
    
    /**
     * Get current user ID
     */
    fun getCurrentUserId(): String? {
        return _userProfile.value?.odId ?: _phoneNumber.value.takeIf { it.isNotEmpty() }?.let { "user_$it" }
    }
    
    /**
     * Get current vehicle thresholds
     */
    fun getCurrentThresholds() = _selectedVehicle.value.thresholds
    
    /**
     * Clear error
     */
    fun clearError() {
        _errorMessage.value = null
    }
}
