package com.example.bussetu.core.ui.welcomescreen

import android.app.Activity
import android.net.Uri
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.view.WindowCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.example.bussetu.core.ui.theme.BrandBlue
import com.example.trackmybus.R // Verify R import

// --- GRADIENTS ---
val DriverGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6))
)
val PassengerGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF0F766E), Color(0xFF14B8A6))
)

// The background color of the screen
val ScreenBackgroundColor = Color(0xFFFAFAFA)

@Composable
fun WelcomeScreen(
    onDriverClick: () -> Unit,
    onUserClick: () -> Unit
) {
    // 1. FORCE DARK STATUS BAR ICONS (Fixes "Invisible Battery")
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // This tells the system: "My background is light, so make the icons DARK"
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ScreenBackgroundColor)
            .systemBarsPadding() // 2. PREVENTS OVERLAP (Pushes content below status bar)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // HEADER
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(BrandBlue)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "BusSetu",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                letterSpacing = (-0.5).sp
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        // HERO VIDEO AREA
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            // Shadow Blob
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .shadow(elevation = 24.dp, shape = CircleShape, spotColor = Color.LightGray.copy(alpha = 0.5f))
            )

            // Video Wrapper
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.9f)
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(24.dp))
            ) {
                LoopingVideoPlayer(
                    videoResId = R.raw.bus_intro2,
                    modifier = Modifier.fillMaxSize()
                )

                // Vignette Overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    ScreenBackgroundColor
                                ),
                                radius = 700f
                            )
                        )
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))

        // TITLE
        Text(
            text = "Welcome !",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.Black
        )
        Text(
            text = "Select your role to continue",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
        )

        // CARDS
        PremiumRoleCard(
            title = "Driver",
            subtitle = "Access route management",
            icon = Icons.Default.DirectionsBus,
            gradient = DriverGradient,
            onClick = onDriverClick
        )

        Spacer(modifier = Modifier.height(16.dp))

        PremiumRoleCard(
            title = "Passenger",
            subtitle = "Find buses & track trips",
            icon = Icons.Default.Person,
            gradient = PassengerGradient,
            onClick = onUserClick
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

// --- COMPONENTS REMAIN SAME ---

@OptIn(UnstableApi::class)
@Composable
fun LoopingVideoPlayer(videoResId: Int, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            val mediaItem = MediaItem.fromUri(Uri.parse("android.resource://${context.packageName}/$videoResId"))
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true
            repeatMode = Player.REPEAT_MODE_ALL
            volume = 0f
        }
    }
    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }
    AndroidView(
        factory = {
            PlayerView(context).apply {
                player = exoPlayer
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                background = null
            }
        },
        modifier = modifier
    )
}

@Composable
fun PremiumRoleCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradient: Brush,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(90.dp)
            .clickable { onClick() }
            .shadow(10.dp, RoundedCornerShape(20.dp), spotColor = Color.Black.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 20.dp, y = (-20).dp)
                    .size(80.dp)
                    .background(Color.White.copy(alpha = 0.1f), CircleShape)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = Color.White, modifier = Modifier.size(26.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(subtitle, fontSize = 13.sp, color = Color.White.copy(alpha = 0.8f))
                }
                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.White.copy(alpha = 0.8f))
            }
        }
    }
}