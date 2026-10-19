package com.vimal.roadsense.ui.screens

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.automirrored.rounded.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vimal.roadsense.data.VehicleType
import com.vimal.roadsense.data.UserSettings
import com.vimal.roadsense.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ProfileScreen(
    displayName: String,
    phoneNumber: String,
    vehicleType: VehicleType,
    totalPotholes: Int,
    userSettings: UserSettings,
    pendingUploads: Int,
    onVehicleChange: (VehicleType) -> Unit,
    onUpdateSettings: (UserSettings) -> Unit,
    onSyncNow: () -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    var showVehicleSelector by remember { mutableStateOf(false) }
    var showAchievements by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var sensitivity by rememberSaveable { mutableStateOf(userSettings.detectionSensitivity) }
    var notificationsEnabled by rememberSaveable { mutableStateOf(userSettings.notificationsEnabled) }
    var autoSync by rememberSaveable { mutableStateOf(userSettings.autoSync) }
    var wifiOnly by rememberSaveable { mutableStateOf(userSettings.wifiOnlySync) }

    val openAppSettings = {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.fromParts("package", context.packageName, null)
        }
        context.startActivity(intent)
    }

    val openLocationSettings = {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        context.startActivity(intent)
    }

    val openNotificationSettings = {
        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
        context.startActivity(intent)
    }
    
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val gradientAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )
    
    val vehicleColor = when (vehicleType) {
        VehicleType.BIKE -> AccentBlue
        VehicleType.BICYCLE -> AccentGreen
        VehicleType.CAR -> AccentPurple
        VehicleType.AUTO -> AccentOrange
        VehicleType.BUS -> AccentRed
    }
    
    val vehicleEmoji = when (vehicleType) {
        VehicleType.BIKE -> "🏍️"
        VehicleType.BICYCLE -> "🚲"
        VehicleType.CAR -> "🚗"
        VehicleType.AUTO -> "🛺"
        VehicleType.BUS -> "🚌"
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Background orbs
        Box(
            modifier = Modifier
                .size(350.dp)
                .offset(
                    x = (-80 + kotlin.math.cos(Math.toRadians(gradientAngle.toDouble())) * 40).dp,
                    y = (-100 + kotlin.math.sin(Math.toRadians(gradientAngle.toDouble())) * 30).dp
                )
                .blur(160.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(vehicleColor.copy(alpha = 0.4f), Color.Transparent)
                    )
                )
        )
        
        Box(
            modifier = Modifier
                .size(280.dp)
                .offset(
                    x = (200 + kotlin.math.sin(Math.toRadians(gradientAngle.toDouble())) * 30).dp,
                    y = (500 + kotlin.math.cos(Math.toRadians(gradientAngle.toDouble())) * 40).dp
                )
                .blur(140.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(AccentPurple.copy(alpha = 0.3f), Color.Transparent)
                    )
                )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar with glassmorphism effect
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = CardDark.copy(alpha = 0.7f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(CardLight)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    
                    Text(
                        text = "My Profile",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.size(44.dp))
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Premium Avatar Card
            PremiumAvatarCard(
                displayName = displayName,
                phoneNumber = phoneNumber,
                vehicleEmoji = vehicleEmoji,
                vehicleColor = vehicleColor,
                vehicleType = vehicleType,
                totalPotholes = totalPotholes
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Achievement Badges Row
            AchievementBadgesRow(
                totalPotholes = totalPotholes,
                vehicleColor = vehicleColor,
                onShowAll = { showAchievements = true }
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Stats Grid - 4 cards
            StatsGridSection(
                totalPotholes = totalPotholes,
                vehicleColor = vehicleColor
            )
            
            Spacer(modifier = Modifier.height(20.dp))

            DetectionSettingsCard(
                sensitivity = sensitivity,
                notificationsEnabled = notificationsEnabled,
                autoSync = autoSync,
                wifiOnly = wifiOnly,
                pendingUploads = pendingUploads,
                vehicleColor = vehicleColor,
                onSensitivityChange = { value -> sensitivity = value },
                onNotificationsToggle = { enabled -> notificationsEnabled = enabled },
                onAutoSyncToggle = { enabled -> autoSync = enabled },
                onWifiOnlyToggle = { enabled -> wifiOnly = enabled },
                onSave = {
                    onUpdateSettings(
                        userSettings.copy(
                            detectionSensitivity = sensitivity,
                            notificationsEnabled = notificationsEnabled,
                            autoSync = autoSync,
                            wifiOnlySync = wifiOnly
                        )
                    )
                },
                onSyncNow = onSyncNow
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            PermissionsCard(
                onOpenAppSettings = { openAppSettings() },
                onOpenLocationSettings = { openLocationSettings() }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Vehicle selection card with animation
            VehicleSelectionCard(
                vehicleEmoji = vehicleEmoji,
                vehicleType = vehicleType,
                vehicleColor = vehicleColor,
                onClick = { showVehicleSelector = true }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Quick Actions
            QuickActionsCard(
                vehicleColor = vehicleColor,
                onNotificationsClick = { openNotificationSettings() },
                onPrivacyClick = { openAppSettings() },
                onAboutClick = { openAppSettings() }
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Danger Zone
            DangerZoneCard(onLogout = onLogout)
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // App Info Footer
            AppInfoFooter()
            
            Spacer(modifier = Modifier.height(32.dp))
        }
        
        // Vehicle selector bottom sheet
        if (showVehicleSelector) {
            VehicleSelectorSheet(
                currentVehicle = vehicleType,
                onSelect = { vehicle ->
                    onVehicleChange(vehicle)
                    showVehicleSelector = false
                },
                onDismiss = { showVehicleSelector = false }
            )
        }
        
        // Achievements sheet
        if (showAchievements) {
            AchievementsSheet(
                totalPotholes = totalPotholes,
                onDismiss = { showAchievements = false }
            )
        }
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Text(
                text = value,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 12.dp)
            )
            
            Text(
                text = label,
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
private fun DetectionSettingsCard(
    sensitivity: Float,
    notificationsEnabled: Boolean,
    autoSync: Boolean,
    wifiOnly: Boolean,
    pendingUploads: Int,
    vehicleColor: Color,
    onSensitivityChange: (Float) -> Unit,
    onNotificationsToggle: (Boolean) -> Unit,
    onAutoSyncToggle: (Boolean) -> Unit,
    onWifiOnlyToggle: (Boolean) -> Unit,
    onSave: () -> Unit,
    onSyncNow: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Detection Settings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Tune sensitivity & syncing",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Icon(Icons.Filled.Settings, contentDescription = null, tint = AccentBlue)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Detection sensitivity",
                fontSize = 14.sp,
                color = TextSecondary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Slider(
                    value = sensitivity,
                    onValueChange = { onSensitivityChange(it.coerceIn(0.6f, 1.4f)) },
                    valueRange = 0.6f..1.4f,
                    steps = 6,
                    colors = SliderDefaults.colors(thumbColor = AccentBlue, activeTrackColor = AccentBlue)
                )
                Text(
                    text = when {
                        sensitivity > 1.15f -> "Aggressive"
                        sensitivity < 0.85f -> "Relaxed"
                        else -> "Balanced"
                    },
                    fontSize = 12.sp,
                    color = Color.White,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Higher sensitivity lowers thresholds to catch more bumps",
                fontSize = 12.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            SettingToggleRow(
                title = "Detection notifications",
                subtitle = "Get alerts when a pothole is captured",
                checked = notificationsEnabled,
                onCheckedChange = onNotificationsToggle,
                icon = Icons.Filled.Notifications
            )

            SettingToggleRow(
                title = "Auto sync",
                subtitle = if (pendingUploads > 0) "$pendingUploads pending uploads" else "Sync detections to cloud",
                checked = autoSync,
                onCheckedChange = onAutoSyncToggle,
                icon = Icons.Filled.Settings
            )

            SettingToggleRow(
                title = "Wi‑Fi only sync",
                subtitle = "Avoid mobile data for uploads",
                checked = wifiOnly,
                onCheckedChange = onWifiOnlyToggle,
                icon = Icons.Filled.Settings
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onSyncNow,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, AccentGreen.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentGreen),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sync now")
                }

                Button(
                    onClick = onSave,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Save")
                }
            }
        }
    }
}

@Composable
private fun SettingToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    icon: ImageVector
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(CardLight),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = TextSecondary)
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
            Text(text = subtitle, fontSize = 12.sp, color = TextSecondary)
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(checkedThumbColor = AccentBlue, checkedTrackColor = AccentBlue.copy(alpha = 0.4f))
        )
    }
}

