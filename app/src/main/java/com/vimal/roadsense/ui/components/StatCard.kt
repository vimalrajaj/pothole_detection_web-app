package com.vimal.roadsense.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vimal.roadsense.ui.theme.*

/**
 * Live stat card for displaying real-time values (speed, acceleration, etc.)
 */
@Composable
fun LiveStatCard(
    icon: ImageVector,
    value: String,
    unit: String,
    label: String,
    color: Color,
    isActive: Boolean = false,
    modifier: Modifier = Modifier
) {
    val animatedColor by animateColorAsState(
        targetValue = color,
        animationSpec = tween(300),
        label = "stat_color"
    )
    
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
                    tint = animatedColor,
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
                    text = value,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) animatedColor else Color.White
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = unit,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
        }
    }
}

/**
 * Neumorphic stat card with depth effect
 */
@Composable
fun NeumorphicStatCard(
    icon: ImageVector,
    value: String,
    label: String,
    accentColor: Color = AccentBlue,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        CardDark,
                        Color(0xFF0F0F18)
                    )
                )
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        CardLight.copy(alpha = 0.3f),
                        Color.Transparent
                    )
                ),
                shape = RoundedCornerShape(24.dp)
            )
            .padding(20.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = label,
                fontSize = 13.sp,
                color = TextSecondary
            )
        }
    }
}

/**
 * Mini stat card for compact displays
 */
@Composable
fun MiniStatCard(
    value: String,
    label: String,
    color: Color = AccentBlue,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = TextSecondary
        )
    }
}

/**
 * Row of live stats
 */
@Composable
fun LiveStatsRow(
    speed: Float,
    acceleration: Float,
    isDetecting: Boolean,
    vehicleColor: Color,
    modifier: Modifier = Modifier,
    speedIcon: ImageVector,
    accelIcon: ImageVector
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        LiveStatCard(
            icon = speedIcon,
            value = String.format("%.1f", speed),
            unit = "km/h",
            label = "Speed",
            color = if (isDetecting) AccentBlue else vehicleColor,
            isActive = isDetecting,
            modifier = Modifier.weight(1f)
        )
        
        LiveStatCard(
            icon = accelIcon,
            value = String.format("%.1f", acceleration),
            unit = "m/s²",
            label = "Accel",
            color = if (isDetecting && acceleration > 15f) AccentRed else AccentOrange,
            isActive = isDetecting,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Stats summary card for profile/dashboard
 */
@Composable
fun StatsSummaryCard(
    totalPotholes: Int,
    totalTrips: Int,
    totalDistance: String,
    modifier: Modifier = Modifier
) {
    PremiumGlassCard(
        modifier = modifier,
        gradientColors = listOf(AccentBlue, AccentPurple)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatItem(value = totalPotholes.toString(), label = "Potholes")
            StatDivider()
            StatItem(value = totalTrips.toString(), label = "Trips")
            StatDivider()
            StatItem(value = totalDistance, label = "Distance")
        }
    }
}

@Composable
private fun StatItem(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            fontSize = 12.sp,
            color = TextSecondary
        )
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(40.dp)
            .background(BorderColor)
    )
}
