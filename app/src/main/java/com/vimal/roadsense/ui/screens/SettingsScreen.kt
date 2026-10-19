package com.vimal.roadsense.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
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
import com.vimal.roadsense.data.AppLanguage
import com.vimal.roadsense.data.AppSettings
import com.vimal.roadsense.data.ThemeMode
import com.vimal.roadsense.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: AppSettings,
    onSettingsChange: (AppSettings) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "⚙️ Settings",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DarkBackground,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = DarkBackground
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Appearance Section
            item {
                SettingsSectionHeader(
                    icon = Icons.Filled.ColorLens,
                    title = "Appearance",
                    color = AccentPurple
                )
            }
            
            item {
                // Theme Setting
                SettingsCard {
                    SettingsItemComposable(
                        icon = Icons.Filled.Brightness4,
                        title = "Theme",
                        subtitle = when (settings.themeMode) {
                            ThemeMode.LIGHT -> "Light Mode"
                            ThemeMode.DARK -> "Dark Mode"
                            ThemeMode.SYSTEM -> "Follow System"
                        },
                        onClick = { showThemeDialog = true }
                    )
                    
                    HorizontalDivider(color = BorderColor)
                    
                    SettingsItemComposable(
                        icon = Icons.Filled.Language,
                        title = "Language",
                        subtitle = settings.language.nativeName,
                        onClick = { showLanguageDialog = true }
                    )
                }
            }
            
            // Detection Section
            item {
                SettingsSectionHeader(
                    icon = Icons.Filled.Sensors,
                    title = "Detection",
                    color = AccentBlue
                )
            }
            
            item {
                SettingsCard {
                    SettingsSlider(
                        icon = Icons.Filled.Tune,
                        title = "Detection Sensitivity",
                        value = settings.detectionSensitivity,
                        onValueChange = {
                            onSettingsChange(settings.copy(detectionSensitivity = it))
                        },
                        valueRange = 0.5f..1.5f
                    )
                    
                    HorizontalDivider(color = BorderColor)
                    
                    SettingsSwitch(
                        icon = Icons.Filled.VolumeUp,
                        title = "Voice Alerts",
                        subtitle = "Announce potholes while driving",
                        checked = settings.voiceAlertsEnabled,
                        onCheckedChange = {
                            onSettingsChange(settings.copy(voiceAlertsEnabled = it))
                        }
                    )
                    
                    HorizontalDivider(color = BorderColor)
                    
                    SettingsSwitch(
                        icon = Icons.Filled.Map,
                        title = "Show Heatmap",
                        subtitle = "Display pothole density on map",
                        checked = settings.showHeatmap,
                        onCheckedChange = {
                            onSettingsChange(settings.copy(showHeatmap = it))
                        }
                    )
                    
                    HorizontalDivider(color = BorderColor)
                    
                    SettingsSwitch(
                        icon = Icons.Filled.Navigation,
                        title = "Track Trips",
                        subtitle = "Record trip history automatically",
                        checked = settings.trackTrips,
                        onCheckedChange = {
                            onSettingsChange(settings.copy(trackTrips = it))
                        }
                    )
                }
            }
            
            // Notifications Section
            item {
                SettingsSectionHeader(
                    icon = Icons.Filled.Notifications,
                    title = "Notifications",
                    color = AccentOrange
                )
            }
            
            item {
                SettingsCard {
                    SettingsSwitch(
                        icon = Icons.Filled.NotificationsActive,
                        title = "Push Notifications",
                        subtitle = "Get alerts for nearby potholes",
                        checked = settings.notificationsEnabled,
                        onCheckedChange = {
                            onSettingsChange(settings.copy(notificationsEnabled = it))
                        }
                    )
                }
            }
            
            // Sync Section
            item {
                SettingsSectionHeader(
                    icon = Icons.Filled.Cloud,
                    title = "Data & Sync",
                    color = AccentGreen
                )
            }
            
            item {
                SettingsCard {
                    SettingsSwitch(
                        icon = Icons.Filled.Sync,
                        title = "Auto Sync",
                        subtitle = "Automatically upload pothole data",
                        checked = settings.autoSync,
                        onCheckedChange = {
                            onSettingsChange(settings.copy(autoSync = it))
                        }
                    )
                    
                    HorizontalDivider(color = BorderColor)
                    
                    SettingsSwitch(
                        icon = Icons.Filled.Wifi,
                        title = "WiFi Only Sync",
                        subtitle = "Sync only when connected to WiFi",
                        checked = settings.wifiOnlySync,
                        onCheckedChange = {
                            onSettingsChange(settings.copy(wifiOnlySync = it))
                        },
                        enabled = settings.autoSync
                    )
                }
            }
            
            // App Info
            item {
                Spacer(modifier = Modifier.height(16.dp))
                
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "🛣️ RoadSense v1.0.0",
                        fontSize = 14.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Made with ❤️ for Smart City Hackathon",
                        fontSize = 12.sp,
                        color = TextSecondary.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
    
    // Theme Dialog
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = settings.themeMode,
            onThemeSelected = { theme ->
                onSettingsChange(settings.copy(themeMode = theme))
                showThemeDialog = false
            },
            onDismiss = { showThemeDialog = false }
        )
    }
    
    // Language Dialog
    if (showLanguageDialog) {
        LanguageSelectionDialog(
            currentLanguage = settings.language,
            onLanguageSelected = { language ->
                onSettingsChange(settings.copy(language = language))
                showLanguageDialog = false
            },
            onDismiss = { showLanguageDialog = false }
        )
    }
}

