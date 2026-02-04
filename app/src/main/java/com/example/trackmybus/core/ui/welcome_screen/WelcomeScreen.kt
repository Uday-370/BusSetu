package com.example.trackmybus.core.ui.welcome_screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.trackmybus.R
import com.example.trackmybus.core.ui.components.TMBRoleButton
import com.example.trackmybus.core.ui.components.TMBTopBar
import com.example.trackmybus.ui.theme.TextPrimary

val DriverGradient = listOf(Color(0xFF6CB6FF), Color(0xFF4A90E2))
val UserGradient = listOf(Color(0xFF81E29C), Color(0xFF57C876))

@Composable
fun WelcomeScreen(
    onDriverClick: () -> Unit,
    onUserClick: () -> Unit
){
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White,
        topBar = {
            TMBTopBar()
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(bottom = 40.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(38.dp))

                Image(
                    painter = painterResource(id = R.drawable.driver_passanger_image),
                    contentDescription = "welcome Illustration",
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .height(220.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(
                    text = "ridhiyam !",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                TMBRoleButton(
                    text = "Driver",
                    imageRes = R.drawable.driver_image,
                    gradientColors = DriverGradient,
                    onClick = onDriverClick
                )

                Spacer(modifier = Modifier.height(24.dp))

                TMBRoleButton(
                    text = "User",
                    imageRes = R.drawable.user_image,
                    gradientColors = UserGradient,
                    onClick = onUserClick
                )
            }
        }

    }
}

@Preview(showBackground = true)
@Composable
fun PreviewWelcomeScreen() {
    WelcomeScreen(
        onDriverClick = {},
        onUserClick = {}
    )
}
