package com.example.bussetu.feature_auth.presentation

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.bussetu.core.presentation.components.TMBTextField
import com.example.bussetu.core.ui.theme.BrandBlue
import com.example.bussetu.core.ui.theme.TextPrimary
import com.example.bussetu.core.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    onLoginClick: () -> Unit
) {
    // 1. FIX: Force Status Bar Icons to be White (Because header is Blue)
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // False = Dark Background -> System paints White icons
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    // --- State ---
    var userName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color(0xFFF5F7FA)
    ) { paddingValues ->

        // Root Box ignores paddingValues so background fills the status bar
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // --- 2. BACKGROUND HEADER (Fills Top Edge) ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.35f) // Top 35%
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(BrandBlue, Color(0xFF2563EB))
                        ),
                        shape = RoundedCornerShape(bottomEnd = 60.dp)
                    )
            ) {
                // Decor
                Box(
                    modifier = Modifier
                        .offset(x = (-50).dp, y = (-50).dp)
                        .size(200.dp)
                        .background(Color.White.copy(alpha = 0.1f), CircleShape)
                )
            }

            // --- 3. SCROLLABLE CONTENT ---
            // We apply paddingValues HERE so content doesn't overlap status bar
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding()) // Handle bottom nav if any
                    .verticalScroll(scrollState)
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // Header Text
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 80.dp, start = 30.dp, end = 30.dp) // Top padding prevents overlap
                ) {
                    Text(
                        text = "BusSetu",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Sign In",
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }

                Spacer(modifier = Modifier.height(40.dp))

                // Floating Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .shadow(16.dp, RoundedCornerShape(24.dp), spotColor = BrandBlue.copy(alpha = 0.2f)),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = "Welcome Captain!",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "Please enter your details",
                            fontSize = 14.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )

                        // Input: User Name
                        TMBTextField(
                            value = userName,
                            onValueChange = { userName = it },
                            placeholder = "User Name",
                            icon = Icons.Default.Person,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Input: Password
                        TMBTextField(
                            value = password,
                            onValueChange = { password = it },
                            placeholder = "Password",
                            icon = Icons.Default.Lock,
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Login Button
                        Button(
                            onClick = {
                                if (userName.isBlank()) {
                                    scope.launch { snackbarHostState.showSnackbar("Enter user name") }
                                } else if (password.isBlank()) {
                                    scope.launch { snackbarHostState.showSnackbar("Enter password") }
                                } else {
                                    onLoginClick()
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .shadow(8.dp, RoundedCornerShape(14.dp), spotColor = BrandBlue.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BrandBlue)
                        ) {
                            Text(
                                text = "LOGIN",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = Color.White)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLoginNew() {
    LoginScreen({})
}