@Composable
private fun PermissionsCard(
    onOpenAppSettings: () -> Unit,
    onOpenLocationSettings: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Permissions", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(
                        text = "Location + notifications",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Icon(Icons.Filled.Lock, contentDescription = null, tint = AccentOrange)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onOpenLocationSettings,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentBlue)
                ) {
                    Icon(Icons.Filled.LocationOn, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Location")
                }

                OutlinedButton(
                    onClick = onOpenAppSettings,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, AccentPurple.copy(alpha = 0.6f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentPurple)
                ) {
                    Icon(Icons.Filled.Settings, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("App settings")
                }
            }
        }
    }
}

data class SettingsItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val onClick: () -> Unit
)

@Composable
private fun SettingsCard(items: List<SettingsItem>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Column {
            items.forEachIndexed { index, item ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { item.onClick() }
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(CardLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            item.icon,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = item.title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                        Text(
                            text = item.subtitle,
                            fontSize = 13.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    
                    Icon(
                        Icons.Filled.ArrowForward,
                        contentDescription = null,
                        tint = TextSecondary.copy(alpha = 0.5f),
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                if (index < items.size - 1) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 80.dp),
                        color = BorderColor
                    )
                }
            }
        }
    }
}

@Composable
private fun VehicleSelectorSheet(
    currentVehicle: VehicleType,
    onSelect: (VehicleType) -> Unit,
    onDismiss: () -> Unit
) {
    val vehicles = listOf(
        Triple(VehicleType.BIKE, "🏍️", AccentBlue),
        Triple(VehicleType.BICYCLE, "🚲", AccentGreen),
        Triple(VehicleType.CAR, "🚗", AccentPurple),
        Triple(VehicleType.AUTO, "🛺", AccentOrange),
        Triple(VehicleType.BUS, "🚌", AccentRed)
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(onClick = onDismiss)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .clickable(enabled = false) { },
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Handle
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(CardLight)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "Change Vehicle",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Text(
                    text = "Select your current ride",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Vehicle options
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    vehicles.forEach { (type, emoji, color) ->
                        val isSelected = currentVehicle == type
                        
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(type) },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) color.copy(alpha = 0.15f) else CardLight
                            ),
                            border = if (isSelected) BorderStroke(2.dp, color) else null
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = emoji, fontSize = 32.sp)
                                
                                Spacer(modifier = Modifier.width(16.dp))
                                
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = type.displayName,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (isSelected) Color.White else TextSecondary
                                    )
                                    Text(
                                        text = getVehicleDescription(type),
                                        fontSize = 12.sp,
                                        color = TextSecondary.copy(alpha = 0.7f)
                                    )
                                }
                                
                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(color),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            Icons.Rounded.Check,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

