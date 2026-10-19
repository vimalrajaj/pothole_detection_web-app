package com.vimal.roadsense.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vimal.roadsense.data.PotholeData
import com.vimal.roadsense.data.ReportStatus
import com.vimal.roadsense.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * Share pothole location via various apps
 */
@Composable
fun SharePotholeBottomSheet(
    pothole: PotholeData,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val dateFormat = remember { SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(CardDark, DarkBackground)
                ),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .padding(24.dp)
    ) {
        // Handle
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(40.dp)
                .height(4.dp)
                .background(TextSecondary.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Title
        Text(
            "📤 Share Pothole Location",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            "Share this pothole location to help others avoid it",
            fontSize = 14.sp,
            color = TextSecondary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Pothole info card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardLight)
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
                        .background(
                            color = when (pothole.severity) {
                                3 -> AccentRed.copy(alpha = 0.2f)
                                2 -> AccentOrange.copy(alpha = 0.2f)
                                else -> AccentYellow.copy(alpha = 0.2f)
                            },
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Warning,
                        contentDescription = null,
                        tint = when (pothole.severity) {
                            3 -> AccentRed
                            2 -> AccentOrange
                            else -> AccentYellow
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "${pothole.severityLabel} Severity Pothole",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "📍 ${String.format("%.6f", pothole.latitude)}, ${String.format("%.6f", pothole.longitude)}",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Share options
        Text(
            "Share via",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondary
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            ShareOption(
                icon = Icons.Filled.Chat,
                label = "WhatsApp",
                color = Color(0xFF25D366),
                onClick = {
                    shareViaWhatsApp(context, pothole)
                    onDismiss()
                }
            )
            
            ShareOption(
                icon = Icons.Filled.Textsms,
                label = "SMS",
                color = AccentBlue,
                onClick = {
                    shareViaSMS(context, pothole)
                    onDismiss()
                }
            )
            
            ShareOption(
                icon = Icons.Filled.ContentCopy,
                label = "Copy",
                color = AccentPurple,
                onClick = {
                    copyToClipboard(context, pothole)
                    onDismiss()
                }
            )
            
            ShareOption(
                icon = Icons.Filled.Share,
                label = "More",
                color = AccentOrange,
                onClick = {
                    shareGeneric(context, pothole)
                    onDismiss()
                }
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Cancel button
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Cancel", color = TextSecondary)
        }
    }
}

@Composable
fun ShareOption(
    icon: ImageVector,
    label: String,
    color: Color,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(color.copy(alpha = 0.15f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            label,
            fontSize = 12.sp,
            color = Color.White
        )
    }
}

// Share functions
private fun shareViaWhatsApp(context: android.content.Context, pothole: PotholeData) {
    val message = buildShareMessage(pothole)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        `package` = "com.whatsapp"
        putExtra(Intent.EXTRA_TEXT, message)
    }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
    }
}

private fun shareViaSMS(context: android.content.Context, pothole: PotholeData) {
    val message = buildShareMessage(pothole)
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = android.net.Uri.parse("smsto:")
        putExtra("sms_body", message)
    }
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "SMS app not found", Toast.LENGTH_SHORT).show()
    }
}

private fun copyToClipboard(context: android.content.Context, pothole: PotholeData) {
    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    val clip = android.content.ClipData.newPlainText("Pothole Location", buildShareMessage(pothole))
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "Location copied to clipboard!", Toast.LENGTH_SHORT).show()
}

private fun shareGeneric(context: android.content.Context, pothole: PotholeData) {
    val message = buildShareMessage(pothole)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)
        putExtra(Intent.EXTRA_SUBJECT, "Pothole Alert - RoadSense")
    }
    context.startActivity(Intent.createChooser(intent, "Share via"))
}

private fun buildShareMessage(pothole: PotholeData): String {
    val mapUrl = "https://www.openstreetmap.org/?mlat=${pothole.latitude}&mlon=${pothole.longitude}#map=18/${pothole.latitude}/${pothole.longitude}"
    val googleMapUrl = "https://www.google.com/maps?q=${pothole.latitude},${pothole.longitude}"
    
    return """
⚠️ *Pothole Alert!*
        
🚧 ${pothole.severityLabel} severity pothole detected
        
📍 *Location:*
Lat: ${String.format("%.6f", pothole.latitude)}
Lng: ${String.format("%.6f", pothole.longitude)}

🗺️ View on Map:
$googleMapUrl

_Reported via RoadSense App_ 🛣️
    """.trimIndent()
}

/**
 * Report to Municipality bottom sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportToMunicipalitySheet(
    pothole: PotholeData,
    onDismiss: () -> Unit,
    onSubmit: (description: String) -> Unit
) {
    val context = LocalContext.current
    var description by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var showSuccess by remember { mutableStateOf(false) }
    
    if (showSuccess) {
        ReportSuccessScreen(onDismiss = onDismiss)
        return
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(CardDark, DarkBackground)
                ),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .padding(24.dp)
    ) {
        // Handle
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .width(40.dp)
                .height(4.dp)
                .background(TextSecondary.copy(alpha = 0.5f), RoundedCornerShape(2.dp))
        )
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Title with icon
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(AccentBlue.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Security,
                    contentDescription = null,
                    tint = AccentBlue,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column {
                Text(
                    "Report to Municipality",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "Help improve road conditions",
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Pothole details card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardLight)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.LocationOn,
                        contentDescription = null,
                        tint = AccentRed,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Location Details",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    "Coordinates: ${String.format("%.6f", pothole.latitude)}, ${String.format("%.6f", pothole.longitude)}",
                    fontSize = 13.sp,
                    color = TextSecondary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row {
                    SeverityChip(severity = pothole.severity)
                    Spacer(modifier = Modifier.width(8.dp))
                    Chip(
                        icon = Icons.Filled.Speed,
                        text = "${pothole.speed.toInt()} km/h",
                        color = AccentBlue
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Description input
        Text(
            "Additional Details (Optional)",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            placeholder = { 
                Text(
                    "E.g., Near bus stop, very deep, causing accidents...",
                    color = TextSecondary.copy(alpha = 0.5f)
                ) 
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AccentBlue,
                unfocusedBorderColor = BorderColor,
                cursorColor = AccentBlue,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp)
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Submit button
        Button(
            onClick = {
                isSubmitting = true
                onSubmit(description)
                // Simulate API call
                android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                    isSubmitting = false
                    showSuccess = true
                }, 1500)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isSubmitting,
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentGreen
            )
        ) {
            if (isSubmitting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    Icons.Filled.Send,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Submit Report",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Info text
        Text(
            "Your report will be sent to the local road department for action.",
            fontSize = 12.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Cancel button
        TextButton(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("Cancel", color = TextSecondary)
        }
    }
}

@Composable
fun ReportSuccessScreen(onDismiss: () -> Unit) {
    val scale = remember { Animatable(0f) }
    
    LaunchedEffect(Unit) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(CardDark, DarkBackground)
                ),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            )
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        
        // Success animation
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(
                    AccentGreen.copy(alpha = 0.15f),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "✅",
                fontSize = 48.sp,
                modifier = Modifier.scale(scale.value)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            "Report Submitted!",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            "Thank you for making our roads safer!\nThe municipality has been notified.",
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onDismiss,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = AccentBlue
            )
        ) {
            Text(
                "Done",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
fun SeverityChip(severity: Int) {
    val (text, color) = when (severity) {
        3 -> "High" to AccentRed
        2 -> "Medium" to AccentOrange
        else -> "Low" to AccentYellow
    }
    
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
fun Chip(
    icon: ImageVector,
    text: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}
