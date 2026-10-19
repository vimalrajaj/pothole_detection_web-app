package com.vimal.roadsense.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vimal.roadsense.data.VehicleType
import com.vimal.roadsense.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class OnboardingPage(
    val emoji: String,
    val title: String,
    val description: String,
    val gradient: List<Color>
)

private val onboardingPages = listOf(
    OnboardingPage(
        emoji = "🛣️",
        title = "Detect Potholes",
        description = "Our AI-powered sensors detect road bumps and potholes automatically while you drive",
        gradient = listOf(AccentBlue, AccentPurple)
    ),
    OnboardingPage(
        emoji = "📍",
        title = "Map & Share",
        description = "Every pothole is mapped in real-time and shared with the community to help others",
        gradient = listOf(AccentPurple, AccentOrange)
    ),
    OnboardingPage(
        emoji = "🏙️",
        title = "Smart Cities",
        description = "Help authorities identify and fix road problems faster for safer cities",
        gradient = listOf(AccentOrange, AccentGreen)
    )
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: (name: String, phone: String, vehicle: VehicleType) -> Unit
) {
    var currentStep by remember { mutableIntStateOf(0) } // 0=welcome, 1=phone, 2=name, 3=vehicle
    var phoneNumber by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    var selectedVehicle by remember { mutableStateOf(VehicleType.BIKE) }
    var isLoading by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val gradientAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Animated gradient orbs
        AnimatedOrbs(gradientAngle)
        
        AnimatedContent(
            targetState = currentStep,
            transitionSpec = {
                fadeIn(animationSpec = tween(300)) + slideInHorizontally { it / 2 } togetherWith
                fadeOut(animationSpec = tween(300)) + slideOutHorizontally { -it / 2 }
            },
            label = "step"
        ) { step ->
            when (step) {
                0 -> WelcomeStep(
                    onGetStarted = { currentStep = 1 }
                )
                1 -> PhoneStep(
                    phoneNumber = phoneNumber,
                    onPhoneChange = { phoneNumber = it.filter { c -> c.isDigit() }.take(10) },
                    onContinue = { 
                        if (phoneNumber.length == 10) currentStep = 2 
                    },
                    onBack = { currentStep = 0 }
                )
                2 -> NameStep(
                    displayName = displayName,
                    onNameChange = { displayName = it.take(25) },
                    onContinue = { 
                        if (displayName.isNotBlank()) currentStep = 3 
                    },
                    onBack = { currentStep = 1 }
                )
                3 -> VehicleStep(
                    selectedVehicle = selectedVehicle,
                    onVehicleSelect = { selectedVehicle = it },
                    isLoading = isLoading,
                    onComplete = {
                        isLoading = true
                        scope.launch {
                            delay(500)
                            onComplete(displayName.trim(), phoneNumber, selectedVehicle)
                        }
                    },
                    onBack = { currentStep = 2 }
                )
            }
        }
        
        // Progress indicator
        if (currentStep > 0) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 60.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .width(if (index < currentStep) 24.dp else 8.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (index < currentStep) AccentBlue
                                else CardLight
                            )
                            .animateContentSize()
                    )
                }
            }
        }
    }
}

