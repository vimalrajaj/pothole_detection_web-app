package com.vimal.roadsense.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vimal.roadsense.ui.theme.*

/**
 * Navigation item data
 */
data class NavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val badge: Int = 0
)

/**
 * Default navigation items
 */
val defaultNavItems = listOf(
    NavItem(
        route = "home",
        label = "Home",
        selectedIcon = Icons.Filled.Home,
        unselectedIcon = Icons.Outlined.Home
    ),
    NavItem(
        route = "map",
        label = "Map",
        selectedIcon = Icons.Filled.Map,
        unselectedIcon = Icons.Outlined.Map
    ),
    NavItem(
        route = "leaderboard",
        label = "Rank",
        selectedIcon = Icons.Filled.EmojiEvents,
        unselectedIcon = Icons.Outlined.EmojiEvents
    ),
    NavItem(
        route = "profile",
        label = "Profile",
        selectedIcon = Icons.Filled.Person,
        unselectedIcon = Icons.Outlined.Person
    )
)

/**
 * Modern floating bottom navigation bar with blur effect
 */
@Composable
fun FloatingBottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    items: List<NavItem> = defaultNavItems,
    accentColor: Color = AccentBlue
) {
    val haptic = LocalHapticFeedback.current
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        // Background with blur effect simulation
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .clip(RoundedCornerShape(35.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            CardDark.copy(alpha = 0.95f),
                            CardDark.copy(alpha = 0.98f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            GlassBorder,
                            Color.Transparent
                        )
                    ),
                    shape = RoundedCornerShape(35.dp)
                )
        )
        
        // Navigation items
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                NavBarItem(
                    item = item,
                    isSelected = currentRoute == item.route,
                    accentColor = accentColor,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNavigate(item.route)
                    }
                )
            }
        }
    }
}

@Composable
private fun NavBarItem(
    item: NavItem,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1f else 0.9f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "nav_scale"
    )
    
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) accentColor else TextSecondary,
        animationSpec = tween(200),
        label = "nav_color"
    )
    
    Box(
        modifier = Modifier
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(color = accentColor, bounded = true),
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.TopEnd) {
                // Icon with animated indicator
                Box(
                    modifier = Modifier
                        .size(if (isSelected) 42.dp else 36.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) accentColor.copy(alpha = 0.15f)
                            else Color.Transparent
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }
                
                // Badge
                if (item.badge > 0) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(AccentRed),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (item.badge > 9) "9+" else item.badge.toString(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
            
            // Label with animation
            AnimatedVisibility(
                visible = isSelected,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Text(
                    text = item.label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

/**
 * Compact bottom navigation bar
 */
@Composable
fun CompactBottomNavBar(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier,
    items: List<NavItem> = defaultNavItems,
    accentColor: Color = AccentBlue
) {
    val haptic = LocalHapticFeedback.current
    
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = SurfaceDark,
        tonalElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            items.forEach { item ->
                val isSelected = currentRoute == item.route
                
                NavigationBarItem(
                    selected = isSelected,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onNavigate(item.route)
                    },
                    icon = {
                        Icon(
                            imageVector = if (isSelected) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label
                        )
                    },
                    label = {
                        Text(
                            text = item.label,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = accentColor,
                        selectedTextColor = accentColor,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary,
                        indicatorColor = accentColor.copy(alpha = 0.12f)
                    )
                )
            }
        }
    }
}

/**
 * Bottom nav bar with center FAB
 */
@Composable
fun BottomNavBarWithFAB(
    currentRoute: String,
    onNavigate: (String) -> Unit,
    onFABClick: () -> Unit,
    modifier: Modifier = Modifier,
    fabIcon: ImageVector = Icons.Filled.Add,
    accentColor: Color = AccentBlue,
    isDetecting: Boolean = false
) {
    val haptic = LocalHapticFeedback.current
    
    val fabScale by animateFloatAsState(
        targetValue = if (isDetecting) 1.1f else 1f,
        animationSpec = if (isDetecting) {
            infiniteRepeatable(
                animation = tween(1000),
                repeatMode = RepeatMode.Reverse
            )
        } else {
            spring()
        },
        label = "fab_pulse"
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(90.dp)
    ) {
        // Background bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(65.dp)
                .align(Alignment.BottomCenter)
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(CardDark)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left items
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    SmallNavItem(
                        icon = Icons.Outlined.Home,
                        selectedIcon = Icons.Filled.Home,
                        label = "Home",
                        isSelected = currentRoute == "home",
                        accentColor = accentColor,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onNavigate("home")
                        }
                    )
                    SmallNavItem(
                        icon = Icons.Outlined.Map,
                        selectedIcon = Icons.Filled.Map,
                        label = "Map",
                        isSelected = currentRoute == "map",
                        accentColor = accentColor,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onNavigate("map")
                        }
                    )
                }
                
                // Spacer for FAB
                Spacer(modifier = Modifier.width(60.dp))
                
                // Right items
                Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    SmallNavItem(
                        icon = Icons.Outlined.EmojiEvents,
                        selectedIcon = Icons.Filled.EmojiEvents,
                        label = "Rank",
                        isSelected = currentRoute == "leaderboard",
                        accentColor = accentColor,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onNavigate("leaderboard")
                        }
                    )
                    SmallNavItem(
                        icon = Icons.Outlined.Person,
                        selectedIcon = Icons.Filled.Person,
                        label = "Profile",
                        isSelected = currentRoute == "profile",
                        accentColor = accentColor,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onNavigate("profile")
                        }
                    )
                }
            }
        }
        
        // Center FAB
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = 5.dp)
                .scale(fabScale)
                .size(60.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = if (isDetecting) {
                            listOf(AccentRed, Color(0xFFFF6B6B))
                        } else {
                            PremiumGradient
                        }
                    )
                )
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onFABClick()
                },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isDetecting) Icons.Filled.Stop else fabIcon,
                contentDescription = "Start/Stop Detection",
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
private fun SmallNavItem(
    icon: ImageVector,
    selectedIcon: ImageVector,
    label: String,
    isSelected: Boolean,
    accentColor: Color,
    onClick: () -> Unit
) {
    val iconColor by animateColorAsState(
        targetValue = if (isSelected) accentColor else TextSecondary,
        animationSpec = tween(200),
        label = "small_nav_color"
    )
    
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = if (isSelected) selectedIcon else icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = iconColor,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
