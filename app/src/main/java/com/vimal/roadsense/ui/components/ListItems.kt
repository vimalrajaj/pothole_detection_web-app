package com.vimal.roadsense.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vimal.roadsense.data.PotholeData
import com.vimal.roadsense.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pothole detection item card
 */
@Composable
fun PotholeListItem(
    pothole: PotholeData,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()) }
    val formattedDate = remember(pothole.timestamp) {
        pothole.timestamp?.toDate()?.let { dateFormat.format(it) } ?: "Unknown"
    }
    
    val severityColor = when (pothole.severity) {
        3 -> SeverityHigh
        2 -> SeverityMedium
        else -> SeverityLow
    }
    
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Severity indicator
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(severityColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = null,
                tint = severityColor,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(14.dp))
        
        // Info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Pothole Detected",
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = formattedDate,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }
        
        // Severity badge
        SeverityChip(severity = pothole.severity)
    }
}

/**
 * Quick action button for home screen
 */
@Composable
fun QuickActionButton(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = AccentBlue,
    badge: Int = 0
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            if (badge > 0) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .offset(x = 4.dp, y = (-4).dp)
                        .clip(CircleShape)
                        .background(AccentRed),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (badge > 9) "9+" else badge.toString(),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = TextSecondary
        )
    }
}

/**
 * Quick actions row
 */
@Composable
fun QuickActionsRow(
    onTripHistoryClick: () -> Unit,
    onLeaderboardClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        QuickActionButton(
            icon = Icons.Filled.History,
            label = "Trips",
            onClick = onTripHistoryClick,
            accentColor = AccentPurple,
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            icon = Icons.Filled.EmojiEvents,
            label = "Leaderboard",
            onClick = onLeaderboardClick,
            accentColor = AccentYellow,
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            icon = Icons.Filled.Settings,
            label = "Settings",
            onClick = onSettingsClick,
            accentColor = AccentGreen,
            modifier = Modifier.weight(1f)
        )
    }
}

/**
 * Settings list item
 */
@Composable
fun SettingsListItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconColor: Color = AccentBlue,
    trailing: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(14.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        }
        
        trailing?.invoke() ?: Icon(
            imageVector = Icons.Rounded.ChevronRight,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(20.dp)
        )
    }
}

/**
 * Settings toggle item
 */
@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    iconColor: Color = AccentBlue
) {
    SettingsListItem(
        icon = icon,
        title = title,
        subtitle = subtitle,
        onClick = { onCheckedChange(!checked) },
        iconColor = iconColor,
        modifier = modifier,
        trailing = {
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = AccentGreen,
                    uncheckedThumbColor = TextSecondary,
                    uncheckedTrackColor = CardLight
                )
            )
        }
    )
}

/**
 * Section header for settings/lists
 */
@Composable
fun SectionHeader(
    title: String,
    icon: ImageVector? = null,
    iconColor: Color = AccentBlue,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = title,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = iconColor
        )
    }
}

/**
 * Info banner card
 */
@Composable
fun InfoBanner(
    icon: ImageVector,
    message: String,
    modifier: Modifier = Modifier,
    accentColor: Color = AccentBlue,
    onClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(accentColor.copy(alpha = 0.1f))
            .border(1.dp, accentColor.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = message,
            fontSize = 14.sp,
            color = Color.White,
            modifier = Modifier.weight(1f)
        )
        if (onClick != null) {
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * Achievement badge
 */
@Composable
fun AchievementBadge(
    emoji: String,
    title: String,
    isUnlocked: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isUnlocked) CardDark else CardDark.copy(alpha = 0.5f))
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = emoji,
            fontSize = 32.sp,
            modifier = Modifier.then(
                if (!isUnlocked) Modifier.alpha(0.3f) else Modifier
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = if (isUnlocked) Color.White else TextSecondary
        )
        if (!isUnlocked) {
            Spacer(modifier = Modifier.height(4.dp))
            Icon(
                imageVector = Icons.Filled.Lock,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
