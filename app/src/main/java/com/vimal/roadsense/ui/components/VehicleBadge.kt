package com.vimal.roadsense.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vimal.roadsense.data.VehicleType
import com.vimal.roadsense.ui.theme.*

/**
 * Get color for vehicle type
 */
fun getVehicleColor(vehicleType: VehicleType): Color = when (vehicleType) {
    VehicleType.BIKE -> AccentBlue
    VehicleType.BICYCLE -> AccentGreen
    VehicleType.CAR -> AccentPurple
    VehicleType.AUTO -> AccentOrange
    VehicleType.BUS -> AccentRed
}

/**
 * Get emoji for vehicle type
 */
fun getVehicleEmoji(vehicleType: VehicleType): String = when (vehicleType) {
    VehicleType.BIKE -> "🏍️"
    VehicleType.BICYCLE -> "🚲"
    VehicleType.CAR -> "🚗"
    VehicleType.AUTO -> "🛺"
    VehicleType.BUS -> "🚌"
}

/**
 * Vehicle badge with emoji and color ring
 */
@Composable
fun VehicleBadge(
    vehicleType: VehicleType,
    size: Dp = 48.dp,
    modifier: Modifier = Modifier,
    showRing: Boolean = true,
    animated: Boolean = false
) {
    val color = getVehicleColor(vehicleType)
    val emoji = getVehicleEmoji(vehicleType)
    
    val scale by if (animated) {
        rememberInfiniteTransition(label = "badge_pulse").animateFloat(
            initialValue = 1f,
            targetValue = 1.05f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500),
                repeatMode = RepeatMode.Reverse
            ),
            label = "scale"
        )
    } else {
        remember { mutableStateOf(1f) }
    }
    
    Box(
        modifier = modifier
            .size(size)
            .scale(scale)
            .clip(CircleShape)
            .background(color.copy(alpha = 0.15f))
            .then(
                if (showRing) Modifier.border(2.dp, color.copy(alpha = 0.5f), CircleShape)
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            fontSize = (size.value * 0.45f).sp
        )
    }
}

/**
 * Vehicle badge button with profile icon
 */
@Composable
fun VehicleProfileButton(
    vehicleType: VehicleType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = getVehicleColor(vehicleType)
    val emoji = getVehicleEmoji(vehicleType)
    
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
            .border(1.dp, color.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(text = emoji, fontSize = 20.sp)
            Icon(
                Icons.Rounded.Person,
                contentDescription = "Profile",
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Large vehicle card for selection
 */
@Composable
fun VehicleSelectionCard(
    vehicleType: VehicleType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val color = getVehicleColor(vehicleType)
    val emoji = getVehicleEmoji(vehicleType)
    
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "selection_scale"
    )
    
    Box(
        modifier = modifier
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (isSelected) {
                    Brush.verticalGradient(
                        colors = listOf(
                            color.copy(alpha = 0.2f),
                            color.copy(alpha = 0.1f)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        colors = listOf(CardDark, CardDark)
                    )
                }
            )
            .border(
                width = if (isSelected) 2.dp else 1.dp,
                color = if (isSelected) color else BorderColor,
                shape = RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = emoji,
                fontSize = 40.sp
            )
            Text(
                text = vehicleType.displayName,
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) color else TextSecondary
            )
        }
    }
}

/**
 * Horizontal vehicle selector row
 */
@Composable
fun VehicleSelectorRow(
    selectedVehicle: VehicleType,
    onVehicleSelect: (VehicleType) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        VehicleType.entries.forEach { vehicle ->
            VehicleSelectionCard(
                vehicleType = vehicle,
                isSelected = vehicle == selectedVehicle,
                onClick = { onVehicleSelect(vehicle) },
                modifier = Modifier.weight(1f).padding(4.dp)
            )
        }
    }
}
