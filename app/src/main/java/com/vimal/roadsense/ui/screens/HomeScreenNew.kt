package com.vimal.roadsense.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.preference.PreferenceManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import com.vimal.roadsense.data.DetectionState
import com.vimal.roadsense.data.PotholeData
import com.vimal.roadsense.data.VehicleType
import com.vimal.roadsense.data.UserSettings
import com.vimal.roadsense.ui.theme.*
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun HomeScreenNew(
    detectionState: DetectionState,
    recentPotholes: List<PotholeData>,
    allPotholes: List<PotholeData>,
    displayName: String,
    vehicleType: VehicleType,
    userSettings: UserSettings,
    isOnline: Boolean,
    pendingUploads: Int,
    onStartStop: () -> Unit,
    onProfileClick: () -> Unit,
    onTripHistoryClick: () -> Unit = {},
    onLeaderboardClick: () -> Unit = {},
    onSettingsClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var hasPermissions by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    
    // Initialize OSMDroid configuration
    LaunchedEffect(Unit) {
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))
        Configuration.getInstance().userAgentValue = context.packageName
    }
    
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
    
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val gradientAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
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
    
    // Permission Dialog
    if (showPermissionDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDialog = false },
            title = { Text("Permission Required", fontWeight = FontWeight.Bold) },
            text = { Text("Location permission is needed to detect potholes and record their positions.") },
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

    var selectedTab by rememberSaveable { mutableStateOf(0) }
    val tabs = listOf("Live", "Map")
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Animated background orbs
        AnimatedBackgroundOrbs(
            isDetecting = detectionState.isDetecting,
            vehicleColor = vehicleColor,
            angle = gradientAngle
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            HeaderRow(
                displayName = displayName,
                vehicleEmoji = vehicleEmoji,
                vehicleColor = vehicleColor,
                isOnline = isOnline,
                pendingUploads = pendingUploads,
                onProfileClick = onProfileClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Tab selector
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = CardDark,
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[selectedTab])
                            .height(3.dp),
                        color = vehicleColor
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        selectedContentColor = Color.White,
                        unselectedContentColor = TextSecondary
                    ) {
                        Text(
                            text = title,
                            modifier = Modifier.padding(vertical = 12.dp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (selectedTab == 0) {
                // Live dashboard
                MainDetectionButton(
                    isDetecting = detectionState.isDetecting,
                    potholeCount = detectionState.potholeCount,
                    vehicleColor = vehicleColor,
                    onToggle = {
                        if (!hasPermissions && !detectionState.isDetecting) {
                            permissionLauncher.launch(requiredPermissions)
                        } else {
                            onStartStop()
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                StatusLabel(
                    isDetecting = detectionState.isDetecting,
                    vehicleType = vehicleType,
                    vehicleColor = vehicleColor
                )

                Spacer(modifier = Modifier.height(32.dp))

                LiveStatsRow(
                    speed = detectionState.currentSpeed,
                    acceleration = detectionState.lastAcceleration,
                    isDetecting = detectionState.isDetecting,
                    vehicleColor = vehicleColor
                )

                Spacer(modifier = Modifier.height(24.dp))

                AccelerometerGraph(
                    isDetecting = detectionState.isDetecting,
                    acceleration = detectionState.lastAcceleration,
                    vehicleColor = vehicleColor
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Quick Actions Row
                QuickActionsRow(
                    onTripHistoryClick = onTripHistoryClick,
                    onLeaderboardClick = onLeaderboardClick,
                    onSettingsClick = onSettingsClick
                )

                Spacer(modifier = Modifier.height(24.dp))

                RecentDetectionsSection(
                    potholes = recentPotholes,
                    vehicleColor = vehicleColor
                )

                Spacer(modifier = Modifier.height(32.dp))
            } else {
                PotholeMapSection(
                    allPotholes = allPotholes,
                    vehicleColor = vehicleColor,
                    vehicleEmoji = vehicleEmoji
                )
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun AnimatedBackgroundOrbs(
    isDetecting: Boolean,
    vehicleColor: Color,
    angle: Float
) {
    val pulseAlpha by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 0.2f,
        targetValue = if (isDetecting) 0.5f else 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Main orb - follows vehicle color
        Box(
            modifier = Modifier
                .size(if (isDetecting) 450.dp else 350.dp)
                .offset(
                    x = (-100 + cos(Math.toRadians(angle.toDouble())) * 50).dp,
                    y = (-150 + sin(Math.toRadians(angle.toDouble())) * 40).dp
                )
                .blur(180.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            (if (isDetecting) AccentRed else vehicleColor).copy(alpha = pulseAlpha),
                            Color.Transparent
                        )
                    )
                )
                .animateContentSize()
        )
        
        // Secondary orb
        Box(
            modifier = Modifier
                .size(280.dp)
                .offset(
                    x = (200 + sin(Math.toRadians(angle.toDouble() * 0.7)) * 40).dp,
                    y = (500 + cos(Math.toRadians(angle.toDouble() * 0.7)) * 50).dp
                )
                .blur(150.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AccentPurple.copy(alpha = pulseAlpha * 0.6f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

@Composable
private fun HeaderRow(
    displayName: String,
    vehicleEmoji: String,
    vehicleColor: Color,
    isOnline: Boolean,
    pendingUploads: Int,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Hello, ",
                    fontSize = 16.sp,
                    color = TextSecondary
                )
                Text(
                    text = displayName.split(" ").first() + "!",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isOnline) AccentGreen else AccentOrange)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isOnline) {
                        if (pendingUploads > 0) "Syncing $pendingUploads..." else "Connected"
                    } else "Offline",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
        
        // Profile button with vehicle
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(CardDark)
                .border(1.dp, vehicleColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .clickable(onClick = onProfileClick)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = vehicleEmoji, fontSize = 20.sp)
                Icon(
                    Icons.Rounded.Person,
                    contentDescription = "Profile",
                    tint = vehicleColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun MainDetectionButton(
    isDetecting: Boolean,
    potholeCount: Int,
    vehicleColor: Color,
    onToggle: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val infiniteTransition = rememberInfiniteTransition(label = "button_anim")
    
    // Smooth breathing animation when detecting
    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )
    
    // Outer ring rotation - slow and elegant
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(40000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ring"
    )
    
    // Glow pulse
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )
    
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.94f
            isDetecting -> breatheScale
            else -> 1f
        },
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    val primaryColor by animateColorAsState(
        targetValue = if (isDetecting) AccentRed else vehicleColor,
        animationSpec = tween(500),
        label = "color"
    )
    
    Box(
        modifier = Modifier
            .size(260.dp)
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        // Ambient glow behind the button
        if (isDetecting) {
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .blur(60.dp)
                    .alpha(glowAlpha)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(primaryColor, Color.Transparent)
                        ),
                        shape = CircleShape
                    )
            )
        }
        
        // Outer decorative ring with dots
        Canvas(
            modifier = Modifier
                .size(240.dp)
                .rotate(ringRotation)
        ) {
            val radius = size.minDimension / 2
            val dotCount = 48
            
            for (i in 0 until dotCount) {
                val angle = Math.toRadians((i * 360.0 / dotCount))
                val dotRadius = if (i % 4 == 0) 3.5.dp.toPx() else 1.8.dp.toPx()
                val x = center.x + (radius - 14.dp.toPx()) * cos(angle).toFloat()
                val y = center.y + (radius - 14.dp.toPx()) * sin(angle).toFloat()
                
                drawCircle(
                    color = primaryColor.copy(alpha = if (i % 4 == 0) 0.7f else 0.25f),
                    radius = dotRadius,
                    center = Offset(x, y)
                )
            }
        }
        
        // Progress ring when detecting
        if (isDetecting) {
            Canvas(modifier = Modifier.size(205.dp)) {
                drawArc(
                    color = primaryColor.copy(alpha = 0.2f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
        
        // Main button surface
        Surface(
            onClick = onToggle,
            modifier = Modifier.size(175.dp),
            shape = CircleShape,
            color = Color.Transparent,
            interactionSource = interactionSource
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                CardDark,
                                Color(0xFF15151F)
                            )
                        ),
                        shape = CircleShape
                    )
                    .border(
                        width = 2.dp,
                        brush = Brush.linearGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.5f),
                                primaryColor.copy(alpha = 0.15f)
                            )
                        ),
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                // Inner gradient circle
                Box(
                    modifier = Modifier
                        .size(135.dp)
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    primaryColor.copy(alpha = 0.12f),
                                    Color.Transparent
                                )
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Icon with beautiful gradient background
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .shadow(16.dp, CircleShape)
                                .background(
                                    brush = Brush.linearGradient(
                                        colors = if (isDetecting) {
                                            listOf(AccentRed, Color(0xFFFF6B6B))
                                        } else {
                                            listOf(vehicleColor, vehicleColor.copy(alpha = 0.85f))
                                        }
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isDetecting) Icons.Filled.Close else Icons.Rounded.PlayArrow,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(30.dp)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(14.dp))
                        
                        Text(
                            text = if (isDetecting) "TAP TO STOP" else "TAP TO START",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary,
                            letterSpacing = 1.2.sp
                        )
                        
                        if (isDetecting && potholeCount > 0) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "$potholeCount detected",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = primaryColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusLabel(
    isDetecting: Boolean,
    vehicleType: VehicleType,
    vehicleColor: Color
) {
    AnimatedContent(
        targetState = isDetecting,
        transitionSpec = {
            fadeIn() + scaleIn() togetherWith fadeOut() + scaleOut()
        },
        label = "status"
    ) { detecting ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(if (detecting) AccentRed.copy(alpha = 0.15f) else vehicleColor.copy(alpha = 0.1f))
                .padding(horizontal = 20.dp, vertical = 10.dp)
        ) {
            if (detecting) {
                // Pulsing dot
                val pulseAlpha by rememberInfiniteTransition(label = "dot").animateFloat(
                    initialValue = 0.4f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(800),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "alpha"
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(AccentRed.copy(alpha = pulseAlpha))
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Detection Active",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = AccentRed
                )
            } else {
                Icon(
                    Icons.Filled.LocationOn,
                    contentDescription = null,
                    tint = vehicleColor,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ready with ${vehicleType.displayName}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = vehicleColor
                )
            }
        }
    }
}

@Composable
private fun LiveStatsRow(
    speed: Float,
    acceleration: Float,
    isDetecting: Boolean,
    vehicleColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LiveStatCard(
            icon = Icons.Filled.KeyboardArrowUp,
            value = String.format("%.1f", speed),
            unit = "km/h",
            label = "Speed",
            color = if (isDetecting) AccentBlue else vehicleColor,
            isActive = isDetecting,
            modifier = Modifier.weight(1f)
        )
        
        LiveStatCard(
            icon = Icons.Filled.Notifications,
            value = String.format("%.1f", acceleration),
            unit = "m/s²",
            label = "Accel",
            color = if (isDetecting && acceleration > 15f) AccentRed else AccentOrange,
            isActive = isDetecting,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun LiveStatCard(
    icon: ImageVector,
    value: String,
    unit: String,
    label: String,
    color: Color,
    isActive: Boolean,
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
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = if (isActive) value else "--",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) Color.White else TextSecondary
                )
                Text(
                    text = unit,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun AccelerometerGraph(
    isDetecting: Boolean,
    acceleration: Float,
    vehicleColor: Color
) {
    var history by remember { mutableStateOf(listOf<Float>()) }
    
    LaunchedEffect(acceleration) {
        if (isDetecting) {
            history = (history + acceleration).takeLast(30)
        }
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Live Sensor Data",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isDetecting) AccentGreen.copy(alpha = 0.15f) else CardLight)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isDetecting) "LIVE" else "IDLE",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDetecting) AccentGreen else TextSecondary,
                        letterSpacing = 1.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Graph
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                val maxValue = 30f
                val graphHeight = size.height
                val graphWidth = size.width
                
                // Draw threshold line
                val thresholdY = graphHeight * (1 - 12f / maxValue)
                drawLine(
                    color = AccentRed.copy(alpha = 0.3f),
                    start = Offset(0f, thresholdY),
                    end = Offset(graphWidth, thresholdY),
                    strokeWidth = 1.dp.toPx(),
                    pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                        floatArrayOf(10f, 10f)
                    )
                )
                
                // Draw graph line
                if (history.size > 1) {
                    val step = graphWidth / (history.size - 1).coerceAtLeast(1)
                    
                    for (i in 0 until history.size - 1) {
                        val x1 = i * step
                        val x2 = (i + 1) * step
                        val y1 = graphHeight * (1 - (history[i].coerceIn(0f, maxValue) / maxValue))
                        val y2 = graphHeight * (1 - (history[i + 1].coerceIn(0f, maxValue) / maxValue))
                        
                        val lineColor = if (history[i + 1] > 12f) AccentRed else vehicleColor
                        
                        drawLine(
                            color = lineColor,
                            start = Offset(x1, y1),
                            end = Offset(x2, y2),
                            strokeWidth = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
                
                // Draw baseline
                drawLine(
                    color = BorderColor,
                    start = Offset(0f, graphHeight),
                    end = Offset(graphWidth, graphHeight),
                    strokeWidth = 1.dp.toPx()
                )
            }
            
            // Labels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Normal",
                    fontSize = 10.sp,
                    color = TextSecondary
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(AccentRed.copy(alpha = 0.5f))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Pothole threshold",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentDetectionsSection(
    potholes: List<PotholeData>,
    vehicleColor: Color
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Recent Detections",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            if (potholes.isNotEmpty()) {
                Text(
                    text = "${potholes.size} total",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (potholes.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "🛣️", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No potholes detected yet",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    Text(
                        text = "Start detection and drive around",
                        fontSize = 14.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(potholes.take(10)) { pothole ->
                    PotholeCard(pothole = pothole, color = vehicleColor)
                }
            }
        }
    }
}

@Composable
private fun PotholeCard(
    pothole: PotholeData,
    color: Color
) {
    val dateFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }
    val severityColor = when {
        pothole.severity >= 3 -> AccentRed
        pothole.severity >= 2 -> AccentOrange
        else -> AccentGreen
    }
    
    // Get timestamp as milliseconds
    val timestampMillis = pothole.timestamp?.toDate()?.time ?: System.currentTimeMillis()
    
    Card(
        modifier = Modifier.width(160.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(severityColor.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = when {
                            pothole.severity >= 3 -> "Severe"
                            pothole.severity >= 2 -> "Medium"
                            else -> "Mild"
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = severityColor
                    )
                }
                
                Text(
                    text = dateFormat.format(java.util.Date(timestampMillis)),
                    fontSize = 11.sp,
                    color = TextSecondary
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Rounded.LocationOn,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = String.format("%.4f, %.4f", pothole.latitude, pothole.longitude),
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${String.format("%.1f", pothole.accelerationDelta)} m/s²",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Text(
                    text = "${String.format("%.0f", pothole.speed)} km/h",
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun PotholeMapSection(
    allPotholes: List<PotholeData>,
    vehicleColor: Color,
    vehicleEmoji: String
) {
    val context = LocalContext.current
    var selectedSeverity by rememberSaveable { mutableStateOf("All") }
    var selectedRange by rememberSaveable { mutableStateOf("7d") }

    val filteredPotholes = remember(allPotholes, selectedSeverity, selectedRange) {
        val now = System.currentTimeMillis()
        val cutoff = when (selectedRange) {
            "24h" -> now - 24 * 60 * 60 * 1000L
            "7d" -> now - 7 * 24 * 60 * 60 * 1000L
            "30d" -> now - 30 * 24 * 60 * 60 * 1000L
            else -> Long.MIN_VALUE
        }

        allPotholes.filter { pothole ->
            val ts = pothole.timestamp?.toDate()?.time ?: 0L
            val inRange = cutoff == Long.MIN_VALUE || ts >= cutoff
            val severityOk = when (selectedSeverity) {
                "High" -> pothole.severity >= 3
                "Medium" -> pothole.severity == 2
                "Low" -> pothole.severity <= 1
                else -> true
            }
            inRange && severityOk
        }
    }

    val startLocation = filteredPotholes.firstOrNull() ?: allPotholes.firstOrNull()
    val startLat = startLocation?.latitude ?: 20.5937
    val startLon = startLocation?.longitude ?: 78.9629
    val startZoom = if (startLocation != null) 14.0 else 5.0

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Detected Potholes Map",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Tap markers to see details",
                        fontSize = 13.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Text(text = vehicleEmoji, fontSize = 22.sp)
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilterChip(
                    selected = selectedSeverity == "All",
                    onClick = { selectedSeverity = "All" },
                    label = { Text("All") },
                    leadingIcon = null
                )
                FilterChip(
                    selected = selectedSeverity == "High",
                    onClick = { selectedSeverity = "High" },
                    label = { Text("High") },
                    leadingIcon = { Icon(Icons.Filled.Warning, contentDescription = null, tint = AccentRed) }
                )
                FilterChip(
                    selected = selectedSeverity == "Medium",
                    onClick = { selectedSeverity = "Medium" },
                    label = { Text("Medium") },
                    leadingIcon = { Icon(Icons.Filled.Info, contentDescription = null, tint = AccentOrange) }
                )
                FilterChip(
                    selected = selectedSeverity == "Low",
                    onClick = { selectedSeverity = "Low" },
                    label = { Text("Low") },
                    leadingIcon = { Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = AccentGreen) }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val ranges = listOf("24h", "7d", "30d", "All")
                ranges.forEach { range ->
                    FilterChip(
                        selected = selectedRange == range,
                        onClick = { selectedRange = range },
                        label = { Text(range) },
                        leadingIcon = null
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(420.dp)
                    .padding(horizontal = 12.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                // OpenStreetMap view using osmdroid - always show the map
                AndroidView(
                    factory = { ctx ->
                        MapView(ctx).apply {
                            setTileSource(TileSourceFactory.MAPNIK)
                            setMultiTouchControls(true)
                            controller.setZoom(startZoom)
                            controller.setCenter(GeoPoint(startLat, startLon))
                            
                            // Add markers for each pothole
                            filteredPotholes.forEach { pothole ->
                                val marker = Marker(this)
                                marker.position = GeoPoint(pothole.latitude, pothole.longitude)
                                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                marker.title = "${pothole.severityLabel} severity"
                                marker.snippet = "Speed: ${String.format("%.0f", pothole.speed)} km/h"
                                overlays.add(marker)
                            }
                        }
                    },
                    update = { mapView ->
                        // Clear existing markers and add new ones
                        mapView.overlays.clear()
                        filteredPotholes.forEach { pothole ->
                            val marker = Marker(mapView)
                            marker.position = GeoPoint(pothole.latitude, pothole.longitude)
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            marker.title = "${pothole.severityLabel} severity"
                            marker.snippet = "Speed: ${String.format("%.0f", pothole.speed)} km/h"
                            mapView.overlays.add(marker)
                        }
                        mapView.invalidate()
                        
                        // Center on first pothole if available
                        filteredPotholes.firstOrNull()?.let { pothole ->
                            mapView.controller.animateTo(GeoPoint(pothole.latitude, pothole.longitude))
                        }
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                )
                
                // Show overlay message if no potholes match filters
                if (filteredPotholes.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.5f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = CardDark.copy(alpha = 0.95f)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(text = "📍", fontSize = 32.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No potholes match filters",
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendDot(color = AccentRed)
                Text("High", color = Color.White, fontSize = 12.sp)
                LegendDot(color = AccentOrange)
                Text("Medium", color = Color.White, fontSize = 12.sp)
                LegendDot(color = AccentGreen)
                Text("Low", color = Color.White, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color) {
    Box(
        modifier = Modifier
            .size(10.dp)
            .clip(CircleShape)
            .background(color)
    )
}

@Suppress("unused")
private fun severityColor(severity: Int): Color {
    return when {
        severity >= 3 -> AccentRed
        severity == 2 -> AccentOrange
        else -> AccentGreen
    }
}

@Composable
fun QuickActionsRow(
    onTripHistoryClick: () -> Unit,
    onLeaderboardClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        QuickActionItem(
            icon = Icons.Filled.History,
            label = "Trips",
            color = AccentBlue,
            onClick = onTripHistoryClick
        )
        QuickActionItem(
            icon = Icons.Filled.Star,
            label = "Leaders",
            color = AccentOrange,
            onClick = onLeaderboardClick
        )
        QuickActionItem(
            icon = Icons.Rounded.Settings,
            label = "Settings",
            color = AccentPurple,
            onClick = onSettingsClick
        )
    }
}

@Composable
fun QuickActionItem(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    color.copy(alpha = 0.15f),
                    RoundedCornerShape(16.dp)
                )
                .border(
                    width = 1.dp,
                    color = color.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            label,
            fontSize = 12.sp,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}
