package com.vimal.roadsense.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.airbnb.lottie.compose.*

/**
 * Lottie animation URLs for different states
 * Replace these with your own animations from lottiefiles.com
 */
object LottieAnimations {
    // Onboarding animations
    const val ROAD_DETECTION = "https://assets10.lottiefiles.com/packages/lf20_vvmkxyw5.json"
    const val MAP_LOCATION = "https://assets3.lottiefiles.com/packages/lf20_UJNc2t.json"
    const val SMART_CITY = "https://assets1.lottiefiles.com/packages/lf20_zyu0ctqb.json"
    
    // State animations
    const val SUCCESS = "https://assets2.lottiefiles.com/packages/lf20_s2lryxtd.json"
    const val ERROR = "https://assets6.lottiefiles.com/packages/lf20_qpwbiyxf.json"
    const val LOADING = "https://assets1.lottiefiles.com/packages/lf20_p8bfn5to.json"
    const val EMPTY = "https://assets9.lottiefiles.com/packages/lf20_ydo1amjm.json"
    
    // Feature animations
    const val GPS = "https://assets7.lottiefiles.com/packages/lf20_svy4ivvy.json"
    const val POTHOLE = "https://assets5.lottiefiles.com/packages/lf20_e3odbwbp.json"
    const val CAR_DRIVING = "https://assets4.lottiefiles.com/packages/lf20_CTaizi.json"
    const val TROPHY = "https://assets8.lottiefiles.com/packages/lf20_touohxv0.json"
    const val CONFETTI = "https://assets2.lottiefiles.com/packages/lf20_u4yrau.json"
}

/**
 * Lottie animation composable from URL
 */
@Composable
fun LottieAnimationFromUrl(
    url: String,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    iterations: Int = LottieConstants.IterateForever,
    autoPlay: Boolean = true
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.Url(url))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = iterations,
        isPlaying = autoPlay
    )
    
    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier.size(size)
    )
}

/**
 * Lottie animation from raw resource
 */
@Composable
fun LottieAnimationFromResource(
    resId: Int,
    modifier: Modifier = Modifier,
    size: Dp = 200.dp,
    iterations: Int = LottieConstants.IterateForever,
    autoPlay: Boolean = true
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(resId))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = iterations,
        isPlaying = autoPlay
    )
    
    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier.size(size)
    )
}

/**
 * Success animation
 */
@Composable
fun SuccessAnimation(
    modifier: Modifier = Modifier,
    size: Dp = 150.dp,
    onAnimationEnd: (() -> Unit)? = null
) {
    val composition by rememberLottieComposition(LottieCompositionSpec.Url(LottieAnimations.SUCCESS))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1
    )
    
    LaunchedEffect(progress) {
        if (progress == 1f) {
            onAnimationEnd?.invoke()
        }
    }
    
    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier.size(size)
    )
}

/**
 * Loading animation
 */
@Composable
fun LoadingAnimation(
    modifier: Modifier = Modifier,
    size: Dp = 100.dp
) {
    LottieAnimationFromUrl(
        url = LottieAnimations.LOADING,
        modifier = modifier,
        size = size
    )
}

/**
 * Empty state animation
 */
@Composable
fun EmptyAnimation(
    modifier: Modifier = Modifier,
    size: Dp = 200.dp
) {
    LottieAnimationFromUrl(
        url = LottieAnimations.EMPTY,
        modifier = modifier,
        size = size
    )
}

/**
 * Trophy/Celebration animation
 */
@Composable
fun TrophyAnimation(
    modifier: Modifier = Modifier,
    size: Dp = 150.dp
) {
    LottieAnimationFromUrl(
        url = LottieAnimations.TROPHY,
        modifier = modifier,
        size = size
    )
}

/**
 * Confetti celebration animation
 */
@Composable
fun ConfettiAnimation(
    modifier: Modifier = Modifier,
    size: Dp = 300.dp,
    iterations: Int = 1
) {
    LottieAnimationFromUrl(
        url = LottieAnimations.CONFETTI,
        modifier = modifier,
        size = size,
        iterations = iterations
    )
}

/**
 * GPS/Location animation
 */
@Composable
fun LocationAnimation(
    modifier: Modifier = Modifier,
    size: Dp = 150.dp
) {
    LottieAnimationFromUrl(
        url = LottieAnimations.GPS,
        modifier = modifier,
        size = size
    )
}

/**
 * Car driving animation
 */
@Composable
fun CarDrivingAnimation(
    modifier: Modifier = Modifier,
    size: Dp = 200.dp
) {
    LottieAnimationFromUrl(
        url = LottieAnimations.CAR_DRIVING,
        modifier = modifier,
        size = size
    )
}

/**
 * Onboarding page animation
 */
@Composable
fun OnboardingAnimation(
    pageIndex: Int,
    modifier: Modifier = Modifier,
    size: Dp = 250.dp
) {
    val url = when (pageIndex) {
        0 -> LottieAnimations.ROAD_DETECTION
        1 -> LottieAnimations.MAP_LOCATION
        2 -> LottieAnimations.SMART_CITY
        else -> LottieAnimations.ROAD_DETECTION
    }
    
    LottieAnimationFromUrl(
        url = url,
        modifier = modifier,
        size = size
    )
}

/**
 * Error animation
 */
@Composable
fun ErrorAnimation(
    modifier: Modifier = Modifier,
    size: Dp = 150.dp
) {
    LottieAnimationFromUrl(
        url = LottieAnimations.ERROR,
        modifier = modifier,
        size = size,
        iterations = 1
    )
}
