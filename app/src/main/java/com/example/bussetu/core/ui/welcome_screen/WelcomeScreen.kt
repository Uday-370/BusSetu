package com.example.bussetu.core.ui.welcomescreen

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.bussetu.ui.theme.TextPrimary
import com.example.bussetu.ui.theme.TextSecondary
import com.example.trackmybus.R

// Define Gradients for a "Premium" look
val DriverGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF4285F4), Color(0xFF1976D2)) // Blue-ish
)
val UserGradient = Brush.horizontalGradient(
    colors = listOf(Color(0xFF4CAF50), Color(0xFF388E3C)) // Green-ish
)

@Composable
fun WelcomeScreen(
    onDriverClick: () -> Unit,
    onUserClick: () -> Unit
) {
    Scaffold(
        containerColor = Color.White
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.weight(0.5f))

            // 1. HERO ILLUSTRATION
            // Using a Box to hold the image (replace icon with your drawable later)

                // Ideally, replace this Icon with:
                // Image(painter = painterResource(id = R.drawable.welcome_hero), ...)
                Image(
                    painter = painterResource(id = R.drawable.driver_passanger_image),
                    contentDescription = "Bus Hero",
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size(320.dp)
                )



            Spacer(modifier = Modifier.height(30.dp))

            // 2. WELCOME TEXT
            Text(
                text = "Welcome to TrackMyBus !",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(50.dp))

            Text(
                text = "Choose your role to get started with\nseamless tracking.",
                fontSize = 16.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )

            Spacer(modifier = Modifier.weight(1f))

            // 3. ROLE SELECTION CARDS

            // Driver Card
            RoleSelectionCard(
                title = "I am a Driver",
                subtitle = "Manage trips",
                icon = Icons.Default.DirectionsBus, // Replace with R.drawable.ic_driver
                gradient = DriverGradient,
                onClick = onDriverClick
            )

            Spacer(modifier = Modifier.height(16.dp))

            // User Card
            RoleSelectionCard(
                title = "I am a Passenger",
                subtitle = "Track your bus in real time",
                icon = Icons.Default.Person, // Replace with R.drawable.ic_passenger
                gradient = UserGradient,
                onClick = onUserClick
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// --- REUSABLE FANCY CARD COMPONENT ---
@Composable
fun RoleSelectionCard(
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
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        // Gradient Background
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Icon Box
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Texts
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = subtitle,
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }

                // Optional: Arrow icon to indicate "Go"
                // Icon(Icons.Default.ChevronRight, null, tint = Color.White)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewWelcomeScreen() {
    WelcomeScreen(onDriverClick = {}, onUserClick = {})
}