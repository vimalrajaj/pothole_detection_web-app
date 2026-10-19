package com.vimal.roadsense.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vimal.roadsense.data.OtpState
import com.vimal.roadsense.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun LoginScreen(
    phoneNumber: String,
    otpCode: String,
    otpState: OtpState,
    isLoading: Boolean,
    errorMessage: String?,
    onPhoneNumberChange: (String) -> Unit,
    onOtpCodeChange: (String) -> Unit,
    onSendOtp: () -> Unit,
    onVerifyOtp: () -> Unit,
    onResendOtp: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val otpFocusRequester = remember { FocusRequester() }
    
    // Animation values
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradient"
    )
    
    // Logo pulse animation
    val logoPulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    // Auto-focus OTP field when code is sent
    LaunchedEffect(otpState) {
        if (otpState == OtpState.Sent) {
            delay(300)
            otpFocusRequester.requestFocus()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Animated gradient blobs
        Box(
            modifier = Modifier
                .size(300.dp)
                .offset(x = (-50).dp + (gradientOffset * 100).dp, y = (-100).dp)
                .blur(150.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(AccentBlue.copy(alpha = 0.4f), Color.Transparent)
                    )
                )
        )
        
        Box(
            modifier = Modifier
                .size(250.dp)
                .offset(x = 200.dp - (gradientOffset * 50).dp, y = 400.dp)
                .blur(120.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(AccentPurple.copy(alpha = 0.3f), Color.Transparent)
                    )
                )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(80.dp))
            
            // Logo section
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(logoPulse)
                    .clip(CircleShape)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(AccentBlue, AccentPurple)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🛣️",
                    fontSize = 50.sp
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Road Sense",
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
            
            Spacer(modifier = Modifier.height(60.dp))
            
            // Content based on OTP state
            AnimatedContent(
                targetState = otpState == OtpState.Sent || otpState == OtpState.Verifying,
                transitionSpec = {
                    slideInHorizontally { if (targetState) it else -it } + fadeIn() togetherWith
                            slideOutHorizontally { if (targetState) -it else it } + fadeOut()
                },
                label = "content"
            ) { showOtpInput ->
                if (showOtpInput) {
                    OtpInputSection(
                        phoneNumber = phoneNumber,
                        otpCode = otpCode,
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        focusRequester = otpFocusRequester,
                        onOtpCodeChange = onOtpCodeChange,
                        onVerifyOtp = {
                            focusManager.clearFocus()
                            onVerifyOtp()
                        },
                        onResendOtp = onResendOtp,
                        onChangeNumber = {
                            onPhoneNumberChange("")
                        }
                    )
                } else {
                    PhoneInputSection(
                        phoneNumber = phoneNumber,
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        onPhoneNumberChange = onPhoneNumberChange,
                        onSendOtp = {
                            focusManager.clearFocus()
                            onSendOtp()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun PhoneInputSection(
    phoneNumber: String,
    isLoading: Boolean,
    errorMessage: String?,
    onPhoneNumberChange: (String) -> Unit,
    onSendOtp: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Enter your phone number",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        
        Text(
            text = "We'll send you a verification code",
            fontSize = 14.sp,
            color = TextSecondary,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )
        
        // Phone input card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = AccentBlue,
                    modifier = Modifier.size(24.dp)
                )
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // Country code
                Text(
                    text = "+91",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
                
                Box(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .width(1.dp)
                        .height(24.dp)
                        .background(CardLight)
                )
                
                // Phone input
                BasicTextField(
                    value = phoneNumber,
                    onValueChange = onPhoneNumberChange,
                    modifier = Modifier.weight(1f),
                    textStyle = TextStyle(
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { if (phoneNumber.length == 10) onSendOtp() }
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(AccentBlue),
                    decorationBox = { innerTextField ->
                        Box {
                            if (phoneNumber.isEmpty()) {
                                Text(
                                    text = "Phone number",
                                    color = TextSecondary,
                                    fontSize = 18.sp
                                )
                            }
                            innerTextField()
                        }
                    }
                )
            }
        }
        
        // Error message
        AnimatedVisibility(
            visible = errorMessage != null,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            Text(
                text = errorMessage ?: "",
                color = AccentRed,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 12.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Send OTP button
        Button(
            onClick = onSendOtp,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = phoneNumber.length == 10 && !isLoading,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentBlue,
                disabledContainerColor = AccentBlue.copy(alpha = 0.4f)
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Send OTP",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "By continuing, you agree to our Terms of Service\nand Privacy Policy",
            fontSize = 12.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 18.sp
        )
    }
}

@Composable
private fun OtpInputSection(
    phoneNumber: String,
    otpCode: String,
    isLoading: Boolean,
    errorMessage: String?,
    focusRequester: FocusRequester,
    onOtpCodeChange: (String) -> Unit,
    onVerifyOtp: () -> Unit,
    onResendOtp: () -> Unit,
    onChangeNumber: () -> Unit
) {
    // Timer for resend
    var timeRemaining by remember { mutableIntStateOf(60) }
    var canResend by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        while (timeRemaining > 0) {
            delay(1000)
            timeRemaining--
        }
        canResend = true
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = AccentGreen,
            modifier = Modifier.size(48.dp)
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Verify OTP",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Text(
            text = "Enter the 6-digit code sent to\n+91 $phoneNumber",
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )
        
        // OTP boxes
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            repeat(6) { index ->
                val char = otpCode.getOrNull(index)?.toString() ?: ""
                val isFocused = otpCode.length == index
                
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CardDark)
                        .then(
                            if (isFocused) {
                                Modifier.border(2.dp, AccentBlue, RoundedCornerShape(12.dp))
                            } else if (char.isNotEmpty()) {
                                Modifier.border(2.dp, AccentGreen, RoundedCornerShape(12.dp))
                            } else {
                                Modifier
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = char,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
        
        // Hidden text field for input
        BasicTextField(
            value = otpCode,
            onValueChange = { 
                onOtpCodeChange(it)
                if (it.length == 6) onVerifyOtp()
            },
            modifier = Modifier
                .size(1.dp)
                .focusRequester(focusRequester),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { if (otpCode.length == 6) onVerifyOtp() }
            )
        )
        
        // Error message
        AnimatedVisibility(
            visible = errorMessage != null,
            enter = fadeIn() + slideInVertically(),
            exit = fadeOut() + slideOutVertically()
        ) {
            Text(
                text = errorMessage ?: "",
                color = AccentRed,
                fontSize = 14.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Verify button
        Button(
            onClick = onVerifyOtp,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = otpCode.length == 6 && !isLoading,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentGreen,
                disabledContainerColor = AccentGreen.copy(alpha = 0.4f)
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "Verify & Continue",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Resend option
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Didn't receive code? ",
                color = TextSecondary,
                fontSize = 14.sp
            )
            
            if (canResend) {
                TextButton(onClick = {
                    onResendOtp()
                    timeRemaining = 60
                    canResend = false
                }) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = AccentBlue
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Resend",
                        color = AccentBlue,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Text(
                    text = "Resend in ${timeRemaining}s",
                    color = AccentBlue,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TextButton(onClick = onChangeNumber) {
            Text(
                text = "Change phone number",
                color = TextSecondary,
                fontSize = 14.sp
            )
        }
    }
}
