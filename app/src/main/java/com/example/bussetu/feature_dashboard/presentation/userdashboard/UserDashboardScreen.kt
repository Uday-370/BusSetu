package com.example.bussetu.feature_dashboard.presentation.userdashboard

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.PopupProperties
import androidx.compose.ui.zIndex
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bussetu.core.presentation.components.TMBTextField
import com.example.bussetu.core.presentation.components.TMBTopBar
import com.example.bussetu.core.ui.theme.BrandBlue
import com.example.bussetu.core.ui.theme.TextPrimary
import com.example.bussetu.core.ui.theme.TextSecondary
import kotlinx.coroutines.launch

// Simple Enum for the Tabs
enum class SearchTab { BY_ROUTE, BY_BUS_NO }

@Composable
fun UserDashboardScreen(
    onMenuClick: () -> Unit,
    onNavigateToMap: (Int) -> Unit,
    onNavigateToChatbot: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    // --- FIX: Force Status Bar Icons to be Dark (Black) ---
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    // --- 1. View Model States ---
    val suggestions by viewModel.stopSuggestions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val recentSearches by viewModel.recentSearches.collectAsState()

    // --- 2. Local UI State ---
    var selectedTab by remember { mutableStateOf(SearchTab.BY_ROUTE) }
    var startLocation by remember { mutableStateOf("") }
    var endLocation by remember { mutableStateOf("") }
    var busNumber by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // --- 3. Listen for Navigation & Errors from ViewModel ---
    LaunchedEffect(Unit) {
        viewModel.navigateToMap.collect { tripId ->
            // The ViewModel found the trip in the DB! Go to the map.
            onNavigateToMap(tripId)
        }
    }

    LaunchedEffect(Unit) {
        viewModel.errorMessage.collect { message ->
            // If the bus isn't found, show the error on the screen.
            snackbarHostState.showSnackbar(message)
        }
    }

    // --- 4. Main Search Button Logic ---
    fun onFindBusClick() {
        if (selectedTab == SearchTab.BY_ROUTE) {
            if (startLocation.isBlank()) {
                scope.launch { snackbarHostState.showSnackbar("Please enter a start location") }
                return
            }
            if (endLocation.isBlank()) {
                scope.launch { snackbarHostState.showSnackbar("Please enter a destination") }
                return
            }
            if (startLocation.trim().equals(endLocation.trim(), ignoreCase = true)) {
                scope.launch { snackbarHostState.showSnackbar("Start and End locations cannot be the same") }
                return
            }
            // ✅ Trigger real ViewModel search
            viewModel.searchByRoute(startLocation, endLocation)

        } else {
            if (busNumber.isBlank()) {
                scope.launch { snackbarHostState.showSnackbar("Please enter a bus number") }
                return
            }
            // ✅ Trigger real ViewModel search
            viewModel.searchByBusNumber(busNumber)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TMBTopBar(
                titleContent = {
                    Text(text = "BusSetu", fontSize = 25.sp, fontWeight = FontWeight.Bold, color = BrandBlue)
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToChatbot,
                containerColor = BrandBlue,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.size(60.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = "Open Chatbot",
                    modifier = Modifier.size(28.dp)
                )
            }
        },
        containerColor = Color.White
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(24.dp)
        ) {

            item {
                Text(text = "Find Your Bus", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                SearchTabs(currentTab = selectedTab, onTabSelected = { selectedTab = it })
                Spacer(modifier = Modifier.height(24.dp))
            }

            item {
                if (selectedTab == SearchTab.BY_ROUTE) {
                    RouteSearchInputs(
                        start = startLocation,
                        end = endLocation,
                        suggestions = suggestions,
                        onStartChange = { startLocation = it },
                        onEndChange = { endLocation = it },
                        onSwap = {
                            val temp = startLocation
                            startLocation = endLocation
                            endLocation = temp
                        }
                    )
                } else {
                    TMBTextField(
                        value = busNumber,
                        onValueChange = { busNumber = it },
                        placeholder = "Enter Bus Number (e.g. 24)",
                        icon = Icons.Default.DirectionsBus,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            // --- SEARCH BUTTON ---
            item {
                Button(
                    onClick = { if (!isLoading) onFindBusClick() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandBlue,
                        disabledContainerColor = BrandBlue.copy(alpha = 0.7f)
                    ),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                    } else {
                        Text(text = "SEARCH ROUTES", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            // --- RECENT SEARCHES ---
            if (recentSearches.isNotEmpty()) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Recent Searches",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                    }
                }
                items(recentSearches) { entry ->
                    RecentSearchItem(
                        text = entry,
                        onClick = {
                            viewModel.onRecentSearchClick(
                                entry,
                                onStartChange = { startLocation = it },
                                onEndChange = { endLocation = it }
                            )
                            selectedTab = SearchTab.BY_ROUTE
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

// --- SUB-COMPONENTS (Left exactly as you styled them) ---

@Composable
private fun SearchTabs(currentTab: SearchTab, onTabSelected: (SearchTab) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().height(50.dp).background(Color(0xFFF5F7FA), RoundedCornerShape(25.dp)).padding(4.dp)
    ) {
        TabButton("By Route", currentTab == SearchTab.BY_ROUTE, { onTabSelected(SearchTab.BY_ROUTE) }, Modifier.weight(1f))
        TabButton("By Bus No.", currentTab == SearchTab.BY_BUS_NO, { onTabSelected(SearchTab.BY_BUS_NO) }, Modifier.weight(1f))
    }
}

@Composable
private fun TabButton(text: String, isActive: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxHeight().clip(RoundedCornerShape(21.dp)).background(if (isActive) BrandBlue else Color.Transparent).clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = if (isActive) Color.White else TextSecondary, fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium, fontSize = 14.sp)
    }
}

@Composable
private fun RouteSearchInputs(
    start: String, end: String, suggestions: List<String>, onStartChange: (String) -> Unit, onEndChange: (String) -> Unit, onSwap: () -> Unit
) {
    var isStartExpanded by remember { mutableStateOf(false) }
    var isEndExpanded by remember { mutableStateOf(false) }

    val startFilteredItems = suggestions.filter { it.contains(start, ignoreCase = true) && it != start }.take(5)
    val endFilteredItems = suggestions.filter { it.contains(end, ignoreCase = true) && it != end }.take(5)

    Box(modifier = Modifier.fillMaxWidth()) {
        Column {
            Box(modifier = Modifier.fillMaxWidth().zIndex(2f)) {
                TMBTextField(
                    value = start, onValueChange = { onStartChange(it); isStartExpanded = it.isNotEmpty() },
                    placeholder = "Start Location", icon = Icons.Outlined.Place, modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                )
                DropdownMenu(
                    expanded = isStartExpanded && startFilteredItems.isNotEmpty(), onDismissRequest = { isStartExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f).background(Color.White), properties = PopupProperties(focusable = false)
                ) {
                    startFilteredItems.forEach { label ->
                        DropdownMenuItem(
                            text = { Text(text = label, color = TextPrimary) },
                            onClick = { onStartChange(label); isStartExpanded = false },
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Box(modifier = Modifier.fillMaxWidth().zIndex(1f)) {
                TMBTextField(
                    value = end, onValueChange = { onEndChange(it); isEndExpanded = it.isNotEmpty() },
                    placeholder = "End Destination", icon = Icons.Filled.Place, modifier = Modifier.fillMaxWidth(),
                    textStyle = TextStyle(fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                )
                DropdownMenu(
                    expanded = isEndExpanded && endFilteredItems.isNotEmpty(), onDismissRequest = { isEndExpanded = false },
                    modifier = Modifier.fillMaxWidth(0.9f).background(Color.White), properties = PopupProperties(focusable = false)
                ) {
                    endFilteredItems.forEach { label ->
                        DropdownMenuItem(
                            text = { Text(text = label, color = TextPrimary) },
                            onClick = { onEndChange(label); isEndExpanded = false },
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                }
            }
        }
        Box(modifier = Modifier.align(Alignment.CenterEnd).padding(end = 24.dp).offset(y = (-4).dp).zIndex(3f)) {
            Box(
                modifier = Modifier.size(44.dp).clip(CircleShape).background(Color.White).border(1.dp, Color(0xFFE0E0E0), CircleShape).clickable { onSwap() },
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = Icons.Default.SwapVert, contentDescription = "Swap", tint = BrandBlue)
            }
        }
    }
}

@Composable
private fun RecentSearchItem(text: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF8F9FA))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(BrandBlue.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.History,
                contentDescription = null,
                tint = BrandBlue,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = text,
            fontSize = 15.sp,
            color = TextPrimary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
    }
}