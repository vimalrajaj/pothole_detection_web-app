package com.vimal.roadsense.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.vimal.roadsense.ui.theme.*
import kotlin.math.cos
import kotlin.math.sin

/**
 * Animated background orbs for creating ambient atmosphere
 * Reusable across all screens
 */
@Composable
fun AnimatedBackgroundOrbs(
    modifier: Modifier = Modifier,
    primaryColor: Color = AccentBlue,
    secondaryColor: Color = AccentPurple,
    isActive: Boolean = false,
    activeColor: Color = AccentRed
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orbs")
    
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )
    
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.2f,
        targetValue = if (isActive) 0.5f else 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    val orbColor = if (isActive) activeColor else primaryColor
    
    Box(modifier = modifier.fillMaxSize()) {
        // Primary orb - top left
        Box(
            modifier = Modifier
                .size(if (isActive) 450.dp else 350.dp)
                .offset(
                    x = (-100 + cos(Math.toRadians(angle.toDouble())) * 50).dp,
                    y = (-150 + sin(Math.toRadians(angle.toDouble())) * 40).dp
                )
                .blur(180.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            orbColor.copy(alpha = pulseAlpha),
                            Color.Transparent
                        )
                    )
                )
                .animateContentSize()
        )
        
        // Secondary orb - bottom right
        Box(
            modifier = Modifier
                .size(280.dp)
                .offset(
                    x = (200 + sin(Math.toRadians(angle * 0.7)) * 40).dp,
                    y = (500 + cos(Math.toRadians(angle * 0.7)) * 50).dp
                )
                .blur(150.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            secondaryColor.copy(alpha = pulseAlpha * 0.6f),
                            Color.Transparent
                        )
                    )
                )
        )
        
        // Tertiary orb - center right (subtle)
        Box(
            modifier = Modifier
                .size(200.dp)
                .offset(
                    x = (280 + cos(Math.toRadians(angle * 1.3)) * 30).dp,
                    y = (200 + sin(Math.toRadians(angle * 1.3)) * 35).dp
                )
                .blur(120.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AccentGreen.copy(alpha = pulseAlpha * 0.3f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}

/**
 * Simplified orbs for lighter screens
 */
@Composable
fun SubtleBackgroundOrbs(
    modifier: Modifier = Modifier,
    accentColor: Color = AccentBlue
) {
    val infiniteTransition = rememberInfiniteTransition(label = "subtle_orbs")
    
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )
    
    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(
                    x = (-80 + cos(Math.toRadians(angle.toDouble())) * 30).dp,
                    y = (-100 + sin(Math.toRadians(angle.toDouble())) * 25).dp
                )
                .blur(160.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    )
                )
        )
        
        Box(
            modifier = Modifier
                .size(250.dp)
                .offset(
                    x = (220 + sin(Math.toRadians(angle * 0.8)) * 25).dp,
                    y = (450 + cos(Math.toRadians(angle * 0.8)) * 30).dp
                )
                .blur(140.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            AccentPurple.copy(alpha = 0.2f),
                            Color.Transparent
                        )
                    )
                )
        )
    }
}
