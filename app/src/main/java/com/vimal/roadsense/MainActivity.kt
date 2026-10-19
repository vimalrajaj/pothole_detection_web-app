package com.vimal.roadsense

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.vimal.roadsense.data.AuthState
import com.vimal.roadsense.data.LeaderboardEntry
import com.vimal.roadsense.data.TripData
import com.vimal.roadsense.data.adjustForSensitivity
import com.vimal.roadsense.ui.screens.HomeScreenNew
import com.vimal.roadsense.ui.screens.LeaderboardScreen
import com.vimal.roadsense.ui.screens.OnboardingScreen
import com.vimal.roadsense.ui.screens.ProfileScreen
import com.vimal.roadsense.ui.screens.SettingsScreen
import com.vimal.roadsense.ui.screens.TripHistoryScreen
import com.vimal.roadsense.ui.theme.DarkBackground
import com.vimal.roadsense.ui.theme.RoadSenseTheme
import com.vimal.roadsense.ui.theme.SurfaceLight
import com.vimal.roadsense.viewmodel.AuthViewModel
import com.vimal.roadsense.viewmodel.MainViewModel

// Navigation routes
sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Home : Screen("home")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object TripHistory : Screen("trip_history")
    object Leaderboard : Screen("leaderboard")
}

class MainActivity : ComponentActivity() {
    
    private lateinit var mainViewModel: MainViewModel
    private lateinit var authViewModel: AuthViewModel
    
