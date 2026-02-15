package com.example.trackmybus.core.ui.userdashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.outlined.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.trackmybus.core.ui.components.TMBTextField
import com.example.trackmybus.core.ui.components.TMBTopBar
import com.example.trackmybus.ui.theme.BrandBlue
import com.example.trackmybus.ui.theme.TextPrimary
import com.example.trackmybus.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Simple Enum for the Tabs
enum class SearchTab { BY_ROUTE, BY_BUS_NO }

@Composable
fun UserDashboardScreen(
    onMenuClick: () -> Unit,
    onNavigateToMap: () -> Unit
) {
    // --- 1. Local UI State & Data ---
    var selectedTab by remember { mutableStateOf(SearchTab.BY_ROUTE) }
    var startLocation by remember { mutableStateOf("") }
    var endLocation by remember { mutableStateOf("") }
    var busNumber by remember { mutableStateOf("") }

    var isLoading by remember { mutableStateOf(false) }

    // FAKE DATA for suggestions
    val fakeLocations = remember {
        listOf(
            "Central Station", "City Park Plaza", "Airport Terminal 1",
            "University Main Gate", "Tech Hub Towers", "Downtown Market",
            "Westside Mall", "General Hospital", "Riverfront Station"
        )
    }


    // --- 2. Snackbar State ---
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // --- Helper to Handle Navigation Logic ---
    fun executeSearch(searchDescription: String) {
        scope.launch {
            if (isLoading) return@launch // Prevent double clicks

            isLoading = true
            delay(1500) // Simulate network delay
            isLoading = false
            onNavigateToMap()
        }
    }

    // --- 3. Main Search Button Logic ---
    fun onFindBusClick() {
        scope.launch {
            if (selectedTab == SearchTab.BY_ROUTE) {
                if (startLocation.isBlank()) {
                    snackbarHostState.showSnackbar("Please enter a start location")
                    return@launch
                }
                if (endLocation.isBlank()) {
                    snackbarHostState.showSnackbar("Please enter a destination")
                    return@launch
                }
                if (startLocation.trim().equals(endLocation.trim(), ignoreCase = true)) {
                    snackbarHostState.showSnackbar("Start and End locations cannot be the same")
                    return@launch
                }
                executeSearch("$startLocation to $endLocation")
            } else {
                if (busNumber.isBlank()) {
                    snackbarHostState.showSnackbar("Please enter a bus number")
                    return@launch
                }
                executeSearch("Bus $busNumber")
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TMBTopBar(
                titleContent = {
                    Text(
                        text = "BusSetu",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandBlue
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClick) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = TextPrimary)
                    }
                }
            )
        },
        containerColor = Color.White
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(24.dp)
        ) {

            // Header
            item {
                Text(
                    text = "Find Your Bus",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Tabs
            item {
                SearchTabs(
                    currentTab = selectedTab,
                    onTabSelected = { selectedTab = it }
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Inputs
            item {
                if (selectedTab == SearchTab.BY_ROUTE) {
                    // Passes the suggestion list down
                    RouteSearchInputs(
                        start = startLocation,
                        end = endLocation,
                        suggestions = fakeLocations,
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

            // --- SEARCH BUTTON WITH INTERNAL LOADING ---
            item {
                Button(
                    onClick = { if (!isLoading) onFindBusClick() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp), // Standard height
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandBlue,
                        disabledContainerColor = BrandBlue.copy(alpha = 0.7f) // Keep it blue but dim
                    ),
                    enabled = !isLoading // Disable click while loading
                ) {
                    if (isLoading) {
                        // Small white spinner inside the button
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 3.dp
                        )
                    } else {
                        // Standard Text
                        Text(
                            text = "SEARCH ROUTES",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            // Recent Searches
            item {
                Text(
                    text = "Recent Searches",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(16.dp))

                // Clickable Recent Items (Triggers Global Loading)
                RecentSearchItem(
                    text = "Route 42: Downtown → Airport",
                    onClick = { executeSearch("Route 42") }
                )
                Spacer(modifier = Modifier.height(12.dp))

                RecentSearchItem(
                    text = "Route 10A: University → Mall",
                    onClick = { executeSearch("Route 10A") }
                )
                Spacer(modifier = Modifier.height(12.dp))

                RecentSearchItem(
                    text = "Bus 24: Central → Malkapur",
                    onClick = { executeSearch("Bus 24") }
                )
            }
        }
    }
}

// --- SUB-COMPONENTS ---

@Composable
private fun SearchTabs(
    currentTab: SearchTab,
    onTabSelected: (SearchTab) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
            .background(Color(0xFFF5F7FA), RoundedCornerShape(25.dp))
            .padding(4.dp)
    ) {
        TabButton(
            text = "By Route",
            isActive = currentTab == SearchTab.BY_ROUTE,
            onClick = { onTabSelected(SearchTab.BY_ROUTE) },
            modifier = Modifier.weight(1f)
        )
        TabButton(
            text = "By Bus No.",
            isActive = currentTab == SearchTab.BY_BUS_NO,
            onClick = { onTabSelected(SearchTab.BY_BUS_NO) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun TabButton(
    text: String,
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(21.dp))
            .background(if (isActive) BrandBlue else Color.Transparent)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isActive) Color.White else TextSecondary,
            fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun RouteSearchInputs(
    start: String,
    end: String,
    suggestions: List<String>, // New parameter for data
    onStartChange: (String) -> Unit,
    onEndChange: (String) -> Unit,
    onSwap: () -> Unit
) {
    // State to control dropdown visibility
    var isStartExpanded by remember { mutableStateOf(false) }
    var isEndExpanded by remember { mutableStateOf(false) }

    // Simple Filtering Logic based on user input
    // Shows max 5 results that match the input, excluding exact match
    val startFilteredItems = suggestions.filter {
        it.contains(start, ignoreCase = true) && it != start
    }.take(5)

    val endFilteredItems = suggestions.filter {
        it.contains(end, ignoreCase = true) && it != end
    }.take(5)

    Box(modifier = Modifier.fillMaxWidth()) {
        Column {
            // --- START LOCATION INPUT & DROPDOWN ---
            // We use Box + zIndex to ensure dropdown appears on top of elements below it
            Box(modifier = Modifier.fillMaxWidth().zIndex(2f)) {
                TMBTextField(
                    value = start,
                    onValueChange = {
                        onStartChange(it)
                        // Open dropdown only if text is not empty
                        isStartExpanded = it.isNotEmpty()
                    },
                    placeholder = "Start Location",
                    icon = Icons.Outlined.Place,
                    modifier = Modifier.fillMaxWidth()
                    // Note: If TMBTextField has an 'onFocusChanged', set isStartExpanded = true there too for better UX
                )

                // The Dropdown Menu
                DropdownMenu(
                    expanded = isStartExpanded && startFilteredItems.isNotEmpty(),
                    onDismissRequest = { isStartExpanded = false },
                    // Using fillMaxWidth(0.9f) as a simple heuristic to match textfield width roughly.
                    // For perfect width matching, ExposedDropdownMenuBox is needed (requires modifying TMBTextField).
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .background(Color.White)
                ) {
                    startFilteredItems.forEach { label ->
                        DropdownMenuItem(
                            text = { Text(text = label, color = TextPrimary) },
                            onClick = {
                                onStartChange(label)
                                isStartExpanded = false
                            },
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // --- END LOCATION INPUT & DROPDOWN ---
            Box(modifier = Modifier.fillMaxWidth().zIndex(1f)) {
                TMBTextField(
                    value = end,
                    onValueChange = {
                        onEndChange(it)
                        isEndExpanded = it.isNotEmpty()
                    },
                    placeholder = "End Destination",
                    icon = Icons.Filled.Place,
                    modifier = Modifier.fillMaxWidth()
                )

                DropdownMenu(
                    expanded = isEndExpanded && endFilteredItems.isNotEmpty(),
                    onDismissRequest = { isEndExpanded = false },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .background(Color.White)
                ) {
                    endFilteredItems.forEach { label ->
                        DropdownMenuItem(
                            text = { Text(text = label, color = TextPrimary) },
                            onClick = {
                                onEndChange(label)
                                isEndExpanded = false
                            },
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                }
            }
        }

        // The Swap Button (zIndex 3 ensures it's above the dropdowns)
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 24.dp)
                .offset(y = (-4).dp)
                .zIndex(3f)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(1.dp, Color(0xFFE0E0E0), CircleShape)
                    .clickable { onSwap() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.SwapVert,
                    contentDescription = "Swap",
                    tint = BrandBlue
                )
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
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.DirectionsBus,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            fontSize = 15.sp,
            color = TextPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewUserDashboard() {
    UserDashboardScreen(
        onMenuClick = {},
        onNavigateToMap = {}
    )
}