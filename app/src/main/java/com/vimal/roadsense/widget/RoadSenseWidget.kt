package com.vimal.roadsense.widget

import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.glance.*
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.*
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.layout.*
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.vimal.roadsense.MainActivity

// Widget colors
val WidgetBackground = Color(0xFF1A1A2E)
val WidgetCard = Color(0xFF252540)
val WidgetAccentBlue = Color(0xFF6C63FF)
val WidgetAccentOrange = Color(0xFFFFAB4C)
val WidgetAccentGreen = Color(0xFF4ADE80)
val WidgetTextPrimary = Color(0xFFFFFFFF)
val WidgetTextSecondary = Color(0xFF9CA3AF)

// Preference keys for widget state
object WidgetKeys {
    val TOTAL_POTHOLES = intPreferencesKey("total_potholes")
    val TODAY_POTHOLES = intPreferencesKey("today_potholes")
    val TOTAL_TRIPS = intPreferencesKey("total_trips")
    val TOTAL_DISTANCE = intPreferencesKey("total_distance") // in meters
}

/**
 * RoadSense Home Screen Widget
 * Shows quick stats and launch button
 */
class RoadSenseWidget : GlanceAppWidget() {
    
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition
    
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            RoadSenseWidgetContent()
        }
    }
}

@Composable
fun RoadSenseWidgetContent() {
    val prefs = currentState<androidx.datastore.preferences.core.Preferences>()
    
    val totalPotholes = prefs[WidgetKeys.TOTAL_POTHOLES] ?: 0
    val todayPotholes = prefs[WidgetKeys.TODAY_POTHOLES] ?: 0
    val totalTrips = prefs[WidgetKeys.TOTAL_TRIPS] ?: 0
    val totalDistance = prefs[WidgetKeys.TOTAL_DISTANCE] ?: 0
    
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(WidgetBackground)
            .cornerRadius(24.dp)
            .padding(16.dp)
            .clickable(actionStartActivity<MainActivity>()),
        verticalAlignment = Alignment.Vertical.Top,
        horizontalAlignment = Alignment.Horizontal.Start
    ) {
        // Header
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Horizontal.Start,
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Text(
                text = "🛣️",
                style = TextStyle(fontSize = 24.sp)
            )
            Spacer(modifier = GlanceModifier.width(8.dp))
            Column {
                Text(
                    text = "RoadSense",
                    style = TextStyle(
                        color = ColorProvider(WidgetTextPrimary),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
                Text(
                    text = "Pothole Detection",
                    style = TextStyle(
                        color = ColorProvider(WidgetTextSecondary),
                        fontSize = 12.sp
                    )
                )
            }
        }
        
        Spacer(modifier = GlanceModifier.height(16.dp))
        
        // Stats Grid
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            // Total Potholes
            WidgetStatCard(
                emoji = "🕳️",
                value = "$totalPotholes",
                label = "Total",
                modifier = GlanceModifier.defaultWeight()
            )
            
            Spacer(modifier = GlanceModifier.width(8.dp))
            
            // Today
            WidgetStatCard(
                emoji = "📅",
                value = "$todayPotholes",
                label = "Today",
                modifier = GlanceModifier.defaultWeight()
            )
        }
        
        Spacer(modifier = GlanceModifier.height(8.dp))
        
        Row(
            modifier = GlanceModifier.fillMaxWidth(),
            horizontalAlignment = Alignment.Horizontal.CenterHorizontally
        ) {
            // Trips
            WidgetStatCard(
                emoji = "🚗",
                value = "$totalTrips",
                label = "Trips",
                modifier = GlanceModifier.defaultWeight()
            )
            
            Spacer(modifier = GlanceModifier.width(8.dp))
            
            // Distance
            WidgetStatCard(
                emoji = "📏",
                value = formatWidgetDistance(totalDistance),
                label = "Distance",
                modifier = GlanceModifier.defaultWeight()
            )
        }
        
        Spacer(modifier = GlanceModifier.height(12.dp))
        
        // Start Button
        Box(
            modifier = GlanceModifier
                .fillMaxWidth()
                .height(44.dp)
                .background(WidgetAccentGreen)
                .cornerRadius(12.dp)
                .clickable(actionStartActivity<MainActivity>()),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "▶️ Start Detection",
                style = TextStyle(
                    color = ColorProvider(WidgetTextPrimary),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}

@Composable
fun WidgetStatCard(
    emoji: String,
    value: String,
    label: String,
    modifier: GlanceModifier = GlanceModifier
) {
    Column(
        modifier = modifier
            .background(WidgetCard)
            .cornerRadius(12.dp)
            .padding(12.dp),
        horizontalAlignment = Alignment.Horizontal.CenterHorizontally,
        verticalAlignment = Alignment.Vertical.CenterVertically
    ) {
        Text(
            text = emoji,
            style = TextStyle(fontSize = 16.sp)
        )
        Spacer(modifier = GlanceModifier.height(4.dp))
        Text(
            text = value,
            style = TextStyle(
                color = ColorProvider(WidgetTextPrimary),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        )
        Text(
            text = label,
            style = TextStyle(
                color = ColorProvider(WidgetTextSecondary),
                fontSize = 10.sp
            )
        )
    }
}

fun formatWidgetDistance(meters: Int): String {
    return when {
        meters >= 1000 -> "${meters / 1000} km"
        else -> "$meters m"
    }
}

/**
 * Widget Receiver
 */
class RoadSenseWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = RoadSenseWidget()
}

/**
 * Helper to update widget state from anywhere in the app
 */
object WidgetUpdater {
    suspend fun updateStats(
        context: Context,
        totalPotholes: Int,
        todayPotholes: Int,
        totalTrips: Int,
        totalDistanceMeters: Int
    ) {
        val glanceId = GlanceAppWidgetManager(context)
            .getGlanceIds(RoadSenseWidget::class.java)
            .firstOrNull() ?: return
        
        updateAppWidgetState(context, glanceId) { prefs ->
            prefs[WidgetKeys.TOTAL_POTHOLES] = totalPotholes
            prefs[WidgetKeys.TODAY_POTHOLES] = todayPotholes
            prefs[WidgetKeys.TOTAL_TRIPS] = totalTrips
            prefs[WidgetKeys.TOTAL_DISTANCE] = totalDistanceMeters
        }
        
        RoadSenseWidget().update(context, glanceId)
    }
}
