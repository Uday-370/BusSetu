package com.example.bussetu.feature_map.presentation.mapscreen

import android.app.Activity
import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.bussetu.core.ui.theme.BrandBlue
import com.example.bussetu.core.ui.theme.TextPrimary
import com.example.bussetu.core.ui.theme.TextSecondary
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

// --- COLORS ---
val DarkSurface = Color(0xFF1E293B)
val AccentRed = Color(0xFFEF4444)
val MutedGrey = Color(0xFF94A3B8)

// --- MODELS ---
enum class StopStatus { COMPLETED, CURRENT, UPCOMING }

data class BusRouteStop(
    val id: String,
    val name: String,
    val scheduledTime: String, // e.g. "10:30"
    val delayMinutes: Int,     // How many minutes late? (0 = On Time)
    val status: StopStatus
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onBackClick: () -> Unit
) {
    // --- FIX: Force Status Bar Icons to be Dark (Black) ---
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // This makes Battery/Time icons BLACK so they are visible on the map
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    // --- MOCK DATA ---
    val stops = remember {
        listOf(
            BusRouteStop("1", "Central Station", "10:00 AM", 0, StopStatus.COMPLETED),
            BusRouteStop("2", "Law College Rd", "10:15 AM", 0, StopStatus.COMPLETED),
            BusRouteStop("3", "City Park Plaza", "10:30 AM", 15, StopStatus.CURRENT),
            BusRouteStop("4", "Anno Stop", "10:45 AM", 15, StopStatus.UPCOMING),
            BusRouteStop("5", "Airport Terminal", "11:00 AM", 15, StopStatus.UPCOMING)
        )
    }

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(
            initialValue = SheetValue.PartiallyExpanded
        )
    )

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 220.dp,
        sheetContainerColor = Color.White,
        sheetShadowElevation = 24.dp,
        sheetShape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        sheetContent = {
            Column(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.6f)
            ) {
                // Drag Handle
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 12.dp)
                            .width(48.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color.LightGray.copy(alpha = 0.5f))
                    )
                }

                LazyColumn(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    contentPadding = PaddingValues(bottom = 32.dp)
                ) {
                    item {
                        Text(
                            text = "Trip Progress",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            modifier = Modifier.padding(bottom = 20.dp)
                        )
                    }
                    itemsIndexed(stops) { index, stop ->
                        DashingTimelineItem(
                            stop = stop,
                            isLast = index == stops.lastIndex
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            // 1. OPEN STREET MAP IMPLEMENTATION
            OsmMapView(modifier = Modifier.fillMaxSize())

            // 2. Floating Header
            FloatingHeader(
                routeCode = "42",
                destination = "Downtown → Airport",
                onBackClick = onBackClick
            )
        }
    }
}

// ==========================================
// *** OSM COMPOSABLE IMPLEMENTATION ***
// ==========================================
@Composable
fun OsmMapView(
    modifier: Modifier = Modifier,
    startLat: Double = 18.5204,
    startLng: Double = 73.8567
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Initialize OSM Configuration (Required!)
    DisposableEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        onDispose { }
    }

    // Create and Manage MapView Lifecycle
    val mapView = remember {
        MapView(context).apply {
            setTileSource(TileSourceFactory.MAPNIK) // Default OSM Look
            setMultiTouchControls(true)
            controller.setZoom(15.0)
            controller.setCenter(GeoPoint(startLat, startLng))

            // Add a Bus Marker
            val busMarker = Marker(this)
            busMarker.position = GeoPoint(startLat, startLng)
            busMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            busMarker.title = "Bus Location"
            overlays.add(busMarker)
        }
    }

    // Handle Lifecycle (Pause/Resume map to save battery/data)
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> mapView.onResume()
                Lifecycle.Event.ON_PAUSE -> mapView.onPause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapView.onDetach()
        }
    }

    AndroidView(
        factory = { mapView },
        modifier = modifier
    )
}

// --- FLOATING HEADER ---
@Composable
fun FloatingHeader(routeCode: String, destination: String, onBackClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp, start = 16.dp, end = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Surface(
            modifier = Modifier.height(64.dp).shadow(12.dp, RoundedCornerShape(50)),
            shape = RoundedCornerShape(50),
            color = DarkSurface,
            contentColor = Color.White
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp).fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier.size(48.dp).background(Color.White.copy(alpha = 0.1f), CircleShape)
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(verticalArrangement = Arrangement.Center, modifier = Modifier.padding(end = 24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.background(BrandBlue, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(routeCode, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Live Tracking", fontSize = 12.sp, color = Color(0xFF10B981), fontWeight = FontWeight.Bold)
                    }
                    Text(destination, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                }
            }
        }
    }
}

// --- DASHING TIMELINE ITEM ---
@Composable
fun DashingTimelineItem(stop: BusRouteStop, isLast: Boolean) {
    val isCurrent = stop.status == StopStatus.CURRENT
    val lineColor = if (stop.status == StopStatus.COMPLETED) BrandBlue else MutedGrey.copy(alpha = 0.5f)
    val isDashed = stop.status == StopStatus.UPCOMING

    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        // GRAPHICS COLUMN
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(48.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(32.dp)) {
                if (isCurrent) {
                    PulsingBusNode()
                } else {
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .clip(CircleShape)
                            .background(if (stop.status == StopStatus.COMPLETED) BrandBlue else MutedGrey)
                            .border(2.dp, Color.White, CircleShape)
                    )
                }
            }
            if (!isLast) {
                if (isDashed) {
                    Canvas(modifier = Modifier.width(2.dp).weight(1f)) {
                        drawLine(
                            color = lineColor,
                            start = Offset(center.x, 0f),
                            end = Offset(center.x, size.height),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
                            strokeWidth = 4f
                        )
                    }
                } else {
                    Box(modifier = Modifier.width(2.dp).weight(1f).background(lineColor))
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // CONTENT COLUMN
        Box(modifier = Modifier.weight(1f).padding(bottom = 24.dp)) {
            if (isCurrent) {
                CurrentStopCard(stop)
            } else {
                StandardStopInfo(stop)
            }
        }
    }
}

// --- THE RESTORED CARD COMPONENT ---
@Composable
fun CurrentStopCard(stop: BusRouteStop) {
    Card(
        colors = CardDefaults.cardColors(containerColor = BrandBlue.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Arriving Now",
                    fontSize = 12.sp,
                    color = BrandBlue,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = stop.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            // Delay Badge inside Card
            if (stop.delayMinutes > 0) {
                Surface(
                    color = AccentRed,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "+${stop.delayMinutes} min",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            } else {
                Surface(
                    color = BrandBlue,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "On Time",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun StandardStopInfo(stop: BusRouteStop) {
    Column {
        Text(
            text = stop.name,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = if (stop.status == StopStatus.UPCOMING) TextSecondary else TextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.AccessTime, null, tint = TextSecondary, modifier = Modifier.size(12.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stop.scheduledTime,
                fontSize = 13.sp,
                color = TextSecondary
            )
            if (stop.delayMinutes > 0) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "+${stop.delayMinutes} min",
                    color = AccentRed,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// --- PULSING ANIMATION ---
@Composable
fun PulsingBusNode() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.4f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "scale"
    )

    Box(contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.size(32.dp).scale(scale).background(BrandBlue.copy(alpha = 0.3f), CircleShape))
        Box(modifier = Modifier.size(24.dp).background(BrandBlue, CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.DirectionsBus, null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
    }
}