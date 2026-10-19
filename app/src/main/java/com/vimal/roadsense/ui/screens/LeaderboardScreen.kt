package com.vimal.roadsense.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vimal.roadsense.data.LeaderboardEntry
import com.vimal.roadsense.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    leaderboard: List<LeaderboardEntry>,
    currentUserId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("All Time", "This Week", "Today")
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "🏆 Leaderboard",
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
            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = CardDark,
                contentColor = AccentBlue,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = {
                            Text(
                                title,
                                fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Top 3 Podium
            if (leaderboard.size >= 3) {
                TopThreePodium(
                    first = leaderboard[0],
                    second = leaderboard[1],
                    third = leaderboard[2],
                    currentUserId = currentUserId
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Rest of leaderboard
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(leaderboard.drop(3)) { index, entry ->
                    LeaderboardCard(
                        entry = entry.copy(rank = index + 4),
                        isCurrentUser = entry.userId == currentUserId,
                        index = index
                    )
                }
                
                // Empty state padding
                if (leaderboard.size <= 3) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("🎯", fontSize = 48.sp)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    "Keep reporting potholes\nto climb the leaderboard!",
                                    fontSize = 14.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TopThreePodium(
    first: LeaderboardEntry,
    second: LeaderboardEntry,
    third: LeaderboardEntry,
    currentUserId: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        // Second Place
        PodiumItem(
            entry = second,
            rank = 2,
            height = 100,
            color = Color(0xFFC0C0C0), // Silver
            isCurrentUser = second.userId == currentUserId
        )
        
        // First Place
        PodiumItem(
            entry = first,
            rank = 1,
            height = 140,
            color = Color(0xFFFFD700), // Gold
            isCurrentUser = first.userId == currentUserId
        )
        
        // Third Place
        PodiumItem(
            entry = third,
            rank = 3,
            height = 80,
            color = Color(0xFFCD7F32), // Bronze
            isCurrentUser = third.userId == currentUserId
        )
    }
}

@Composable
fun PodiumItem(
    entry: LeaderboardEntry,
    rank: Int,
    height: Int,
    color: Color,
    isCurrentUser: Boolean
) {
    val infiniteTransition = rememberInfiniteTransition(label = "podium")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (rank == 1) 1.05f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(100.dp)
            .scale(if (rank == 1) scale else 1f)
    ) {
        // Medal/Crown
        Text(
            text = when (rank) {
                1 -> "👑"
                2 -> "🥈"
                3 -> "🥉"
                else -> ""
            },
            fontSize = if (rank == 1) 32.sp else 24.sp
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        // Avatar
        Box(
            modifier = Modifier
                .size(if (rank == 1) 64.dp else 52.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = if (isCurrentUser) {
                            listOf(AccentBlue, AccentPurple)
                        } else {
                            listOf(color.copy(alpha = 0.3f), color.copy(alpha = 0.5f))
                        }
                    ),
                    shape = CircleShape
                )
                .border(
                    width = 3.dp,
                    color = color,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                entry.displayName.take(2).uppercase(),
                fontSize = if (rank == 1) 22.sp else 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Name
        Text(
            entry.displayName.split(" ").first(),
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = if (isCurrentUser) AccentBlue else Color.White,
            maxLines = 1
        )
        
        // Count
        Text(
            "${entry.totalPotholes} 🕳️",
            fontSize = 12.sp,
            color = TextSecondary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Podium stand
        Box(
            modifier = Modifier
                .width(80.dp)
                .height(height.dp)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(color, color.copy(alpha = 0.5f))
                    ),
                    shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                ),
            contentAlignment = Alignment.TopCenter
        ) {
            Text(
                "#$rank",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun LeaderboardCard(
    entry: LeaderboardEntry,
    isCurrentUser: Boolean,
    index: Int
) {
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
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (isCurrentUser) {
                        Modifier.border(
                            width = 2.dp,
                            brush = Brush.linearGradient(
                                colors = listOf(AccentBlue, AccentPurple)
                            ),
                            shape = RoundedCornerShape(16.dp)
                        )
                    } else Modifier
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isCurrentUser) AccentBlue.copy(alpha = 0.15f) else CardDark
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rank
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = when {
                                entry.rank <= 10 -> AccentGreen.copy(alpha = 0.2f)
                                entry.rank <= 50 -> AccentYellow.copy(alpha = 0.2f)
                                else -> CardLight
                            },
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "#${entry.rank}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            entry.rank <= 10 -> AccentGreen
                            entry.rank <= 50 -> AccentYellow
                            else -> TextSecondary
                        }
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Avatar
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(AccentBlue.copy(alpha = 0.3f), AccentPurple.copy(alpha = 0.3f))
                            ),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        entry.displayName.take(2).uppercase(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Name and stats
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            entry.displayName,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                        if (isCurrentUser) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(AccentBlue, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    "YOU",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Row {
                        Text(
                            "${entry.totalTrips} trips",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            " • ",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Text(
                            formatDistance(entry.totalDistance),
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
                
                // Pothole count
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        "${entry.totalPotholes}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentOrange
                    )
                    Text(
                        "potholes",
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
