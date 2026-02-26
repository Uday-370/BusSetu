package com.example.bussetu.feature_driver.presentation

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
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
import androidx.compose.material.icons.automirrored.filled.ExitToApp
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bussetu.core.ui.theme.BrandBlue
import com.example.bussetu.core.ui.theme.TextPrimary
import com.example.bussetu.core.ui.theme.TextSecondary
import kotlinx.coroutines.launch

val IdleBlue = BrandBlue
val ActiveGreen = Color(0xFF059669)
val ErrorRed = Color(0xFFDC2626)
val WarningOrange = Color(0xFFD97706)
val SurfaceColor = Color(0xFFF8FAFC)

// ✅ ADDED: DISCONNECTING state
enum class DashboardState {
    IDLE, CONNECTING, ACTIVE, DISCONNECTING, ERROR
}

@Composable
fun DriverDashboardScreen(
    onBackClick: () -> Unit,
    onLogoutClick: () -> Unit,
    viewModel: DriverViewModel = hiltViewModel()
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // ✅ FIX: Smart state mapping to detect disconnecting
    val currentState = when {
        state.isLoading && state.isTracking -> DashboardState.DISCONNECTING // Ending the trip
        state.isLoading -> DashboardState.CONNECTING // Starting the trip
        state.isTracking -> DashboardState.ACTIVE
        state.error != null -> DashboardState.ERROR
        else -> DashboardState.IDLE
    }

    var showExitDialog by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }

    val headerColor by animateColorAsState(
        targetValue = when (currentState) {
            DashboardState.IDLE -> IdleBlue
            DashboardState.CONNECTING, DashboardState.DISCONNECTING -> WarningOrange
            DashboardState.ACTIVE -> ActiveGreen
            DashboardState.ERROR -> ErrorRed
        },
        label = "HeaderColor"
    )

    // Lock inputs if we are loading, active, or disconnecting
    val areInputsLocked = currentState != DashboardState.IDLE && currentState != DashboardState.ERROR

    val busList = state.availableBuses.map { it.busNumber }
    val routeList = state.availableRoutes.map { it.routeName }
    val selectedBusStr = state.selectedBus?.busNumber
    val selectedRouteStr = state.selectedRoute?.routeName

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val locationGranted = permissions[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissions[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true

            if (locationGranted) {
                // If they said YES, start the trip!
                viewModel.startDuty()
            } else {
                // If they said NO, show an error
                scope.launch {
                    snackbarHostState.showSnackbar("Location permission is required to track the bus.")
                }
            }
        }
    )

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = SurfaceColor,
        bottomBar = {
            BottomActionBar(
                state = currentState,
                onAction = {
                    when (currentState) {
                        DashboardState.IDLE, DashboardState.ERROR -> {
                            // ✅ 2. ASK FOR PERMISSIONS INSTEAD OF STARTING IMMEDIATELY
                            val permissionsToRequest = mutableListOf(
                                android.Manifest.permission.ACCESS_FINE_LOCATION,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                            // Android 13+ requires explicit notification permission for the sticky Foreground Service notification
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                                permissionsToRequest.add(android.Manifest.permission.POST_NOTIFICATIONS)
                            }

                            // Launch the popup!
                            permissionLauncher.launch(permissionsToRequest.toTypedArray())
                        }
                        DashboardState.ACTIVE -> {
                            // Hit END TRIP at bottom - disconnects quietly
                            viewModel.stopDuty { }
                        }
                        else -> {}
                    }
                }
            )
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
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
                        .statusBarsPadding()
                        .padding(top = 12.dp, start = 20.dp, end = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // --- NAVBAR ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 1. BACK BUTTON
                        IconButton(
                            onClick = {
                                if (currentState == DashboardState.ACTIVE) {
                                    showExitDialog = true
                                } else if (currentState == DashboardState.IDLE || currentState == DashboardState.ERROR) {
                                    onBackClick()
                                }
                            },
                            modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // 2. STATUS PILL
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

                        Spacer(modifier = Modifier.width(12.dp))

                        // 3. LOGOUT BUTTON
                        IconButton(
                            onClick = {
                                if (currentState == DashboardState.ACTIVE) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Cannot log out while trip is active. Please end trip first.")
                                    }
                                } else if (currentState == DashboardState.IDLE || currentState == DashboardState.ERROR) {
                                    showLogoutDialog = true
                                }
                            },
                            modifier = Modifier.background(Color.White.copy(alpha = 0.2f), CircleShape)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ExitToApp, null, tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Pro Orb
                    ProStatusOrb(state = currentState)

                    Spacer(modifier = Modifier.height(24.dp))

                    AnimatedContent(targetState = currentState, label = "text") { animState ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                // ✅ FIX: Custom text for Disconnecting
                                text = when (animState) {
                                    DashboardState.IDLE -> "Ready to Drive"
                                    DashboardState.CONNECTING -> "Connecting..."
                                    DashboardState.DISCONNECTING -> "Disconnecting..."
                                    DashboardState.ACTIVE -> "Tracking Active"
                                    DashboardState.ERROR -> "Action Required"
                                },
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                // ✅ FIX: Custom subtitle for Disconnecting
                                text = state.error ?: when (animState) {
                                    DashboardState.IDLE -> "Configure trip details below"
                                    DashboardState.CONNECTING -> "Syncing with server..."
                                    DashboardState.DISCONNECTING -> "Safely ending your trip..."
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
                    value = selectedBusStr,
                    placeholder = "Select Bus",
                    icon = Icons.Default.DirectionsBus,
                    options = busList,
                    isLocked = areInputsLocked,
                    onSelect = { busName ->
                        state.availableBuses.find { it.busNumber == busName }?.let {
                            viewModel.onBusSelected(it)
                        }
                        if(state.error != null) viewModel.dismissError()
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                SelectionCard(
                    title = "Route",
                    value = selectedRouteStr,
                    placeholder = "Select Route",
                    icon = Icons.Default.AltRoute,
                    options = routeList,
                    isLocked = areInputsLocked,
                    onSelect = { routeName ->
                        state.availableRoutes.find { it.routeName == routeName }?.let {
                            viewModel.onRouteSelected(it)
                        }
                        if(state.error != null) viewModel.dismissError()
                    }
                )
            }
            Spacer(modifier = Modifier.height(100.dp))
        }

        // --- 3. DIALOGS ---
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
                            // Orb will instantly show "Disconnecting...", and once success is hit, it navigates
                            viewModel.stopDuty {
                                onBackClick()
                            }
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

        if (showLogoutDialog) {
            AlertDialog(
                onDismissRequest = { showLogoutDialog = false },
                title = { Text(text = "Log Out?", fontWeight = FontWeight.Bold) },
                text = { Text("Are you sure you want to log out of your driver account?") },
                containerColor = Color.White,
                confirmButton = {
                    Button(
                        onClick = {
                            showLogoutDialog = false
                            viewModel.logout()
                            onLogoutClick()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                    ) {
                        Text("Log Out", color = Color.White)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showLogoutDialog = false }) {
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
        DashboardState.CONNECTING, DashboardState.DISCONNECTING -> WarningOrange
        DashboardState.ACTIVE -> ActiveGreen
        DashboardState.ERROR -> ErrorRed
    }

    val badgeIcon = when(state) {
        DashboardState.IDLE -> Icons.Default.GpsFixed
        DashboardState.CONNECTING, DashboardState.DISCONNECTING -> Icons.Default.Refresh
        DashboardState.ACTIVE -> Icons.Default.CheckCircle
        DashboardState.ERROR -> Icons.Default.Warning
    }

    Box(contentAlignment = Alignment.Center) {
        // ✅ FIX: Radar animates for connecting AND disconnecting
        if (state == DashboardState.ACTIVE || state == DashboardState.CONNECTING || state == DashboardState.DISCONNECTING) {
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
                DashboardState.CONNECTING, DashboardState.DISCONNECTING -> Color.Gray
                else -> IdleBlue
            }, label = "BtnColor"
        )

        // ✅ FIX: Text specifically for disconnecting
        val btnText = when (state) {
            DashboardState.ACTIVE -> "END TRIP"
            DashboardState.CONNECTING -> "CONNECTING..."
            DashboardState.DISCONNECTING -> "DISCONNECTING..."
            DashboardState.ERROR -> "RETRY CONNECTION"
            DashboardState.IDLE -> "START TRIP"
        }

        val icon = when (state) {
            DashboardState.ERROR -> Icons.Default.Refresh
            DashboardState.ACTIVE -> Icons.Default.PowerSettingsNew
            else -> Icons.Default.PowerSettingsNew
        }

        val isLoading = state == DashboardState.CONNECTING || state == DashboardState.DISCONNECTING

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
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
            } else {
                Icon(icon, null)
                Spacer(modifier = Modifier.width(12.dp))
                Text(btnText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// (SelectionCard remains exactly the same)
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