package com.vimal.roadsense.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vimal.roadsense.ui.theme.*

/**
 * Empty state component for when there's no data
 */
@Composable
fun EmptyState(
    emoji: String = "📭",
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    val infiniteTransition = rememberInfiniteTransition(label = "empty_bounce")
    val bounce by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 10f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bounce"
    )
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = emoji,
            fontSize = 64.sp,
            modifier = Modifier.offset(y = (-bounce).dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = description,
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(24.dp))
            
            GradientButton(
                text = actionLabel,
                onClick = onAction
            )
        }
    }
}

/**
 * Empty state with icon instead of emoji
 */
@Composable
fun EmptyStateWithIcon(
    icon: ImageVector,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    iconColor: Color = AccentBlue,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(iconColor.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(40.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = description,
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
        
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(24.dp))
            
            GradientButton(
                text = actionLabel,
                onClick = onAction
            )
        }
    }
}

/**
 * No potholes detected empty state
 */
@Composable
fun NoPotholesEmptyState(
    modifier: Modifier = Modifier,
    onStartDetection: () -> Unit
) {
    EmptyState(
        emoji = "🛣️",
        title = "No Potholes Yet",
        description = "Start detecting potholes to help improve road conditions in your area!",
        actionLabel = "Start Detection",
        onAction = onStartDetection,
        modifier = modifier
    )
}

/**
 * No trips empty state
 */
@Composable
fun NoTripsEmptyState(
    modifier: Modifier = Modifier
) {
    EmptyState(
        emoji = "🗺️",
        title = "No Trips Recorded",
        description = "Your trip history will appear here once you start detecting potholes.",
        modifier = modifier
    )
}

/**
 * Network error empty state
 */
@Composable
fun NetworkErrorState(
    modifier: Modifier = Modifier,
    onRetry: () -> Unit
) {
    EmptyStateWithIcon(
        icon = Icons.Filled.WifiOff,
        iconColor = AccentOrange,
        title = "No Connection",
        description = "Please check your internet connection and try again.",
        actionLabel = "Retry",
        onAction = onRetry,
        modifier = modifier
    )
}

/**
 * Loading state with animated indicator
 */
@Composable
fun LoadingState(
    message: String = "Loading...",
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .size(48.dp)
                .rotate(rotation),
            color = AccentBlue,
            strokeWidth = 4.dp
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = message,
            fontSize = 14.sp,
            color = TextSecondary
        )
    }
}

/**
 * Pulsing loading dots
 */
@Composable
fun LoadingDots(
    modifier: Modifier = Modifier,
    dotColor: Color = AccentBlue,
    dotSize: Int = 10
) {
    val infiniteTransition = rememberInfiniteTransition(label = "dots")
    
    val scales = (0..2).map { index ->
        infiniteTransition.animateFloat(
            initialValue = 0.6f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(600, delayMillis = index * 200, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "dot_$index"
        )
    }
    
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        scales.forEach { scale ->
            Box(
                modifier = Modifier
                    .size(dotSize.dp)
                    .scale(scale.value)
                    .clip(CircleShape)
                    .background(dotColor)
            )
        }
    }
}

/**
 * Full screen loading overlay
 */
@Composable
fun LoadingOverlay(
    isLoading: Boolean,
    message: String = "Please wait...",
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        content()
        
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBackground.copy(alpha = 0.8f)),
                contentAlignment = Alignment.Center
            ) {
                PremiumGlassCard(
                    modifier = Modifier.padding(32.dp),
                    contentPadding = PaddingValues(32.dp)
                ) {
                    LoadingDots()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = message,
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

/**
 * Success state with checkmark animation
 */
@Composable
fun SuccessState(
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    onContinue: (() -> Unit)? = null
) {
    val scale by rememberInfiniteTransition(label = "success").animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .scale(scale)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(SuccessGradient)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(40.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Text(
            text = title,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = description,
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        
        if (onContinue != null) {
            Spacer(modifier = Modifier.height(24.dp))
            
            GradientButton(
                text = "Continue",
                onClick = onContinue,
                gradientColors = SuccessGradient
            )
        }
    }
}

/**
 * Error state
 */
@Composable
fun ErrorState(
    title: String = "Something went wrong",
    description: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(AccentRed.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Error,
                contentDescription = null,
                tint = AccentRed,
                modifier = Modifier.size(40.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        Text(
            text = title,
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = description,
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(24.dp))
            
            OutlinedGradientButton(
                text = "Try Again",
                onClick = onRetry,
                borderColor = AccentRed,
                icon = Icons.Filled.Refresh
            )
        }
    }
}
