package com.vimal.roadsense.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vimal.roadsense.data.VehicleType
import com.vimal.roadsense.ui.theme.*

/**
 * App header with greeting and profile button
 */
@Composable
fun HomeHeader(
    displayName: String,
    vehicleType: VehicleType,
    isOnline: Boolean,
    pendingUploads: Int,
    onProfileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val vehicleColor = getVehicleColor(vehicleType)
    val vehicleEmoji = getVehicleEmoji(vehicleType)
    
    Row(
        modifier = modifier.fillMaxWidth(),
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
            
            Spacer(modifier = Modifier.height(4.dp))
            
            ConnectionStatusChip(
                isOnline = isOnline,
                pendingUploads = pendingUploads
            )
        }
        
        VehicleProfileButton(
            vehicleType = vehicleType,
            onClick = onProfileClick
        )
    }
}

/**
 * Screen header with back button
 */
@Composable
fun ScreenHeader(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = CardDark.copy(alpha = 0.7f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButtonWithBackground(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                onClick = onBack,
                backgroundColor = CardLight
            )
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                if (emoji != null) {
                    Text(
                        text = emoji,
                        fontSize = 20.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = title,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Row(content = actions)
        }
    }
}

/**
 * Collapsing app bar for detail screens
 */
@Composable
fun CollapsingHeader(
    title: String,
    subtitle: String? = null,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    gradientColors: List<Color> = PremiumGradient,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = gradientColors.map { it.copy(alpha = 0.3f) } + DarkBackground
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButtonWithBackground(
                    icon = Icons.AutoMirrored.Filled.ArrowBack,
                    onClick = onBack,
                    backgroundColor = CardDark.copy(alpha = 0.5f)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column {
                    Text(
                        text = title,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (subtitle != null) {
                        Text(
                            text = subtitle,
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
            
            content()
        }
    }
}

/**
 * Simple top bar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleTopBar(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    emoji: String? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (emoji != null) {
                    Text(text = emoji, fontSize = 20.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Text(
                    text = title,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = DarkBackground,
            titleContentColor = Color.White,
            navigationIconContentColor = Color.White,
            actionIconContentColor = Color.White
        ),
        modifier = modifier
    )
}

/**
 * Tab selector component
 */
@Composable
fun TabSelector(
    tabs: List<String>,
    selectedIndex: Int,
    onTabSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = AccentBlue
) {
    TabRow(
        selectedTabIndex = selectedIndex,
        containerColor = CardDark,
        contentColor = Color.White,
        modifier = modifier.clip(RoundedCornerShape(16.dp)),
        indicator = { tabPositions ->
            TabRowDefaults.SecondaryIndicator(
                modifier = Modifier.tabIndicatorOffset(tabPositions[selectedIndex]),
                height = 3.dp,
                color = accentColor
            )
        }
    ) {
        tabs.forEachIndexed { index, title ->
            Tab(
                selected = selectedIndex == index,
                onClick = { onTabSelected(index) },
                selectedContentColor = Color.White,
                unselectedContentColor = TextSecondary
            ) {
                Text(
                    text = title,
                    modifier = Modifier.padding(vertical = 12.dp),
                    fontWeight = if (selectedIndex == index) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

/**
 * Segmented button selector
 */
@Composable
fun SegmentedSelector(
    options: List<String>,
    selectedIndex: Int,
    onOptionSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    accentColor: Color = AccentBlue
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CardDark)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        options.forEachIndexed { index, option ->
            val isSelected = index == selectedIndex
            
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        if (isSelected) accentColor.copy(alpha = 0.2f)
                        else Color.Transparent
                    )
                    .clickable { onOptionSelected(index) }
                    .padding(vertical = 10.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = option,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isSelected) accentColor else TextSecondary
                )
            }
        }
    }
}
