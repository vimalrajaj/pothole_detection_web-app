package com.vimal.roadsense.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vimal.roadsense.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * Main detection button with premium animations
 */
@Composable
fun DetectionButton(
    isDetecting: Boolean,
    potholeCount: Int,
    vehicleColor: Color,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val infiniteTransition = rememberInfiniteTransition(label = "detection_button")
    
    // Breathing animation when detecting
    val breatheScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )
    
    // Outer ring rotation
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
        modifier = modifier
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
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggle()
            },
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
                        // Icon with gradient background
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

/**
 * Compact detection toggle button
 */
@Composable
fun CompactDetectionButton(
    isDetecting: Boolean,
    vehicleColor: Color,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    
    val scale by animateFloatAsState(
        targetValue = if (isDetecting) 1.05f else 1f,
        animationSpec = if (isDetecting) {
            infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            )
        } else {
            spring()
        },
        label = "compact_scale"
    )
    
    Box(
        modifier = modifier
            .scale(scale)
            .size(64.dp)
            .clip(CircleShape)
            .background(
                brush = Brush.linearGradient(
                    colors = if (isDetecting) {
                        listOf(AccentRed, Color(0xFFFF6B6B))
                    } else {
                        listOf(vehicleColor, vehicleColor.copy(alpha = 0.8f))
                    }
                )
            )
            .clickable {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onToggle()
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isDetecting) Icons.Filled.Stop else Icons.Rounded.PlayArrow,
            contentDescription = if (isDetecting) "Stop Detection" else "Start Detection",
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
    }
}

/**
 * Animated counter for pothole count
 */
@Composable
fun AnimatedPotholeCounter(
    count: Int,
    modifier: Modifier = Modifier,
    color: Color = AccentBlue
) {
    var previousCount by remember { mutableIntStateOf(0) }
    val animatedCount by animateIntAsState(
        targetValue = count,
        animationSpec = tween(300),
        label = "count"
    )
    
    val scale by animateFloatAsState(
        targetValue = if (count > previousCount) 1.2f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "count_scale"
    )
    
    LaunchedEffect(count) {
        previousCount = count
    }
    
    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Text(
                text = "$animatedCount",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = "detected",
                fontSize = 14.sp,
                color = TextSecondary
            )
        }
    }
}