    private val notificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* Handle result if needed */ }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        mainViewModel = ViewModelProvider(this)[MainViewModel::class.java]
        authViewModel = ViewModelProvider(this)[AuthViewModel::class.java]
        
        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        setContent {
            // App settings with theme
            val appSettings by authViewModel.appSettings.collectAsState()
            
            RoadSenseTheme(themeMode = appSettings.themeMode) {
                val navController = rememberNavController()
                
                // Auth state
                val authState by authViewModel.authState.collectAsState()
                val userProfile by authViewModel.userProfile.collectAsState()
                val selectedVehicle by authViewModel.selectedVehicle.collectAsState()
                val displayName by authViewModel.displayName.collectAsState()
                val phoneNumber by authViewModel.phoneNumber.collectAsState()
                
                // Main detection state
                val detectionState by mainViewModel.detectionState.collectAsState()
                val recentPotholes by mainViewModel.userPotholes.collectAsState()
                val allPotholes by mainViewModel.allPotholes.collectAsState()
                
                // Real-time sync status
                val isOnline by mainViewModel.isOnline.collectAsState()
                val pendingUploads by mainViewModel.pendingUploads.collectAsState()
                val userSettings by authViewModel.userSettings.collectAsState()
                
                // Trip history (sample data for now)
                val trips by mainViewModel.trips.collectAsState()
                
                // Leaderboard (sample data for now)
                val leaderboard by mainViewModel.leaderboard.collectAsState()
                
                // Determine start destination based on auth state
                val startDestination = remember(authState) {
                    when (authState) {
                        is AuthState.Authenticated -> Screen.Home.route
                        is AuthState.Loading -> Screen.Onboarding.route
                        else -> Screen.Onboarding.route
                    }
                }
                
                // Handle auth state changes
                LaunchedEffect(authState) {
                    when (authState) {
                        is AuthState.Authenticated -> {
                            // Set user ID for repository
                            authViewModel.getCurrentUserId()?.let { uid ->
                                mainViewModel.setUserId(uid)
                            }
                            
                            if (navController.currentDestination?.route != Screen.Home.route) {
                                navController.navigate(Screen.Home.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }
                        is AuthState.NotAuthenticated -> {
                            if (navController.currentDestination?.route != Screen.Onboarding.route) {
                                navController.navigate(Screen.Onboarding.route) {
                                    popUpTo(0) { inclusive = true }
                                }
                            }
                        }
                        else -> {}
                    }
                }
                
                val backgroundColor = if (appSettings.themeMode == com.vimal.roadsense.data.ThemeMode.LIGHT) SurfaceLight else DarkBackground
                
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = backgroundColor
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = startDestination
                    ) {
                        composable(
                            route = Screen.Onboarding.route,
                            enterTransition = { fadeIn() },
                            exitTransition = { fadeOut() }
                        ) {
                            OnboardingScreen(
                                onComplete = { name, phone, vehicle ->
                                    authViewModel.completeOnboarding(name, phone, vehicle)
                                }
                            )
                        }
                        
                        composable(
                            route = Screen.Home.route,
                            enterTransition = { fadeIn() + slideInHorizontally { it / 3 } },
                            exitTransition = { fadeOut() + slideOutHorizontally { -it / 3 } }
                        ) {
                            HomeScreenNew(
                                detectionState = detectionState,
                                recentPotholes = recentPotholes,
                                allPotholes = allPotholes,
                                displayName = displayName.ifEmpty { "User" },
                                vehicleType = selectedVehicle,
                                userSettings = userSettings,
                                isOnline = isOnline,
                                pendingUploads = pendingUploads,
                                onStartStop = {
                                    if (detectionState.isDetecting) {
                                        mainViewModel.stopDetection()
                                    } else {
                                        mainViewModel.startDetection(
                                            vehicleThresholds = selectedVehicle.thresholds.adjustForSensitivity(
                                                userSettings.detectionSensitivity
                                            )
                                        )
                                    }
                                },
                                onProfileClick = {
                                    navController.navigate(Screen.Profile.route)
                                },
                                onTripHistoryClick = {
                                    navController.navigate(Screen.TripHistory.route)
                                },
                                onLeaderboardClick = {
                                    navController.navigate(Screen.Leaderboard.route)
                                },
                                onSettingsClick = {
                                    navController.navigate(Screen.Settings.route)
                                }
                            )
                        }
                        
                        composable(
                            route = Screen.Profile.route,
                            enterTransition = { slideInHorizontally { it } + fadeIn() },
                            exitTransition = { slideOutHorizontally { it } + fadeOut() }
                        ) {
                            ProfileScreen(
                                displayName = displayName.ifEmpty { "User" },
                                phoneNumber = phoneNumber,
                                vehicleType = selectedVehicle,
                                totalPotholes = allPotholes.size,
                                userSettings = userSettings,
                                pendingUploads = pendingUploads,
                                onVehicleChange = { vehicle ->
                                    authViewModel.updateVehicle(vehicle)
                                },
                                onUpdateSettings = { settings ->
                                    authViewModel.updateSettings(settings)
                                },
                                onSyncNow = {
                                    mainViewModel.syncPendingUploads()
                                },
                                onLogout = {
                                    // Stop detection if running
                                    if (detectionState.isDetecting) {
                                        mainViewModel.stopDetection()
                                    }
                                    authViewModel.signOut()
                                },
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        
                        composable(
                            route = Screen.Settings.route,
                            enterTransition = { slideInHorizontally { it } + fadeIn() },
                            exitTransition = { slideOutHorizontally { it } + fadeOut() }
                        ) {
                            SettingsScreen(
                                settings = appSettings,
                                onSettingsChange = { settings ->
                                    authViewModel.updateAppSettings(settings)
                                },
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        
                        composable(
                            route = Screen.TripHistory.route,
                            enterTransition = { slideInHorizontally { it } + fadeIn() },
                            exitTransition = { slideOutHorizontally { it } + fadeOut() }
                        ) {
                            TripHistoryScreen(
                                trips = trips,
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                        
                        composable(
                            route = Screen.Leaderboard.route,
                            enterTransition = { slideInHorizontally { it } + fadeIn() },
                            exitTransition = { slideOutHorizontally { it } + fadeOut() }
                        ) {
                            LeaderboardScreen(
                                leaderboard = leaderboard,
                                currentUserId = authViewModel.getCurrentUserId() ?: "",
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
    
    override fun onStart() {
        super.onStart()
        // Rebind to service if it's running
        mainViewModel.bindToService()
    }
}