@Composable
private fun AnimatedOrbs(angle: Float) {
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .size(400.dp)
                .offset(
                    x = (-100 + kotlin.math.cos(Math.toRadians(angle.toDouble())) * 50).dp,
                    y = (-150 + kotlin.math.sin(Math.toRadians(angle.toDouble())) * 30).dp
                )
                .blur(180.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(AccentBlue.copy(alpha = 0.4f), Color.Transparent)
                    )
                )
        )
        
        Box(
            modifier = Modifier
                .size(350.dp)
                .offset(
                    x = (250 + kotlin.math.sin(Math.toRadians(angle.toDouble())) * 40).dp,
                    y = (400 + kotlin.math.cos(Math.toRadians(angle.toDouble())) * 50).dp
                )
                .blur(160.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(AccentPurple.copy(alpha = 0.35f), Color.Transparent)
                    )
                )
        )
        
        Box(
            modifier = Modifier
                .size(280.dp)
                .offset(
                    x = (100 + kotlin.math.cos(Math.toRadians(angle.toDouble() * 0.7)) * 60).dp,
                    y = (600 + kotlin.math.sin(Math.toRadians(angle.toDouble() * 0.7)) * 40).dp
                )
                .blur(140.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(AccentOrange.copy(alpha = 0.25f), Color.Transparent)
                    )
                )
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun WelcomeStep(onGetStarted: () -> Unit) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()
    
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))
        
        // App logo
        Box(
            modifier = Modifier
                .size(100.dp)
                .scale(pulse)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(AccentBlue, AccentPurple)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "🛣️",
                fontSize = 48.sp
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Road-Sense",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Text(
            text = "Smart Pothole Detection",
            fontSize = 16.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 4.dp)
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Pager
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) { page ->
            OnboardingPageContent(onboardingPages[page])
        }
        
        // Page indicators
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(onboardingPages.size) { index ->
                Box(
                    modifier = Modifier
                        .size(if (pagerState.currentPage == index) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (pagerState.currentPage == index) AccentBlue
                            else CardLight
                        )
                        .animateContentSize()
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Get Started button
        Button(
            onClick = onGetStarted,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
        ) {
            Text(
                text = "Get Started",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                Icons.Rounded.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Emoji with glow
        Box(
            modifier = Modifier.size(120.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .blur(40.dp)
                    .background(
                        brush = Brush.radialGradient(
                            colors = page.gradient + Color.Transparent
                        )
                    )
            )
            Text(
                text = page.emoji,
                fontSize = 72.sp
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = page.title,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
        
        Text(
            text = page.description,
            fontSize = 16.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp, start = 16.dp, end = 16.dp),
            lineHeight = 24.sp
        )
    }
}

@Composable
private fun PhoneStep(
    phoneNumber: String,
    onPhoneChange: (String) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    
    LaunchedEffect(Unit) {
        delay(300)
        focusRequester.requestFocus()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(100.dp))
        
        // Icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(AccentBlue.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "📱", fontSize = 40.sp)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "Enter Your Phone",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Text(
            text = "We'll use this to save your reports",
            fontSize = 16.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Phone input
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Country code
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardLight)
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Text(
                        text = "🇮🇳 +91",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // Phone number input
                BasicTextField(
                    value = phoneNumber,
                    onValueChange = onPhoneChange,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 2.sp
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            onContinue()
                        }
                    ),
                    cursorBrush = SolidColor(AccentBlue),
                    decorationBox = { innerTextField ->
                        Box {
                            if (phoneNumber.isEmpty()) {
                                Text(
                                    text = "98765 43210",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = TextSecondary.copy(alpha = 0.5f),
                                    letterSpacing = 2.sp
                                )
                            }
                            innerTextField()
                        }
                    }
                )
                
                // Checkmark when valid
                AnimatedVisibility(
                    visible = phoneNumber.length == 10,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(AccentGreen),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderColor),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
            ) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Back")
            }
            
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .weight(2f)
                    .height(56.dp),
                enabled = phoneNumber.length == 10,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentBlue,
                    disabledContainerColor = AccentBlue.copy(alpha = 0.3f)
                )
            ) {
                Text("Continue", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Rounded.ArrowForward, contentDescription = null)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun NameStep(
    displayName: String,
    onNameChange: (String) -> Unit,
    onContinue: () -> Unit,
    onBack: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    
    LaunchedEffect(Unit) {
        delay(300)
        focusRequester.requestFocus()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(100.dp))
        
        // Avatar
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(AccentPurple, AccentBlue)
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (displayName.isNotEmpty()) 
                    displayName.first().uppercase() 
                else "👤",
                fontSize = if (displayName.isNotEmpty()) 42.sp else 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Text(
            text = "What's Your Name?",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Text(
            text = "This will appear on your reports",
            fontSize = 16.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Name input
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Rounded.Person,
                    contentDescription = null,
                    tint = AccentBlue,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                BasicTextField(
                    value = displayName,
                    onValueChange = onNameChange,
                    modifier = Modifier
                        .weight(1f)
                        .focusRequester(focusRequester),
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            if (displayName.isNotBlank()) onContinue()
                        }
                    ),
                    cursorBrush = SolidColor(AccentBlue),
                    decorationBox = { innerTextField ->
                        Box {
                            if (displayName.isEmpty()) {
                                Text(
                                    text = "Enter your name",
                                    fontSize = 20.sp,
                                    color = TextSecondary.copy(alpha = 0.5f)
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderColor),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
            ) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Back")
            }
            
            Button(
                onClick = onContinue,
                modifier = Modifier
                    .weight(2f)
                    .height(56.dp),
                enabled = displayName.isNotBlank(),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentPurple,
                    disabledContainerColor = AccentPurple.copy(alpha = 0.3f)
                )
            ) {
                Text("Continue", fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Rounded.ArrowForward, contentDescription = null)
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

data class VehicleChoice(
    val type: VehicleType,
    val emoji: String,
    val name: String,
    val description: String,
    val color: Color
)

private val vehicleChoices = listOf(
    VehicleChoice(VehicleType.BIKE, "🏍️", "Motorcycle", "Scooter / Bike", AccentBlue),
    VehicleChoice(VehicleType.BICYCLE, "🚲", "Bicycle", "Cycle / E-bike", AccentGreen),
    VehicleChoice(VehicleType.CAR, "🚗", "Car", "Car / SUV", AccentPurple),
    VehicleChoice(VehicleType.AUTO, "🛺", "Auto", "Auto Rickshaw", AccentOrange),
    VehicleChoice(VehicleType.BUS, "🚌", "Bus", "Bus / Truck", AccentRed)
)

@Composable
private fun VehicleStep(
    selectedVehicle: VehicleType,
    onVehicleSelect: (VehicleType) -> Unit,
    isLoading: Boolean,
    onComplete: () -> Unit,
    onBack: () -> Unit
) {
    val selectedChoice = vehicleChoices.find { it.type == selectedVehicle } ?: vehicleChoices[0]
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(100.dp))
        
        // Selected vehicle preview
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(28.dp))
                .background(selectedChoice.color.copy(alpha = 0.15f))
                .border(2.dp, selectedChoice.color.copy(alpha = 0.5f), RoundedCornerShape(28.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = selectedChoice.emoji,
                fontSize = 52.sp
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Select Your Vehicle",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Text(
            text = "We'll optimize detection for your ride",
            fontSize = 16.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 8.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Vehicle grid
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            vehicleChoices.take(3).forEach { vehicle ->
                VehicleChip(
                    vehicle = vehicle,
                    isSelected = selectedVehicle == vehicle.type,
                    onClick = { onVehicleSelect(vehicle.type) },
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(modifier = Modifier.weight(0.5f))
            vehicleChoices.drop(3).forEach { vehicle ->
                VehicleChip(
                    vehicle = vehicle,
                    isSelected = selectedVehicle == vehicle.type,
                    onClick = { onVehicleSelect(vehicle.type) },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.weight(0.5f))
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Sensitivity info
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(selectedChoice.color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Settings,
                        contentDescription = null,
                        tint = selectedChoice.color,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Detection Sensitivity",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Text(
                        text = getSensitivityDescription(selectedVehicle),
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.weight(1f))
        
        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, BorderColor),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary)
            ) {
                Icon(Icons.Rounded.ArrowBack, contentDescription = null)
            }
            
            Button(
                onClick = onComplete,
                modifier = Modifier
                    .weight(3f)
                    .height(56.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = selectedChoice.color)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Let's Go!", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("🚀", fontSize = 18.sp)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun VehicleChip(
    vehicle: VehicleChoice,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.05f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    Card(
        modifier = modifier
            .scale(scale)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) vehicle.color.copy(alpha = 0.2f) else CardDark
        ),
        border = if (isSelected) BorderStroke(2.dp, vehicle.color) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = vehicle.emoji, fontSize = 32.sp)
            Text(
                text = vehicle.name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = if (isSelected) Color.White else TextSecondary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

private fun getSensitivityDescription(vehicle: VehicleType): String {
    return when (vehicle) {
        VehicleType.BIKE -> "High sensitivity for two-wheelers"
        VehicleType.BICYCLE -> "Maximum sensitivity for cycles"
        VehicleType.CAR -> "Balanced for four-wheelers"
        VehicleType.AUTO -> "Optimized for three-wheelers"
        VehicleType.BUS -> "Lower sensitivity for heavy vehicles"
    }
}