@Composable
fun SettingsSectionHeader(
    icon: ImageVector,
    title: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            title,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
fun SettingsCard(
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark)
    ) {
        Column(
            modifier = Modifier.padding(4.dp),
            content = content
        )
    }
}

@Composable
fun SettingsItemComposable(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(AccentBlue.copy(alpha = 0.1f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = AccentBlue,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Text(
                subtitle,
                fontSize = 13.sp,
                color = TextSecondary
            )
        }
        
        Icon(
            Icons.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun SettingsSwitch(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = enabled) { onCheckedChange(!checked) }
            .padding(16.dp)
            .then(if (!enabled) Modifier.alpha(0.5f) else Modifier),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(
                    if (checked) AccentGreen.copy(alpha = 0.1f) 
                    else CardLight,
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = if (checked) AccentGreen else TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Text(
                subtitle,
                fontSize = 13.sp,
                color = TextSecondary
            )
        }
        
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AccentGreen,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = CardLight
            )
        )
    }
}

@Composable
fun SettingsSlider(
    icon: ImageVector,
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(AccentPurple.copy(alpha = 0.1f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = AccentPurple,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            
            Text(
                when {
                    value < 0.8f -> "Low"
                    value > 1.2f -> "High"
                    else -> "Normal"
                },
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = AccentPurple
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = AccentPurple,
                activeTrackColor = AccentPurple,
                inactiveTrackColor = CardLight
            ),
            modifier = Modifier.padding(horizontal = 24.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSelectionDialog(
    currentTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardDark,
        title = {
            Text(
                "🎨 Select Theme",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            Column {
                ThemeMode.entries.forEach { theme ->
                    val (icon, name) = when (theme) {
                        ThemeMode.LIGHT -> Icons.Filled.LightMode to "Light Mode"
                        ThemeMode.DARK -> Icons.Filled.DarkMode to "Dark Mode"
                        ThemeMode.SYSTEM -> Icons.Filled.BrightnessAuto to "System Default"
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onThemeSelected(theme) }
                            .background(
                                if (theme == currentTheme) AccentBlue.copy(alpha = 0.15f)
                                else Color.Transparent
                            )
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            icon,
                            contentDescription = null,
                            tint = if (theme == currentTheme) AccentBlue else TextSecondary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            name,
                            fontSize = 16.sp,
                            color = if (theme == currentTheme) AccentBlue else Color.White
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (theme == currentTheme) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                tint = AccentBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageSelectionDialog(
    currentLanguage: AppLanguage,
    onLanguageSelected: (AppLanguage) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = CardDark,
        title = {
            Text(
                "🌐 Select Language",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        text = {
            LazyColumn {
                items(AppLanguage.entries) { language ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onLanguageSelected(language) }
                            .background(
                                if (language == currentLanguage) AccentBlue.copy(alpha = 0.15f)
                                else Color.Transparent
                            )
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                language.nativeName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (language == currentLanguage) AccentBlue else Color.White
                            )
                            Text(
                                language.displayName,
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }
                        if (language == currentLanguage) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                tint = AccentBlue,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}
