package com.vimal.roadsense.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vimal.roadsense.ui.theme.*

/**
 * Online/Offline status indicator chip
 */
@Composable
fun ConnectionStatusChip(
    isOnline: Boolean,
    pendingUploads: Int = 0,
    modifier: Modifier = Modifier
) {
    val dotColor by animateColorAsState(
        targetValue = when {
            !isOnline -> AccentOrange
            pendingUploads > 0 -> AccentYellow
            else -> AccentGreen
        },
        animationSpec = tween(300),
        label = "dot_color"
    )
    
    val pulseAlpha by rememberInfiniteTransition(label = "status_pulse").animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CardDark.copy(alpha = 0.7f))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(
                    if (pendingUploads > 0 || !isOnline) 
                        dotColor.copy(alpha = pulseAlpha) 
                    else dotColor
                )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = when {
                !isOnline -> "Offline"
                pendingUploads > 0 -> "Syncing $pendingUploads..."
                else -> "Connected"
            },
            fontSize = 12.sp,
            color = TextSecondary
        )
    }
}

/**
 * Detection status chip with animation
 */
@Composable
fun DetectionStatusChip(
    isDetecting: Boolean,
    vehicleColor: Color,
    vehicleName: String,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (isDetecting) AccentRed.copy(alpha = 0.15f) else vehicleColor.copy(alpha = 0.1f),
        animationSpec = tween(300),
        label = "bg_color"
    )
    
    val textColor by animateColorAsState(
        targetValue = if (isDetecting) AccentRed else vehicleColor,
        animationSpec = tween(300),
        label = "text_color"
    )
    
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(backgroundColor)
            .padding(horizontal = 20.dp, vertical = 10.dp)
    ) {
        if (isDetecting) {
            PulsingDot(color = AccentRed)
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Detection Active",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        } else {
            Icon(
                Icons.Filled.LocationOn,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Ready with $vehicleName",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )
        }
    }
}

/**
 * Pulsing dot indicator
 */
@Composable
fun PulsingDot(
    color: Color,
    size: Int = 10,
    modifier: Modifier = Modifier
) {
    val pulseAlpha by rememberInfiniteTransition(label = "dot_pulse").animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )
    
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(color.copy(alpha = pulseAlpha))
    )
}

/**
 * Generic status chip with icon
 */
@Composable
fun StatusChip(
    text: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

/**
 * Severity chip for pothole severity indication
 */
@Composable
fun SeverityChip(
    severity: Int,
    modifier: Modifier = Modifier
) {
    val (color, icon, label) = when (severity) {
        3 -> Triple(SeverityHigh, Icons.Filled.Warning, "High")
        2 -> Triple(SeverityMedium, Icons.Filled.Error, "Medium")
        else -> Triple(SeverityLow, Icons.Filled.CheckCircle, "Low")
    }
    
    StatusChip(
        text = label,
        icon = icon,
        color = color,
        modifier = modifier
    )
}