private fun getVehicleDescription(type: VehicleType): String {
    return when (type) {
        VehicleType.BIKE -> "Scooter, Motorcycle"
        VehicleType.BICYCLE -> "Cycle, E-bike"
        VehicleType.CAR -> "Car, SUV, Sedan"
        VehicleType.AUTO -> "Auto Rickshaw"
        VehicleType.BUS -> "Bus, Truck, Heavy"
    }
}

// ============ NEW AWESOME COMPOSABLES ============

@Composable
private fun PremiumAvatarCard(
    displayName: String,
    phoneNumber: String,
    vehicleEmoji: String,
    vehicleColor: Color,
    vehicleType: VehicleType,
    totalPotholes: Int
) {
    val infiniteTransition = rememberInfiniteTransition(label = "avatar")
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring"
    )
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = CardDark
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            vehicleColor.copy(alpha = 0.1f),
                            Color.Transparent
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar with animated ring
                Box(
                    modifier = Modifier.size(130.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Animated outer ring
                    Canvas(
                        modifier = Modifier
                            .size(130.dp)
                            .rotate(ringRotation)
                    ) {
                        val radius = size.minDimension / 2
                        for (i in 0 until 24) {
                            val angle = Math.toRadians((i * 15.0))
                            val dotRadius = if (i % 3 == 0) 4.dp.toPx() else 2.dp.toPx()
                            val x = center.x + (radius - 8.dp.toPx()) * cos(angle).toFloat()
                            val y = center.y + (radius - 8.dp.toPx()) * sin(angle).toFloat()
                            drawCircle(
                                color = vehicleColor.copy(alpha = if (i % 3 == 0) 0.8f else 0.3f),
                                radius = dotRadius,
                                center = Offset(x, y)
                            )
                        }
                    }
                    
                    // Glow
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .blur(30.dp)
                            .background(
                                brush = Brush.radialGradient(
                                    colors = listOf(vehicleColor.copy(alpha = 0.5f), Color.Transparent)
                                )
                            )
                    )
                    
                    // Avatar
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(CircleShape)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(vehicleColor, vehicleColor.copy(alpha = 0.7f))
                                )
                            )
                            .border(3.dp, vehicleColor.copy(alpha = 0.4f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (displayName.isNotEmpty()) displayName.first().uppercase() else "?",
                            fontSize = 40.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    
                    // Vehicle badge
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 4.dp, y = 4.dp)
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(CardDark)
                            .border(2.dp, vehicleColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = vehicleEmoji, fontSize = 20.sp)
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Name
                Text(
                    text = displayName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                // Phone with chip
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = CardLight,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Rounded.Phone,
                            contentDescription = null,
                            tint = vehicleColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "+91 $phoneNumber",
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Level badge
                val level = when {
                    totalPotholes >= 100 -> "🏆 Legend"
                    totalPotholes >= 50 -> "💎 Expert"
                    totalPotholes >= 20 -> "⭐ Pro"
                    totalPotholes >= 5 -> "🌟 Active"
                    else -> "🚀 Beginner"
                }
                
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = vehicleColor.copy(alpha = 0.15f),
                    border = BorderStroke(1.dp, vehicleColor.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = level,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = vehicleColor,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AchievementBadgesRow(
    totalPotholes: Int,
    vehicleColor: Color,
    onShowAll: () -> Unit
) {
    val achievements = listOf(
        Triple("🎯", "First Find", totalPotholes >= 1),
        Triple("🔥", "On Fire", totalPotholes >= 5),
        Triple("⚡", "Speed Demon", totalPotholes >= 10),
        Triple("🛡️", "Guardian", totalPotholes >= 25),
        Triple("👑", "Road King", totalPotholes >= 50)
    )
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = CardDark
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Achievements",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                TextButton(onClick = onShowAll) {
                    Text(
                        text = "See All",
                        color = vehicleColor,
                        fontSize = 13.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                achievements.forEach { (emoji, name, unlocked) ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(
                                    if (unlocked) vehicleColor.copy(alpha = 0.15f) 
                                    else CardLight
                                )
                                .alpha(if (unlocked) 1f else 0.4f),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = emoji,
                                fontSize = 22.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = name,
                            fontSize = 9.sp,
                            color = if (unlocked) Color.White else TextSecondary,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsGridSection(
    totalPotholes: Int,
    vehicleColor: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MiniStatCard(
                emoji = "🕳️",
                value = totalPotholes.toString(),
                label = "Potholes Found",
                color = AccentOrange,
                modifier = Modifier.weight(1f)
            )
            MiniStatCard(
                emoji = "⭐",
                value = "${totalPotholes * 10}",
                label = "Points Earned",
                color = AccentGreen,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MiniStatCard(
                emoji = "🛣️",
                value = "${String.format("%.1f", totalPotholes * 0.5)}",
                label = "Km Traveled",
                color = AccentBlue,
                modifier = Modifier.weight(1f)
            )
            MiniStatCard(
                emoji = "📍",
                value = "${totalPotholes}",
                label = "Reports Sent",
                color = AccentPurple,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MiniStatCard(
    emoji: String,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = CardDark
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = emoji, fontSize = 22.sp)
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    text = value,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun VehicleSelectionCard(
    vehicleEmoji: String,
    vehicleType: VehicleType,
    vehicleColor: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = CardDark
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                vehicleColor.copy(alpha = 0.2f),
                                vehicleColor.copy(alpha = 0.1f)
                            )
                        )
                    )
                    .border(1.dp, vehicleColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = vehicleEmoji, fontSize = 28.sp)
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Current Vehicle",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
                Text(
                    text = vehicleType.displayName,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            
            Surface(
                shape = CircleShape,
                color = vehicleColor.copy(alpha = 0.15f)
            ) {
                Icon(
                    Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    tint = vehicleColor,
                    modifier = Modifier
                        .padding(8.dp)
                        .size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickActionsCard(
    vehicleColor: Color,
    onNotificationsClick: () -> Unit,
    onPrivacyClick: () -> Unit,
    onAboutClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = CardDark
    ) {
        Column {
            QuickActionRow(
                icon = Icons.Rounded.Notifications,
                iconColor = AccentBlue,
                title = "Notifications",
                subtitle = "Manage alerts & sounds",
                onClick = onNotificationsClick
            )
            HorizontalDivider(color = BorderColor, modifier = Modifier.padding(start = 72.dp))
            QuickActionRow(
                icon = Icons.Rounded.Lock,
                iconColor = AccentGreen,
                title = "Privacy",
                subtitle = "Data & permissions",
                onClick = onPrivacyClick
            )
            HorizontalDivider(color = BorderColor, modifier = Modifier.padding(start = 72.dp))
            QuickActionRow(
                icon = Icons.Rounded.Info,
                iconColor = AccentPurple,
                title = "About Road-Sense",
                subtitle = "Version 1.0.0 • Made with ❤️",
                onClick = onAboutClick
            )
        }
    }
}

@Composable
private fun QuickActionRow(
    icon: ImageVector,
    iconColor: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(22.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            
            Icon(
                Icons.AutoMirrored.Rounded.ArrowForward,
                contentDescription = null,
                tint = TextSecondary.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun DangerZoneCard(onLogout: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = AccentRed.copy(alpha = 0.08f),
        border = BorderStroke(1.dp, AccentRed.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onLogout)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.AutoMirrored.Rounded.ExitToApp,
                contentDescription = null,
                tint = AccentRed,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Sign Out",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = AccentRed
            )
        }
    }
}

@Composable
private fun AppInfoFooter() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🛣️ Road-Sense",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary
        )
        Text(
            text = "Making roads safer, one pothole at a time",
            fontSize = 11.sp,
            color = TextSecondary.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(
            text = "v1.0.0 • Smart City Hackathon 2026",
            fontSize = 10.sp,
            color = TextSecondary.copy(alpha = 0.4f),
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
private fun AchievementsSheet(
    totalPotholes: Int,
    onDismiss: () -> Unit
) {
    val allAchievements = listOf(
        Achievement("🎯", "First Find", "Detect your first pothole", 1, AccentBlue),
        Achievement("🔥", "On Fire", "Detect 5 potholes", 5, AccentOrange),
        Achievement("⚡", "Speed Demon", "Detect 10 potholes", 10, AccentGreen),
        Achievement("🌟", "Rising Star", "Detect 15 potholes", 15, AccentPurple),
        Achievement("🛡️", "Road Guardian", "Detect 25 potholes", 25, AccentBlue),
        Achievement("💎", "Diamond Hunter", "Detect 40 potholes", 40, AccentGreen),
        Achievement("👑", "Road King", "Detect 50 potholes", 50, AccentOrange),
        Achievement("🏆", "Legend", "Detect 100 potholes", 100, AccentRed)
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(onClick = onDismiss)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .clickable(enabled = false) { },
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(CardLight)
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                Text(
                    text = "🏆 Achievements",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Text(
                    text = "Unlock badges by detecting potholes",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                allAchievements.forEach { achievement ->
                    val unlocked = totalPotholes >= achievement.requirement
                    val progress = (totalPotholes.toFloat() / achievement.requirement).coerceAtMost(1f)
                    
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = if (unlocked) achievement.color.copy(alpha = 0.1f) else CardLight
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (unlocked) achievement.color.copy(alpha = 0.2f)
                                        else CardDark
                                    )
                                    .alpha(if (unlocked) 1f else 0.5f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = achievement.emoji, fontSize = 24.sp)
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = achievement.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (unlocked) Color.White else TextSecondary
                                )
                                Text(
                                    text = achievement.description,
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                                if (!unlocked) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = { progress },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .clip(RoundedCornerShape(2.dp)),
                                        color = achievement.color,
                                        trackColor = CardDark,
                                    )
                                }
                            }
                            
                            if (unlocked) {
                                Icon(
                                    Icons.Rounded.Check,
                                    contentDescription = null,
                                    tint = achievement.color,
                                    modifier = Modifier.size(24.dp)
                                )
                            } else {
                                Text(
                                    text = "${totalPotholes}/${achievement.requirement}",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

private data class Achievement(
    val emoji: String,
    val title: String,
    val description: String,
    val requirement: Int,
    val color: Color
)
