package com.vimal.roadsense.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.vimal.roadsense.ui.theme.*

/**
 * Animated shimmer brush for skeleton loading
 */
@Composable
fun shimmerBrush(
    shimmerColors: List<Color> = listOf(
        CardDark,
        CardLight.copy(alpha = 0.5f),
        CardDark
    )
): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )
    
    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 500f, translateAnim - 500f),
        end = Offset(translateAnim, translateAnim)
    )
}

/**
 * Basic skeleton box with shimmer
 */
@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(8.dp)
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(shimmerBrush())
    )
}

/**
 * Skeleton text line
 */
@Composable
fun SkeletonText(
    width: Dp = 100.dp,
    height: Dp = 16.dp,
    modifier: Modifier = Modifier
) {
    SkeletonBox(
        modifier = modifier
            .width(width)
            .height(height),
        shape = RoundedCornerShape(4.dp)
    )
}

/**
 * Skeleton circle (for avatars)
 */
@Composable
fun SkeletonCircle(
    size: Dp = 48.dp,
    modifier: Modifier = Modifier
) {
    SkeletonBox(
        modifier = modifier.size(size),
        shape = CircleShape
    )
}

/**
 * Skeleton card loader
 */
@Composable
fun SkeletonCard(
    modifier: Modifier = Modifier,
    height: Dp = 100.dp
) {
    SkeletonBox(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        shape = RoundedCornerShape(20.dp)
    )
}

/**
 * Skeleton for stat card
 */
@Composable
fun SkeletonStatCard(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(CardDark)
            .padding(20.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            SkeletonText(width = 60.dp, height = 14.dp)
            Spacer(modifier = Modifier.height(12.dp))
            SkeletonText(width = 80.dp, height = 28.dp)
        }
    }
}

/**
 * Skeleton for list item
 */
@Composable
fun SkeletonListItem(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SkeletonCircle(size = 44.dp)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            SkeletonText(width = 120.dp, height = 16.dp)
            Spacer(modifier = Modifier.height(8.dp))
            SkeletonText(width = 80.dp, height = 12.dp)
        }
        SkeletonText(width = 40.dp, height = 20.dp)
    }
}

/**
 * Skeleton for leaderboard item
 */
@Composable
fun SkeletonLeaderboardItem(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SkeletonText(width = 24.dp, height = 24.dp)
        Spacer(modifier = Modifier.width(12.dp))
        SkeletonCircle(size = 40.dp)
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            SkeletonText(width = 100.dp, height = 16.dp)
            Spacer(modifier = Modifier.height(6.dp))
            SkeletonText(width = 60.dp, height = 12.dp)
        }
        SkeletonText(width = 50.dp, height = 18.dp)
    }
}

/**
 * Skeleton for pothole detection card
 */
@Composable
fun SkeletonPotholeCard(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CardDark)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        SkeletonBox(
            modifier = Modifier.size(48.dp),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            SkeletonText(width = 150.dp, height = 14.dp)
            Spacer(modifier = Modifier.height(8.dp))
            SkeletonText(width = 100.dp, height = 12.dp)
        }
        SkeletonBox(
            modifier = Modifier
                .width(60.dp)
                .height(24.dp),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

/**
 * Skeleton loader list
 */
@Composable
fun SkeletonList(
    itemCount: Int = 5,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(itemCount) {
            SkeletonListItem()
        }
    }
}

/**
 * Skeleton for profile header
 */
@Composable
fun SkeletonProfileHeader(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(CardDark)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SkeletonCircle(size = 80.dp)
        Spacer(modifier = Modifier.height(16.dp))
        SkeletonText(width = 140.dp, height = 22.dp)
        Spacer(modifier = Modifier.height(8.dp))
        SkeletonText(width = 100.dp, height = 14.dp)
        Spacer(modifier = Modifier.height(20.dp))
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            repeat(3) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    SkeletonText(width = 40.dp, height = 24.dp)
                    Spacer(modifier = Modifier.height(4.dp))
                    SkeletonText(width = 60.dp, height = 12.dp)
                }
            }
        }
    }
}

/**
 * Skeleton for map section
 */
@Composable
fun SkeletonMapSection(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(300.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(shimmerBrush())
    )
}
