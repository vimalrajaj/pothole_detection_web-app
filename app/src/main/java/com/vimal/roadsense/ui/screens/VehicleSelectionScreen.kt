package com.vimal.roadsense.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vimal.roadsense.data.VehicleType
import com.vimal.roadsense.ui.theme.*

data class VehicleOption(
    val type: VehicleType,
    val emoji: String,
    val description: String,
    val color: Color
)

private val vehicleOptions = listOf(
    VehicleOption(VehicleType.BIKE, "🏍️", "Motorcycle / Scooter", AccentBlue),
    VehicleOption(VehicleType.BICYCLE, "🚲", "Bicycle / E-bike", AccentGreen),
    VehicleOption(VehicleType.CAR, "🚗", "Car / SUV", AccentPurple),
    VehicleOption(VehicleType.AUTO, "🛺", "Auto Rickshaw", AccentOrange),
    VehicleOption(VehicleType.BUS, "🚌", "Bus / Truck", AccentRed)
)

@Composable
fun VehicleSelectionScreen(
    selectedVehicle: VehicleType,
    isLoading: Boolean,
    onVehicleSelect: (VehicleType) -> Unit,
    onContinue: (String) -> Unit
) {
    var displayName by remember { mutableStateOf("") }
    var showNameInput by remember { mutableStateOf(false) }
    
    val infiniteTransition = rememberInfiniteTransition(label = "bg")
    val gradientOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "gradient"
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Animated gradient blobs
        Box(
            modifier = Modifier
                .size(350.dp)
                .offset(x = 150.dp - (gradientOffset * 80).dp, y = (-80).dp)
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
                .offset(x = (-100).dp + (gradientOffset * 60).dp, y = 500.dp)
                .blur(140.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(AccentOrange.copy(alpha = 0.25f), Color.Transparent)
                    )
                )
        )
        
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            
            // Header
            Text(
                text = "Select Your Vehicle",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Text(
                text = "We'll calibrate detection for your ride",
                fontSize = 16.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
            )
            
            // Vehicle grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(vehicleOptions) { option ->
                    VehicleCard(
                        option = option,
                        isSelected = selectedVehicle == option.type,
                        onClick = { onVehicleSelect(option.type) }
                    )
                }
            }
            
            // Calibration info card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp),
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
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(
                                vehicleOptions.find { it.type == selectedVehicle }?.color?.copy(alpha = 0.2f)
                                    ?: AccentBlue.copy(alpha = 0.2f)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = vehicleOptions.find { it.type == selectedVehicle }?.emoji ?: "🏍️",
                            fontSize = 24.sp
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
                            text = getCalibrationDescription(selectedVehicle),
                            fontSize = 12.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }
            }
            
            // Name input section
            AnimatedVisibility(
                visible = showNameInput,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "What should we call you?",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(CardLight)
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = AccentBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            BasicTextField(
                                value = displayName,
                                onValueChange = { displayName = it.take(30) },
                                modifier = Modifier.weight(1f),
                                textStyle = TextStyle(
                                    color = Color.White,
                                    fontSize = 16.sp
                                ),
                                singleLine = true,
                                cursorBrush = SolidColor(AccentBlue),
                                decorationBox = { innerTextField ->
                                    Box {
                                        if (displayName.isEmpty()) {
                                            Text(
                                                text = "Enter your name",
                                                color = TextSecondary,
                                                fontSize = 16.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                }
                            )
                        }
                    }
                }
            }
            
            // Continue button
            Button(
                onClick = {
                    if (showNameInput && displayName.isNotBlank()) {
                        onContinue(displayName.trim())
                    } else if (!showNameInput) {
                        showNameInput = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = !isLoading && (!showNameInput || displayName.isNotBlank()),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = vehicleOptions.find { it.type == selectedVehicle }?.color ?: AccentBlue,
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
                        text = if (showNameInput) "Start Detecting" else "Continue",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun VehicleCard(
    option: VehicleOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = spring(dampingRatio = 0.6f),
        label = "scale"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.9f)
            .scale(scale)
            .then(
                if (isSelected) {
                    Modifier.border(2.dp, option.color, RoundedCornerShape(20.dp))
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) option.color.copy(alpha = 0.15f) else CardDark
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // Selection indicator
            androidx.compose.animation.AnimatedVisibility(
                visible = isSelected,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(option.color),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Emoji with glow effect when selected
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) {
                                Brush.radialGradient(
                                    colors = listOf(
                                        option.color.copy(alpha = 0.3f),
                                        Color.Transparent
                                    )
                                )
                            } else {
                                Brush.radialGradient(
                                    colors = listOf(CardLight, Color.Transparent)
                                )
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option.emoji,
                        fontSize = 42.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = option.type.displayName,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) option.color else Color.White
                )
                
                Text(
                    text = option.description,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

private fun getCalibrationDescription(vehicleType: VehicleType): String {
    return when (vehicleType) {
        VehicleType.BIKE -> "High sensitivity for two-wheelers. Detects smaller bumps and potholes."
        VehicleType.BICYCLE -> "Maximum sensitivity for bicycles. Even minor road irregularities detected."
        VehicleType.CAR -> "Moderate sensitivity for cars. Focuses on significant potholes."
        VehicleType.AUTO -> "Balanced detection for auto-rickshaws. Filters out normal vibrations."
        VehicleType.BUS -> "Lower sensitivity for heavy vehicles. Only major potholes detected."
    }
}
