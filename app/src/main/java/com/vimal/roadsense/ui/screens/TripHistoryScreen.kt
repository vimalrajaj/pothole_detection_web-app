package com.vimal.roadsense.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vimal.roadsense.data.TripData
import com.vimal.roadsense.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripHistoryScreen(
    trips: List<TripData>,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    // Calculate total stats
    val totalTrips = trips.size
    val totalPotholes = trips.sumOf { it.potholesFound }
    val totalDistance = trips.sumOf { it.distance.toDouble() }.toFloat()
    val totalDuration = trips.sumOf { it.duration }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Trip History",
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
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Summary Stats Card
            TripSummaryCard(
                totalTrips = totalTrips,
                totalPotholes = totalPotholes,
                totalDistance = totalDistance,
                totalDuration = totalDuration
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Trip List Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Recent Trips",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Text(
                    "${trips.size} trips",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (trips.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "🚗",
                            fontSize = 64.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No trips yet",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Start detecting potholes to record your trips",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Trip List
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(trips.sortedByDescending { it.startTime }) { index, trip ->
                        TripCard(
                            trip = trip,
                            index = index
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TripSummaryCard(
    totalTrips: Int,
    totalPotholes: Int,
    totalDistance: Float,
    totalDuration: Long
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            AccentBlue.copy(alpha = 0.3f),
                            AccentPurple.copy(alpha = 0.2f)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(AccentBlue.copy(alpha = 0.5f), AccentPurple.copy(alpha = 0.3f))
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(20.dp)
        ) {
            Column {
                Text(
                    "📊 All-Time Stats",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SummaryStatItem(
                        icon = Icons.Filled.History,
                        value = "$totalTrips",
                        label = "Trips",
                        color = AccentBlue
                    )
                    SummaryStatItem(
                        icon = Icons.Filled.Warning,
                        value = "$totalPotholes",
                        label = "Potholes",
                        color = AccentOrange
                    )
                    SummaryStatItem(
                        icon = Icons.Filled.Place,
                        value = formatDistance(totalDistance),
                        label = "Distance",
                        color = AccentGreen
                    )
                    SummaryStatItem(
                        icon = Icons.Filled.Schedule,
                        value = formatDuration(totalDuration),
                        label = "Time",
                        color = AccentPurple
                    )
                }
            }
        }
    }
}

@Composable
fun SummaryStatItem(
    icon: ImageVector,
    value: String,
    label: String,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color.copy(alpha = 0.2f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            label,
            fontSize = 12.sp,
            color = TextSecondary
        )
    }
}

@Composable
fun TripCard(
    trip: TripData,
    index: Int
) {
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault()) }
    
    // Animation
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(index * 50L)
        visible = true
    }
    
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInHorizontally { -50 }
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Trip number badge
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .background(AccentBlue.copy(alpha = 0.2f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "${index + 1}",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentBlue
                            )
                        }
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Column {
                            Text(
                                dateFormat.format(Date(trip.startTime)),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                            Text(
                                trip.vehicleType,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                    }
                    
                    // Pothole count badge
                    if (trip.potholesFound > 0) {
                        Box(
                            modifier = Modifier
                                .background(
                                    AccentOrange.copy(alpha = 0.2f),
                                    RoundedCornerShape(12.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.Warning,
                                    contentDescription = null,
                                    tint = AccentOrange,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    "${trip.potholesFound}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = AccentOrange
                                )
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Trip Stats Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    TripStatChip(
                        icon = Icons.Filled.Schedule,
                        value = trip.durationFormatted,
                        color = AccentPurple
                    )
                    TripStatChip(
                        icon = Icons.Filled.Place,
                        value = trip.distanceFormatted,
                        color = AccentGreen
                    )
                    TripStatChip(
                        icon = Icons.Filled.Speed,
                        value = "${trip.averageSpeed.toInt()} km/h",
                        color = AccentBlue
                    )
                }
                
                // View on map button
                if (trip.startLatitude != 0.0 && trip.startLongitude != 0.0) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedButton(
                        onClick = {
                            val uri = Uri.parse("geo:${trip.startLatitude},${trip.startLongitude}?q=${trip.startLatitude},${trip.startLongitude}(Trip Start)")
                            val intent = Intent(Intent.ACTION_VIEW, uri)
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = AccentBlue
                        ),
                        border = BorderStroke(1.dp, AccentBlue.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            Icons.Filled.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("View on Map")
                    }
                }
            }
        }
    }
}

@Composable
fun TripStatChip(
    icon: ImageVector,
    value: String,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(color.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            value,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

// Helper functions
fun formatDistance(km: Float): String = when {
    km >= 1000 -> String.format("%.0fk km", km / 1000)
    km >= 1 -> String.format("%.1f km", km)
    else -> String.format("%.0f m", km * 1000)
}

fun formatDuration(ms: Long): String {
    val hours = ms / (1000 * 60 * 60)
    val minutes = (ms / (1000 * 60)) % 60
    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        else -> "${minutes}m"
    }
}
