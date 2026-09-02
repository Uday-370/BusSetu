package com.example.bussetu.feature_map.presentation.mapscreen

import android.app.Activity
import android.content.Context
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.example.bussetu.core.ui.theme.BrandBlue
import com.example.bussetu.core.ui.theme.TextPrimary
import com.example.bussetu.core.ui.theme.TextSecondary
import com.example.bussetu.feature_map.domain.model.BusRouteStop
import com.example.bussetu.feature_map.domain.model.StopStatus
import com.example.trackmybus.R
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Polyline
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

val DarkSurface = Color(0xFF1E293B)
val MutedGrey  = Color(0xFF94A3B8)
val DelayRed   = Color(0xFFEF4444)
val DelayRedBg = Color(0xFFFEF2F2)

// Helper function for the UI layer to format time
fun calculateExpectedTime(scheduledTime: String, delayMinutes: Int): String {
    if (delayMinutes <= 0) return scheduledTime
    return try {
        val formatter = DateTimeFormatter.ofPattern("h:mm a", Locale.US)
        val time = LocalTime.parse(scheduledTime.uppercase(), formatter)
        time.plusMinutes(delayMinutes.toLong()).format(formatter)
    } catch (e: Exception) {
        "$scheduledTime (+$delayMinutes)"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    onBackClick: () -> Unit,
    onChatbotClick: () -> Unit = {},
    viewModel: PassengerViewModel = hiltViewModel()
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    // ✅ 2. OBSERVE THE STATE FLOW
    val uiState by viewModel.uiState.collectAsState()

    // ✅ NEW: State trigger for the Re-center button
    var centerCameraTrigger by remember { mutableIntStateOf(0) }

    // ✅ 3. HANDLE LOADING STATE
    // If the data hasn't arrived from the repository yet, show a clean loading spinner.
    if (uiState.isLoading || uiState.currentBusLocation == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = BrandBlue)
        }
        return // Stop drawing the rest of the screen until data is ready!
    }

    // Extract dynamic header data from the ViewModel's state
    val currentStop = uiState.stops.find { it.status == StopStatus.CURRENT }
    val expectedHeaderTime = if (currentStop != null && uiState.isDelayed) {
        calculateExpectedTime(currentStop.scheduledTime, currentStop.delayMinutes)
    } else ""

    val scaffoldState = rememberBottomSheetScaffoldState(
        bottomSheetState = rememberStandardBottomSheetState(initialValue = SheetValue.PartiallyExpanded)
    )

    val isSheetExpanded = scaffoldState.bottomSheetState.targetValue == SheetValue.Expanded
    val mapDimAlpha by animateFloatAsState(
        targetValue = if (isSheetExpanded) 0.6f else 0f,
        animationSpec = tween(300), label = "dim"
    )

    BottomSheetScaffold(
        scaffoldState = scaffoldState,
        sheetPeekHeight = 220.dp,
        sheetContainerColor = Color.White,
        sheetShadowElevation = 40.dp,
        sheetShape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        // ✅ DELETED the floatingActionButton parameter from here
        sheetContent = {
            Column(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.75f)) {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Box(modifier = Modifier.padding(vertical = 12.dp).width(40.dp).height(4.dp).clip(RoundedCornerShape(50)).background(Color(0xFFE2E8F0)))
                }
                LazyColumn(modifier = Modifier.padding(horizontal = 24.dp), contentPadding = PaddingValues(bottom = 32.dp)) {
                    item { Text("Trip Progress", fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, color = TextPrimary, modifier = Modifier.padding(bottom = 24.dp)) }

                    itemsIndexed(uiState.stops) { index, stop ->
                        DashingTimelineItem(stop = stop, isLast = index == uiState.stops.lastIndex)
                    }
                }
            }
        }
    ) { paddingValues ->
        // ✅ Because we use paddingValues here, the bottom of this Box is EXACTLY the top of the sheet!
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            OsmMapView(
                modifier = Modifier.fillMaxSize(),
                currentBusLocation = uiState.currentBusLocation!!,
                routePoints = uiState.routePoints,
                stops = uiState.stops,
                centerTrigger = centerCameraTrigger,
                viewModel = viewModel
            )

            Box(
                modifier = Modifier.fillMaxWidth().height(140.dp)
                    .background(Brush.verticalGradient(colors = listOf(Color.Black.copy(alpha = 0.5f), Color.Transparent)))
            )

            if (mapDimAlpha > 0f) {
                Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = mapDimAlpha)))
            }

            FloatingHeader(
                routeCode = uiState.routeCode,
                destination = uiState.destinationTitle,
                etaMinutes = uiState.currentDelayMinutes,
                expectedTime = expectedHeaderTime,
                onBackClick = onBackClick
            )

            // Re-center FAB
            FloatingActionButton(
                onClick = { centerCameraTrigger++ },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp),
                containerColor = Color.White,
                contentColor = BrandBlue,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = "Center on Bus"
                )
            }

            // Chatbot FAB
            FloatingActionButton(
                onClick = onChatbotClick,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 16.dp, bottom = 16.dp),
                containerColor = BrandBlue,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.SmartToy,
                    contentDescription = "Open Chatbot",
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}


