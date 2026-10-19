package com.vimal.roadsense.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.vimal.roadsense.data.DetectionState
import com.vimal.roadsense.data.PotholeData
import com.vimal.roadsense.data.VehicleType
import com.vimal.roadsense.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HomeScreen(
    detectionState: DetectionState,
    recentPotholes: List<PotholeData>,
    vehicleType: VehicleType = VehicleType.BIKE,
    isOnline: Boolean = true,
    pendingUploads: Int = 0,
    onStartStop: () -> Unit,
    onChangeVehicle: () -> Unit = {},
    onLogout: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasPermissions by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var showMenu by remember { mutableStateOf(false) }
    
    val requiredPermissions = remember {
        mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                add(Manifest.permission.POST_NOTIFICATIONS)
            }
        }.toTypedArray()
    }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasPermissions = permissions.values.all { it }
        if (hasPermissions) {
            onStartStop()
        } else {
            showPermissionDialog = true
        }
    }
    
    LaunchedEffect(Unit) {
        hasPermissions = requiredPermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
    
    // Permission Dialog
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { 
                Text("Permission Required", fontWeight = FontWeight.Bold) 
            },
            text = { 
                Text("Location permission is needed to detect potholes and record their positions.") 
            },
            confirmButton = {
                Button(
                    onClick = { 
                        showPermissionDialog = false
                        permissionLauncher.launch(requiredPermissions)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
                ) {
                    Text("Grant Permission")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPermissionDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CardDark,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary
        )
    }
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0A0A14),
                        Color(0xFF12121F),
                        Color(0xFF0A0A14)
                    )
                )
            )
    ) {
        // Animated background
        AnimatedBackground(isActive = detectionState.isDetecting)
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            
            // Header with menu and sync status
            HeaderSection(
                vehicleType = vehicleType,
                showMenu = showMenu,
                isOnline = isOnline,
                pendingUploads = pendingUploads,
                onMenuToggle = { showMenu = !showMenu },
                onChangeVehicle = onChangeVehicle,
                onLogout = onLogout,
                onDismissMenu = { showMenu = false }
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Main Control Button
            MainControlButton(
                isDetecting = detectionState.isDetecting,
                potholeCount = detectionState.potholeCount,
                onToggle = {
                    if (!hasPermissions && !detectionState.isDetecting) {
                        permissionLauncher.launch(requiredPermissions)
                    } else {
                        onStartStop()
                    }
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Status Text
            StatusText(isDetecting = detectionState.isDetecting, vehicleType = vehicleType)
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Stats Row
            StatsRow(
                speed = detectionState.currentSpeed,
                acceleration = detectionState.lastAcceleration
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Live Monitor Card
            LiveMonitorCard(
                isDetecting = detectionState.isDetecting,
                speed = detectionState.currentSpeed,
                acceleration = detectionState.lastAcceleration
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Recent Detections
            RecentDetections(potholes = recentPotholes)
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}


@Composable
private fun AnimatedBackground(isActive: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.03f,
        targetValue = if (isActive) 0.12f else 0.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    Canvas(modifier = Modifier.fillMaxSize()) {
        val color = if (isActive) AccentRed else AccentBlue
        
        drawCircle(
            color = color.copy(alpha = alpha),
            radius = 350.dp.toPx(),
            center = Offset(size.width * 0.8f, size.height * 0.1f)
        )
        
        drawCircle(
            color = AccentPurple.copy(alpha = alpha * 0.6f),
            radius = 250.dp.toPx(),
            center = Offset(size.width * 0.1f, size.height * 0.85f)
        )
    }
}

@Composable
private fun HeaderSection(
    vehicleType: VehicleType,
    showMenu: Boolean,
    isOnline: Boolean = true,
    pendingUploads: Int = 0,
    onMenuToggle: () -> Unit,
    onChangeVehicle: () -> Unit,
    onLogout: () -> Unit,
    onDismissMenu: () -> Unit
) {
    val vehicleEmoji = when (vehicleType) {
        VehicleType.BIKE -> "🏍️"
        VehicleType.BICYCLE -> "🚲"
        VehicleType.CAR -> "🚗"
        VehicleType.AUTO -> "🛺"
        VehicleType.BUS -> "🚌"
    }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Road-Sense",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Connection status indicator
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isOnline) AccentGreen else AccentOrange)
                )
                Text(
                    text = if (isOnline) {
                        if (pendingUploads > 0) "Syncing $pendingUploads..." else "Real-time Sync"
                    } else {
                        "Offline Mode"
                    },
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
        }
        
        // Vehicle badge and menu
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Vehicle badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(CardDark)
                    .border(1.dp, BorderColor, RoundedCornerShape(12.dp))
                    .clickable(onClick = onChangeVehicle)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(text = vehicleEmoji, fontSize = 18.sp)
                    Text(
                        text = vehicleType.displayName,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                }
            }
            
            // Menu button
            Box {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(CardDark)
                        .border(1.dp, BorderColor, CircleShape)
                        .clickable(onClick = onMenuToggle),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Settings,
                        contentDescription = "Settings",
                        tint = TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = onDismissMenu,
                    modifier = Modifier.background(CardDark)
                ) {
                    DropdownMenuItem(
                        text = { 
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(text = vehicleEmoji, fontSize = 18.sp)
                                Text("Change Vehicle", color = TextPrimary)
                            }
                        },
                        onClick = {
                            onDismissMenu()
                            onChangeVehicle()
                        }
                    )
                    DropdownMenuItem(
                        text = { 
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(
                                    Icons.Rounded.ExitToApp,
                                    contentDescription = null,
                                    tint = AccentRed,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text("Logout", color = AccentRed)
                            }
                        },
                        onClick = {
                            onDismissMenu()
                            onLogout()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MainControlButton(
    isDetecting: Boolean,
    potholeCount: Int,
    onToggle: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "main")
    
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    val actualScale by animateFloatAsState(
        targetValue = if (isDetecting) pulseScale else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    val ringColor by animateColorAsState(
        targetValue = if (isDetecting) AccentRed else AccentBlue,
        animationSpec = tween(400),
        label = "color"
    )
    
    Box(
        modifier = Modifier
            .size(260.dp)
            .scale(actualScale),
        contentAlignment = Alignment.Center
    ) {
        // Outer rotating ring
        Canvas(
            modifier = Modifier
                .size(260.dp)
                .rotate(rotation)
        ) {
            val radius = size.minDimension / 2 - 6.dp.toPx()
            val dashCount = 48
            
            for (i in 0 until dashCount) {
                val angle = (i * 360f / dashCount) * (Math.PI / 180)
                val length = if (i % 4 == 0) 14.dp.toPx() else 8.dp.toPx()
                val startX = center.x + (radius - length) * cos(angle).toFloat()
                val startY = center.y + (radius - length) * sin(angle).toFloat()
                val endX = center.x + radius * cos(angle).toFloat()
                val endY = center.y + radius * sin(angle).toFloat()
                
                drawLine(
                    color = ringColor.copy(alpha = if (i % 4 == 0) 0.8f else 0.3f),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = 2.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
        
        // Glow
        Box(
            modifier = Modifier
                .size(200.dp)
                .blur(40.dp)
                .clip(CircleShape)
                .background(ringColor.copy(alpha = if (isDetecting) 0.35f else 0.2f))
        )
        
        // Middle ring
        Box(
            modifier = Modifier
                .size(180.dp)
                .clip(CircleShape)
                .background(CardDark)
                .border(2.dp, ringColor.copy(alpha = 0.4f), CircleShape)
        )
        
        // Inner button
        Box(
            modifier = Modifier
                .size(145.dp)
                .shadow(16.dp, CircleShape)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = if (isDetecting) {
                            listOf(AccentRed, Color(0xFFFF8585))
                        } else {
                            listOf(AccentBlue, AccentPurple)
                        }
                    )
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { onToggle() },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    if (isDetecting) Icons.Rounded.Close else Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(52.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (isDetecting) "STOP" else "START",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.5.sp
                )
            }
        }
        
        // Count badge
        if (potholeCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = (-15).dp, y = 15.dp)
                    .size(52.dp)
                    .shadow(8.dp, CircleShape)
                    .clip(CircleShape)
                    .background(AccentOrange),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$potholeCount",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun StatusText(isDetecting: Boolean, vehicleType: VehicleType = VehicleType.BIKE) {
    AnimatedContent(
        targetState = isDetecting,
        transitionSpec = {
            fadeIn(tween(300)) togetherWith fadeOut(tween(300))
        },
        label = "status"
    ) { active ->
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = if (active) "● Detection Active" else "Tap to Start Detection",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = if (active) AccentRed else TextSecondary,
                textAlign = TextAlign.Center
            )
            if (active) {
                Text(
                    text = "Calibrated for ${vehicleType.displayName}",
                    fontSize = 12.sp,
                    color = TextSecondary.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun StatsRow(speed: Float, acceleration: Float) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        StatCard(
            title = "SPEED",
            value = String.format("%.0f", speed),
            unit = "km/h",
            icon = Icons.Rounded.KeyboardArrowUp,
            color = AccentGreen,
            modifier = Modifier.weight(1f)
        )
        
        StatCard(
            title = "G-FORCE",
            value = String.format("%.1f", acceleration / 9.8f),
            unit = "G",
            icon = Icons.Rounded.Notifications,
            color = if (acceleration < 7 || acceleration > 13) AccentRed else AccentBlue,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    unit: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(115.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(1.dp, BorderColor, RoundedCornerShape(20.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(30.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = color,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text = title,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondary,
                        letterSpacing = 0.5.sp
                    )
                }
                
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = value,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = unit,
                        fontSize = 13.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 5.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveMonitorCard(
    isDetecting: Boolean,
    speed: Float,
    acceleration: Float
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderColor, RoundedCornerShape(20.dp))
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Live Monitor",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        val infiniteTransition = rememberInfiniteTransition(label = "dot")
                        val dotAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.4f,
                            targetValue = 1f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(700),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "dotAlpha"
                        )
                        
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isDetecting) AccentGreen.copy(alpha = dotAlpha)
                                    else TextSecondary.copy(alpha = 0.4f)
                                )
                        )
                        Text(
                            text = if (isDetecting) "Active" else "Idle",
                            fontSize = 13.sp,
                            color = if (isDetecting) AccentGreen else TextSecondary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(18.dp))
                
                // Vibration Level Bar
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Vibration Level", fontSize = 13.sp, color = TextSecondary)
                        Text(
                            String.format("%.1f m/s²", acceleration),
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    val normalizedAccel = ((acceleration - 5f) / 15f).coerceIn(0f, 1f)
                    val animatedProgress by animateFloatAsState(
                        targetValue = normalizedAccel,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy),
                        label = "progress"
                    )
                    
                    val barColor = when {
                        acceleration < 7f || acceleration > 13f -> AccentRed
                        acceleration < 8f || acceleration > 12f -> AccentOrange
                        else -> AccentGreen
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp))
                            .background(Color(0xFF252538))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(animatedProgress)
                                .fillMaxHeight()
                                .clip(RoundedCornerShape(5.dp))
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(barColor, barColor.copy(alpha = 0.7f))
                                    )
                                )
                        )
                    }
                }
                
                // Speed warning
                if (isDetecting && speed < 10f) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(AccentOrange.copy(alpha = 0.12f))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            Icons.Rounded.Info,
                            contentDescription = null,
                            tint = AccentOrange,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Speed up to 10+ km/h for detection",
                            fontSize = 13.sp,
                            color = AccentOrange
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RecentDetections(potholes: List<PotholeData>) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Detections",
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            if (potholes.isNotEmpty()) {
                Text(
                    text = "See All",
                    fontSize = 14.sp,
                    color = AccentBlue,
                    modifier = Modifier.clickable { }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(14.dp))
        
        if (potholes.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(20.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, BorderColor, RoundedCornerShape(20.dp))
                        .padding(36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            Icons.Rounded.CheckCircle,
                            contentDescription = null,
                            tint = AccentGreen.copy(alpha = 0.4f),
                            modifier = Modifier.size(52.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No potholes detected yet",
                            fontSize = 15.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = "Start driving to begin",
                            fontSize = 13.sp,
                            color = TextSecondary.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                potholes.take(5).forEach { pothole ->
                    PotholeItem(pothole = pothole)
                }
            }
        }
    }
}

@Composable
private fun PotholeItem(pothole: PotholeData) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    val timeString = pothole.timestamp?.toDate()?.let { dateFormat.format(it) } ?: "Just now"
    
    val severityColor = when (pothole.severity) {
        3 -> AccentRed
        2 -> AccentOrange
        else -> AccentYellow
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(14.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, BorderColor, RoundedCornerShape(14.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(severityColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Rounded.Warning,
                        contentDescription = null,
                        tint = severityColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(14.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${pothole.severityLabel} Severity",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Text(
                        text = "${String.format("%.0f", pothole.speed)} km/h",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = timeString,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(severityColor.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = pothole.severityLabel.uppercase(),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = severityColor,
                            letterSpacing = 0.5.sp
                        )
                    }
                }
            }
        }
    }
}
