package com.example.trackmybus.core.ui.driverdashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trackmybus.R
import com.example.trackmybus.ui.theme.BrandBlue
import com.example.trackmybus.ui.theme.TextPrimary
import com.example.trackmybus.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- Custom Colors ---
val HeaderBlue = BrandBlue
val ActiveGreen = Color(0xFF43A047)
val ActiveGreenBg = Color(0xFFE8F5E9)
val StopRed = Color(0xFFE53935)
val WarningYellow = Color(0xFFFFF3E0)
val WarningText = Color(0xFFE65100)

@Composable
fun DriverDashboardScreen(
    onBackClick: () -> Unit
) {
    // --- State Management ---
    var isTracking by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var selectedBus by remember { mutableStateOf<String?>(null) }

    // Simulating a connection error state for future logic
    var connectionMessage by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    // Mock Data
    val busList = listOf("Bus 24", "Bus 12", "Bus 45")
    val currentRoute = "Kolhapur Central to Malkapur"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F7FA))
    ) {
        // 1. Blue Header Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp) // Slightly shorter since stats are gone
                .background(
                    color = HeaderBlue,
                    shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
                )
        )

        // 2. Scrollable Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // -- Custom Top Bar --
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 48.dp, start = 16.dp, end = 16.dp, bottom = 24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Text(
                    text = "Driver Dashboard",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

            // -- Welcome Text --
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = "Welcome ,",
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 16.sp
                )
                Text(
                    text = "Ready to start?",
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // -- MAIN CARD --
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    // Header Row: Bus Selector & Status Pill
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            // FIXED DROPDOWN COMPONENT
                            BusDropdownSimple(
                                selectedBus = selectedBus,
                                busOptions = busList,
                                isEnabled = !isTracking && !isLoading,
                                onBusSelected = { selectedBus = it }
                            )

                            // Route Text
                            Text(
                                text = if (selectedBus != null) currentRoute else "Select a bus to continue",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                modifier = Modifier.padding(top = 4.dp),
                                maxLines = 1
                            )
                        }

                        if (selectedBus != null) {
                            StatusPill(isActive = isTracking)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Bus Image
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFFE3F2FD)), // Light Blue placeholder
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.driver_passanger_image),
                            contentDescription = "Bus",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 12.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Status Logic Display
                    if (isTracking) {
                        StatusRowItem(Icons.Default.LocationOn, "GPS Signal Acquired", ActiveGreen)
                        Spacer(modifier = Modifier.height(8.dp))
                        StatusRowItem(Icons.Default.CheckCircle, "Server Connected", ActiveGreen)
                    } else if (isLoading) {
                        StatusRowItem(Icons.Default.LocationOn, "Acquiring GPS...", Color.Gray)
                        Spacer(modifier = Modifier.height(8.dp))
                        StatusRowItem(Icons.Default.CheckCircle, "Connecting to Server...", Color.Gray)
                    } else {
                        // Empty state or instruction
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Please ensure GPS is enabled", fontSize = 12.sp, color = Color.Gray)
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ACTION BUTTON
                    val buttonText = if (isTracking) "STOP TRACKING" else "START TRACKING"
                    val buttonColor = if (isTracking) StopRed else BrandBlue
                    val buttonEnabled = (isTracking || selectedBus != null) && !isLoading

                    Button(
                        onClick = {
                            coroutineScope.launch {
                                isLoading = true
                                connectionMessage = null // Clear old messages

                                // SIMULATE API/GPS CONNECTION DELAY
                                delay(2000)

                                // Logic: If simulating success
                                isTracking = !isTracking

                                // FUTURE LOGIC: If connection fails, set connectionMessage = "GPS Failed"

                                isLoading = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = buttonColor,
                            disabledContainerColor = if (isLoading) buttonColor.copy(alpha = 0.7f) else Color(0xFFE0E0E0)
                        ),
                        enabled = buttonEnabled
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = if (isTracking) "STOPPING..." else "CONNECTING...",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        } else {
                            Text(
                                text = buttonText,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (buttonEnabled) Color.White else Color.Gray
                            )
                        }
                    }
                }
            }

            // --- WARNING / CONNECTION MESSAGE AREA ---
            // This is where your future "Poping some messages" will go
            AnimatedVisibility(visible = connectionMessage != null || (!isTracking && selectedBus != null && !isLoading)) {

                // Example message logic: If bus selected but not tracking, show reminder.
                // Or if connectionMessage is not null, show error.

                val message = connectionMessage ?: "Ready to start duty?"
                val isError = connectionMessage != null

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                        .background(
                            if (isError) WarningYellow else Color.White,
                            RoundedCornerShape(12.dp)
                        )
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if(isError) {
                            Icon(Icons.Default.Warning, null, tint = WarningText, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = message,
                            color = if(isError) WarningText else TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

// --- FIXED DROPDOWN COMPONENT ---
// Re-written to avoid the "White Box" glitch
@Composable
fun BusDropdownSimple(
    selectedBus: String?,
    busOptions: List<String>,
    isEnabled: Boolean,
    onBusSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .clickable(enabled = isEnabled) { expanded = true }
                .padding(vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = selectedBus ?: "Select Bus No",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary // Keeps the main label visible
            )
            if (isEnabled) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Select",
                    modifier = Modifier
                        .padding(start = 4.dp)
                        .size(24.dp),
                    tint = TextPrimary
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Locked",
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(18.dp),
                    tint = Color.Gray
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier
                .background(Color.White) // Ensures background is white
                .width(200.dp)
        ) {
            busOptions.forEach { bus ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = bus,
                            color = Color.Black // <--- FORCE TEXT TO BE BLACK
                        )
                    },
                    onClick = {
                        onBusSelected(bus)
                        expanded = false
                    }
                )
            }
        }
    }
}

// --- OTHER COMPONENTS ---

@Composable
fun StatusPill(isActive: Boolean) {
    val bg = if (isActive) ActiveGreenBg else Color(0xFFF5F5F5)
    val contentColor = if (isActive) ActiveGreen else Color.Gray
    val text = if (isActive) "ACTIVE" else "INACTIVE"
    val icon = if (isActive) Icons.Default.CheckCircle else Icons.Default.Lock

    Row(
        modifier = Modifier
            .background(bg, RoundedCornerShape(50))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = contentColor, modifier = Modifier.size(14.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, color = contentColor, fontWeight = FontWeight.Bold, fontSize = 11.sp)
    }
}

@Composable
fun StatusRowItem(icon: ImageVector, text: String, tint: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF8F9FA), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, fontSize = 14.sp, color = Color.Black.copy(alpha = 0.7f))
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewDriverDashboardV4() {
    DriverDashboardScreen(onBackClick = {})
}