@Composable
fun OsmMapView(
    modifier: Modifier = Modifier,
    currentBusLocation: GeoPoint,
    routePoints: List<GeoPoint>,
    stops: List<BusRouteStop>,
    centerTrigger: Int, // ✅ NEW: Listens for the button click
    viewModel: PassengerViewModel // Added to use lifecycle tracking
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCenteredCamera by remember { mutableStateOf(false) }
    // ✅ Keeps track of the last time the button was clicked
    var lastTrigger by remember { mutableIntStateOf(0) }

    DisposableEffect(Unit) {
        Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        onDispose { }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> viewModel.startLiveTracking()
                Lifecycle.Event.ON_STOP -> viewModel.stopLiveTracking()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val mapView = remember {
        MapView(context).apply {
            // ... (Keep all your existing setup, TileSource, Polylines, and Markers exactly the same) ...

            // TileSource
            val positronTileSource = object : XYTileSource(
                "CartoDB-Positron", 0, 20, 256, ".png",
                arrayOf("https://a.basemaps.cartocdn.com/light_all/", "https://b.basemaps.cartocdn.com/light_all/", "https://c.basemaps.cartocdn.com/light_all/")
            ) {
                override fun getTileURLString(pMapTileIndex: Long): String {
                    return baseUrl + MapTileIndex.getZoom(pMapTileIndex) + "/" + MapTileIndex.getX(pMapTileIndex) + "/" + MapTileIndex.getY(pMapTileIndex) + mImageFilenameEnding
                }
            }
            setTileSource(positronTileSource)
            setMultiTouchControls(true)
            setBuiltInZoomControls(false)

            // ✅ 1. THE BORDER (Darker, thicker line at the bottom)
            val routeBorder = Polyline(this).apply {
                setPoints(routePoints)
                // Solid Dark Blue (No transparency!)
                outlinePaint.color = android.graphics.Color.parseColor("#1E3A8A")
                outlinePaint.strokeWidth = 11f // Thicker
                outlinePaint.strokeCap = android.graphics.Paint.Cap.ROUND
                outlinePaint.strokeJoin = android.graphics.Paint.Join.ROUND
                outlinePaint.isAntiAlias = true
            }
            overlays.add(routeBorder)

            // ✅ 2. THE CORE (Brighter, thinner line on top)
            val routeCore = Polyline(this).apply {
                setPoints(routePoints)
                // Solid Bright Blue (No transparency!)
                outlinePaint.color = android.graphics.Color.parseColor("#3B82F6")
                outlinePaint.strokeWidth = 7f // Thinner, leaving a 4f border on each side
                outlinePaint.strokeCap = android.graphics.Paint.Cap.ROUND
                outlinePaint.strokeJoin = android.graphics.Paint.Join.ROUND
                outlinePaint.isAntiAlias = true
            }
            overlays.add(routeCore)
//            overlays.add(cleanRouteLine)

            // ✅ 3. BEAUTIFUL STOP NAMES (Custom Overlay)
            val textOverlay = object : org.osmdroid.views.overlay.Overlay() {
                override fun draw(c: android.graphics.Canvas, osmv: MapView, shadow: Boolean) {
                    super.draw(c, osmv, shadow)
                    if (shadow) return
                    
                    val textPaint = android.graphics.Paint().apply {
                        color = android.graphics.Color.parseColor("#1E293B") // Dark Slate
                        textSize = 38f // Large, readable text
                        isAntiAlias = true
                        typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                        // White glow effect so it's readable over any map line/feature
                        setShadowLayer(8f, 0f, 0f, android.graphics.Color.WHITE) 
                    }
                    
                    stops.forEach { stop ->
                        val point = android.graphics.Point()
                        osmv.projection.toPixels(stop.location, point)
                        // Draw text slightly to the right of the marker icon
                        c.drawText(stop.name, point.x + 35f, point.y + 12f, textPaint)
                    }
                }
            }
            overlays.add(textOverlay)

            // ✅ 4. STOP ICONS
            stops.forEach { stop ->
                val stopMarker = Marker(this).apply {
                    position = stop.location
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                    title = stop.name
                    setOnMarkerClickListener { _, _ -> true }
                    icon = ContextCompat.getDrawable(context, R.drawable.ic_bus_stop)
                }
                overlays.add(stopMarker)
            }

            val busMarker = Marker(this).apply {
                id = "BUS_MARKER"
                position = currentBusLocation
                setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                title = "Live Bus"
                icon = ContextCompat.getDrawable(context, R.drawable.live_tracking_puck)
            }
            overlays.add(busMarker)
        }
    }

    // ... (Keep your DisposableEffect for lifecycle exactly the same) ...
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
        modifier = modifier,
        factory = { mapView },
        update = { view ->
            // 1. Animate the bus marker smoothly
            val marker = view.overlays.filterIsInstance<Marker>().find { it.id == "BUS_MARKER" }
            if (marker != null) {
                val startPoint = marker.position
                if (startPoint.latitude != currentBusLocation.latitude || startPoint.longitude != currentBusLocation.longitude) {
                    val animator = android.animation.ValueAnimator.ofFloat(0f, 1f)
                    animator.duration = 1500
                    animator.interpolator = android.view.animation.LinearInterpolator()

                    animator.addUpdateListener { animation ->
                        val fraction = animation.animatedFraction
                        val lat = startPoint.latitude + (currentBusLocation.latitude - startPoint.latitude) * fraction
                        val lon = startPoint.longitude + (currentBusLocation.longitude - startPoint.longitude) * fraction

                        marker.position = GeoPoint(lat, lon)
                        view.invalidate()
                    }
                    animator.start()
                }
            }

            // 2. Initial map load camera center
            if (!hasCenteredCamera && routePoints.isNotEmpty()) {
                view.controller.setZoom(19)
                view.controller.animateTo(currentBusLocation)
                hasCenteredCamera = true
            }

            // ✅ 3. THE NEW LOGIC: Manual re-center when the button is clicked!
            if (centerTrigger != lastTrigger) {
                view.controller.animateTo(currentBusLocation)
                lastTrigger = centerTrigger // Save the state so it doesn't get stuck in a loop
            }
        }
    )
}

