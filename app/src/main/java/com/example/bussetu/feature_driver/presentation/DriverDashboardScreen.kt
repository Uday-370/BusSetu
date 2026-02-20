package com.example.bussetu.feature_driver.presentation

import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AltRoute
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.bussetu.core.ui.theme.BrandBlue
import com.example.bussetu.core.ui.theme.TextPrimary
import com.example.bussetu.core.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- Palette ---
val IdleBlue = BrandBlue
val ActiveGreen = Color(0xFF059669) // Emerald 600
val ErrorRed = Color(0xFFDC2626)    // Red 600
val WarningOrange = Color(0xFFD97706) // Amber 600
val SurfaceColor = Color(0xFFF8FAFC)

enum class DashboardState {
    IDLE, CONNECTING, ACTIVE, ERROR
}

@Composable
fun DriverDashboardScreen(
    onBackClick: () -> Unit
) {
    // 1. Status Bar Logic: Force White Icons
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // False = Dark Background (so system paints White icons)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    // --- State ---
    var currentState by remember { mutableStateOf(DashboardState.IDLE) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showExitDialog by remember { mutableStateOf(false) }

    var selectedBus by remember { mutableStateOf<String?>(null) }
    var selectedRoute by remember { mutableStateOf<String?>(null) }

    val coroutineScope = rememberCoroutineScope()

    val headerColor by animateColorAsState(
        targetValue = when (currentState) {
            DashboardState.IDLE -> IdleBlue
            DashboardState.CONNECTING -> WarningOrange
            DashboardState.ACTIVE -> ActiveGreen
            DashboardState.ERROR -> ErrorRed
        },
        label = "HeaderColor"
    )

    val areInputsLocked = currentState == DashboardState.CONNECTING || currentState == DashboardState.ACTIVE

    // --- Mock Data ---
    val busList = listOf("MH-12-FC-2244", "MH-09-CV-9988", "MH-14-GH-1122")
    val routeList = listOf("R-101: Central -> Airport", "R-202: City -> Suburbs")

    Scaffold(
        containerColor = SurfaceColor,
        bottomBar = {
            BottomActionBar(
                state = currentState,
                onAction = {
                    coroutineScope.launch {
                        when (currentState) {
                            DashboardState.IDLE, DashboardState.ERROR -> {
                                if (selectedBus == null || selectedRoute == null) {
                                    currentState = DashboardState.ERROR
                                    errorMessage = "⚠️ Select Bus & Route first"
                                    return@launch
                                }
                                currentState = DashboardState.CONNECTING
                                errorMessage = null
                                delay(2000)
                                if (Math.random() < 0.2) {
                                    currentState = DashboardState.ERROR
                                    errorMessage = "❌ GPS Connection Failed"
                                } else {
                                    currentState = DashboardState.ACTIVE
                                }
                            }
                            DashboardState.ACTIVE -> {
                                currentState = DashboardState.CONNECTING
                                delay(1000)
                                currentState = DashboardState.IDLE
                                errorMessage = null
                            }
                            else -> {}
                        }
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                // FIX: Only apply Bottom Padding. Ignore Top so Header fills status bar.
                .padding(bottom = paddingValues.calculateBottomPadding())
                .verticalScroll(rememberScrollState())
        ) {
            // --- 1. STATUS HEADER ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(350.dp)
                    .clip(RoundedCornerShape(bottomStart = 48.dp, bottomEnd = 48.dp))
                    .background(headerColor)
            ) {
                // Background Pattern
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(x = 50.dp, y = (-50).dp)
                        .size(300.dp)
                        .background(Color.White.copy(alpha = 0.05f), CircleShape)
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding() // FIX: Pushes content down so it doesn't overlap Battery Icon
                        .padding(top = 12.dp, start = 20.dp, end = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Navbar
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (currentState == DashboardState.ACTIVE) {
                                    showExitDialog = true
                                } else {
                                    onBackClick()
                                }
                            },
                            modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                        }
                        Spacer(modifier = Modifier.weight(1f))

                        Box(
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(50))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if(currentState == DashboardState.ACTIVE) "● LIVE" else "OFFLINE",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Pro Orb
                    ProStatusOrb(state = currentState)

                    Spacer(modifier = Modifier.height(24.dp))

                    // Text
                    AnimatedContent(targetState = currentState, label = "text") { state ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = when (state) {
                                    DashboardState.IDLE -> "Ready to Drive"
                                    DashboardState.CONNECTING -> "Connecting..."
                                    DashboardState.ACTIVE -> "Tracking Active"
                                    DashboardState.ERROR -> "Action Required"
                                },
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = errorMessage ?: when (state) {
                                    DashboardState.IDLE -> "Configure trip details below"
                                    DashboardState.CONNECTING -> "Syncing with satellite..."
                                    DashboardState.ACTIVE -> "Location data is live"
                                    DashboardState.ERROR -> "Please check connection"
                                },
                                fontSize = 15.sp,
                                color = Color.White.copy(alpha = 0.8f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- 2. INPUTS ---
            Column(modifier = Modifier.padding(horizontal = 24.dp)) {
                Text(
                    text = "TRIP CONFIGURATION",
                    fontSize = 12.sp,
                    color = TextSecondary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 4.dp, bottom = 12.dp)
                )

                SelectionCard(
                    title = "Vehicle",
                    value = selectedBus,
                    placeholder = "Select Bus",
                    icon = Icons.Default.DirectionsBus,
                    options = busList,
                    isLocked = areInputsLocked,
                    onSelect = {
                        selectedBus = it
                        if(currentState == DashboardState.ERROR) {
                            currentState = DashboardState.IDLE
                            errorMessage = null
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                SelectionCard(
                    title = "Route",
                    value = selectedRoute,
                    placeholder = "Select Route",
                    icon = Icons.Default.AltRoute,
                    options = routeList,
                    isLocked = areInputsLocked,
                    onSelect = {
                        selectedRoute = it
                        if(currentState == DashboardState.ERROR) {
                            currentState = DashboardState.IDLE
                            errorMessage = null
                        }
                    }
                )
            }
            Spacer(modifier = Modifier.height(100.dp))
        }

        // --- 3. DIALOG ---
        if (showExitDialog) {
            AlertDialog(
                onDismissRequest = { showExitDialog = false },
                title = { Text(text = "Stop Trip?", fontWeight = FontWeight.Bold) },
                text = { Text("You are currently tracking a trip. Going back will stop the session. Are you sure?") },
                containerColor = Color.White,
                confirmButton = {
                    Button(
                        onClick = {
                            showExitDialog = false
                            onBackClick()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                    ) {
                        Text("Stop & Exit", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showExitDialog = false }) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            )
        }
    }
}

// --- COMPONENTS ---

@Composable
fun ProStatusOrb(state: DashboardState) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val radarScale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.6f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Restart), label = "radar"
    )
    val radarAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Restart), label = "alpha"
    )

    val mainIconColor = when (state) {
        DashboardState.IDLE -> IdleBlue
        DashboardState.CONNECTING -> WarningOrange
        DashboardState.ACTIVE -> ActiveGreen
        DashboardState.ERROR -> ErrorRed
    }

    val badgeIcon = when(state) {
        DashboardState.IDLE -> Icons.Default.GpsFixed
        DashboardState.CONNECTING -> Icons.Default.Refresh
        DashboardState.ACTIVE -> Icons.Default.CheckCircle
        DashboardState.ERROR -> Icons.Default.Warning
    }

    Box(contentAlignment = Alignment.Center) {
        if (state == DashboardState.ACTIVE || state == DashboardState.CONNECTING) {
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .scale(radarScale)
                    .background(Color.White.copy(alpha = radarAlpha), CircleShape)
            )
        }

        Box(
            modifier = Modifier
                .size(110.dp)
                .shadow(16.dp, CircleShape, spotColor = Color.Black.copy(alpha = 0.2f))
                .background(Color.White, CircleShape)
                .border(4.dp, Color.White.copy(alpha = 0.5f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsBus,
                contentDescription = null,
                modifier = Modifier.size(52.dp),
                tint = mainIconColor
            )
        }

        Box(
            modifier = Modifier
                .size(32.dp)
                .align(Alignment.BottomEnd)
                .offset(x = (-4).dp, y = (-4).dp)
                .shadow(4.dp, CircleShape)
                .background(mainIconColor, CircleShape)
                .border(2.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = badgeIcon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun BottomActionBar(state: DashboardState, onAction: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .shadow(20.dp, clip = false)
            .padding(24.dp)
    ) {
        val btnColor by animateColorAsState(
            when (state) {
                DashboardState.ACTIVE -> ErrorRed
                DashboardState.ERROR -> IdleBlue
                DashboardState.CONNECTING -> Color.Gray
                else -> IdleBlue
            }, label = "BtnColor"
        )

        val btnText = when (state) {
            DashboardState.ACTIVE -> "END TRIP"
            DashboardState.CONNECTING -> "CONNECTING..."
            DashboardState.ERROR -> "RETRY CONNECTION"
            DashboardState.IDLE -> "START TRIP"
        }

        val icon = when (state) {
            DashboardState.ERROR -> Icons.Default.Refresh
            DashboardState.ACTIVE -> Icons.Default.PowerSettingsNew
            else -> Icons.Default.PowerSettingsNew
        }

        Button(
            onClick = onAction,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = btnColor,
                disabledContainerColor = Color.Gray
            ),
            enabled = state != DashboardState.CONNECTING
        ) {
            if (state == DashboardState.CONNECTING) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
            } else {
                Icon(icon, null)
                Spacer(modifier = Modifier.width(12.dp))
                Text(btnText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SelectionCard(
    title: String,
    value: String?,
    placeholder: String,
    icon: ImageVector,
    options: List<String>,
    isLocked: Boolean,
    onSelect: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val alpha = if (isLocked) 0.5f else 1f

    Box {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .alpha(alpha)
                .clip(RoundedCornerShape(16.dp))
                .background(if (isLocked) Color(0xFFF1F5F9) else Color.White)
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                .clickable(enabled = !isLocked) { expanded = true }
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFFF8FAFC), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = TextSecondary)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 11.sp, color = TextSecondary)
                Text(
                    text = value ?: placeholder,
                    fontSize = 16.sp,
                    color = if (value != null) TextPrimary else Color.Gray,
                    fontWeight = if (value != null) FontWeight.SemiBold else FontWeight.Normal
                )
            }
            if(isLocked) {
                Icon(Icons.Default.Lock, null, tint = TextSecondary)
            } else {
                Icon(Icons.Default.KeyboardArrowDown, null, tint = TextSecondary)
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White).fillMaxWidth(0.85f)
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option, color = TextPrimary) },
                    onClick = { onSelect(option); expanded = false }
                )
            }
        }
    }
}

@Preview
@Composable
fun PreviewRobustDashboard() {
    DriverDashboardScreen({})
}