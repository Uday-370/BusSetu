package com.example.bussetu.feature_map.presentation.mapscreen

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.util.MapTileIndex
import org.osmdroid.views.MapView

@Composable
fun OsmMapView(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // 1. Initialize Configuration
    // Essential for loading tiles and caching
    val osmConfig = remember {
        Configuration.getInstance().load(
            context,
            context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE)
        )
    }

    // 2. DEFINE THE "DASHING" MAP STYLE (CartoDB Positron)
    // This removes the clutter and gives a sleek, silver/grey look.
    val positronTileSource = object : XYTileSource(
        "CartoDB-Positron",
        0, 20, 256, ".png",
        arrayOf(
            "https://a.basemaps.cartocdn.com/light_all/",
            "https://b.basemaps.cartocdn.com/light_all/",
            "https://c.basemaps.cartocdn.com/light_all/"
        )
    ) {
        override fun getTileURLString(pMapTileIndex: Long): String {
            return baseUrl + MapTileIndex.getZoom(pMapTileIndex) + "/" +
                    MapTileIndex.getX(pMapTileIndex) + "/" +
                    MapTileIndex.getY(pMapTileIndex) + mImageFilenameEnding
        }
    }

    AndroidView(
        modifier = modifier,
        factory = { ctx ->
            MapView(ctx).apply {
                // 3. APPLY THE CUSTOM STYLE
                setTileSource(positronTileSource)

                // Enable touch controls
                setMultiTouchControls(true)

                // 4. Set View (Start at Pune)
                controller.setZoom(15.0)
                val startPoint = GeoPoint(18.5204, 73.8567)
                controller.setCenter(startPoint)
            }
        },
        update = { mapView ->
            // Update logic here if needed
        }
    )
}