@Composable
fun FloatingHeader(
    routeCode: String,
    destination: String,
    etaMinutes: Int,
    expectedTime: String,
    onBackClick: () -> Unit
) {
    val hasEta = etaMinutes > 0
    // Treat > 5 min as "delayed" for the header colour
    val isDelayed = hasEta && etaMinutes > 5
    val chipBg    = if (isDelayed) DelayRedBg else Color(0xFFEFF6FF)
    val chipText  = if (isDelayed) DelayRed   else BrandBlue

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp, start = 16.dp, end = 16.dp)
            .shadow(20.dp, RoundedCornerShape(22.dp), spotColor = Color.Black.copy(alpha = 0.12f)),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Back button
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(40.dp).background(Color(0xFFF1F5F9), CircleShape)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Bus badge
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(BrandBlue.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = routeCode,
                    fontSize = if (routeCode.length > 3) 13.sp else 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = BrandBlue,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title + status
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = destination,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier.size(7.dp)
                            .background(if (hasEta) Color(0xFF10B981) else MutedGrey, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = if (hasEta) "Live Tracking" else "Waiting for GPS…",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (hasEta) Color(0xFF10B981) else MutedGrey
                    )
                }
            }

            // ETA chip
            if (hasEta) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .background(chipBg, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (isDelayed) {
                                Text("+", fontSize = 13.sp, fontWeight = FontWeight.Black, color = chipText)
                            }
                            Text(
                                text = "$etaMinutes min",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = chipText
                            )
                        }
                        Text(
                            text = if (isDelayed) "delayed" else "to arrive",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = chipText.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

// Premium timeline item with delay indicators
@Composable
fun DashingTimelineItem(stop: BusRouteStop, isLast: Boolean) {
    val isCurrent  = stop.status == StopStatus.CURRENT
    val isCompleted = stop.status == StopStatus.COMPLETED
    val isUpcoming  = stop.status == StopStatus.UPCOMING
    val hasEta      = stop.delayMinutes > 0
    // > 5 min = visually "delayed"
    val isDelayed   = isUpcoming && stop.delayMinutes > 5

    val dotColor  = if (isCompleted) BrandBlue else MutedGrey.copy(alpha = 0.35f)
    val lineColor = if (isCompleted) BrandBlue.copy(alpha = 0.35f)
                    else if (isDelayed) DelayRed.copy(alpha = 0.25f)
                    else MutedGrey.copy(alpha = 0.18f)

    Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {
        // Left: dot + line
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(52.dp)
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(36.dp)) {
                when {
                    isCurrent  -> PulsingBusNode(color = BrandBlue)
                    isCompleted -> Box(
                        modifier = Modifier.size(12.dp).clip(CircleShape).background(BrandBlue)
                    )
                    else -> Box(
                        modifier = Modifier.size(9.dp).clip(CircleShape)
                            .background(if (isDelayed) DelayRed.copy(alpha = 0.4f) else MutedGrey.copy(alpha = 0.35f))
                    )
                }
            }
            if (!isLast) {
                Box(
                    modifier = Modifier.width(2.dp).weight(1f).background(lineColor)
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Box(modifier = Modifier.weight(1f).padding(bottom = 22.dp)) {
            if (isCurrent) CurrentStopCard(stop) else StandardStopInfo(stop)
        }
    }
}

// Current stop — rich gradient card
@Composable
fun CurrentStopCard(stop: BusRouteStop) {
    val etaMins = stop.delayMinutes
    val arrivalText = if (etaMins <= 1) "Arriving now" else "~$etaMins min away"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.horizontalGradient(listOf(BrandBlue, Color(0xFF4F46E5))),
                RoundedCornerShape(16.dp)
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "🚌  Bus is here",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.75f),
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = stop.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.18f), RoundedCornerShape(10.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = arrivalText,
                    color = Color.White,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// Upcoming / completed stop — bigger fonts, delay badge
@Composable
fun StandardStopInfo(stop: BusRouteStop) {
    val isCompleted = stop.status == StopStatus.COMPLETED
    val hasEta      = stop.delayMinutes > 0
    val isDelayed   = !isCompleted && stop.delayMinutes > 5

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = stop.name,
                fontSize = 17.sp,
                fontWeight = if (isCompleted) FontWeight.Normal else FontWeight.SemiBold,
                color = if (isCompleted) TextSecondary else TextPrimary
            )
            if (isDelayed) {
                Spacer(modifier = Modifier.height(3.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = DelayRed,
                        modifier = Modifier.size(11.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "Running late",
                        fontSize = 11.sp,
                        color = DelayRed,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        Spacer(modifier = Modifier.width(10.dp))
        when {
            isCompleted -> Text(
                text = "✓ Passed",
                fontSize = 12.sp,
                color = BrandBlue.copy(alpha = 0.55f),
                fontWeight = FontWeight.SemiBold
            )
            hasEta -> Box(
                modifier = Modifier
                    .background(
                        if (isDelayed) DelayRedBg else Color(0xFFEFF6FF),
                        RoundedCornerShape(10.dp)
                    )
                    .padding(horizontal = 11.dp, vertical = 6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isDelayed) {
                        Text(
                            text = "+",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            color = DelayRed
                        )
                    }
                    Text(
                        text = "${stop.delayMinutes} min",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDelayed) DelayRed else BrandBlue
                    )
                    if (isDelayed) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "late",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = DelayRed.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PulsingBusNode(color: Color = BrandBlue) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 1.4f,
        animationSpec = infiniteRepeatable(tween(1000), RepeatMode.Reverse), label = "scale"
    )

    Box(contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.size(32.dp).scale(scale).background(color.copy(alpha = 0.3f), CircleShape))
        Box(modifier = Modifier.size(24.dp).background(color, CircleShape), contentAlignment = Alignment.Center) {
            Icon(Icons.Default.DirectionsBus, null, tint = Color.White, modifier = Modifier.size(16.dp))
        }
    }
}