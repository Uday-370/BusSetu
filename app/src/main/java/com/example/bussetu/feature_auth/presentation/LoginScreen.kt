package com.example.bussetu.feature_auth.presentation

import android.app.Activity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.bussetu.core.presentation.components.TMBTextField
import com.example.bussetu.core.ui.theme.BrandBlue
import com.example.bussetu.core.ui.theme.TextPrimary
import com.example.bussetu.core.ui.theme.TextSecondary

@Composable
fun LoginScreen(
    onLoginClick: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    // Input States
    var userName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // ✅ NEW: Error States for inline validation
    var userNameError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }

    // ✅ NEW: Password Visibility State
    var passwordVisible by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val scrollState = rememberScrollState()

    val state by viewModel.loginState.collectAsState()

    LaunchedEffect(state) {
        when (state) {
            is LoginState.Success -> {
                onLoginClick()
            }
            is LoginState.Error -> {
                // We ONLY use the Snackbar for actual server/backend errors now
                val errorMessage = (state as LoginState.Error).message
                snackbarHostState.showSnackbar(errorMessage)
                viewModel.resetState()
            }
            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        containerColor = Color(0xFFF5F7FA)
    ) { paddingValues ->

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            // --- BACKGROUND HEADER ---
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.35f)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(BrandBlue, Color(0xFF2563EB))
                        ),
                        shape = RoundedCornerShape(bottomEnd = 60.dp)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .offset(x = (-50).dp, y = (-50).dp)
                        .size(200.dp)
                        .background(Color.White.copy(alpha = 0.1f), CircleShape)
                )
            }

            // --- SCROLLABLE CONTENT ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = paddingValues.calculateBottomPadding())
                    .verticalScroll(scrollState)
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 80.dp, start = 30.dp, end = 30.dp)
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

                        // --- USERNAME INPUT ---
                        TMBTextField(
                            value = userName,
                            onValueChange = {
                                userName = it
                                userNameError = false // Clear error when typing
                            },
                            placeholder = "User Name",
                            icon = Icons.Default.Person,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                            modifier = Modifier.fillMaxWidth()
                        )
                        // ✅ NEW: Inline Username Error
                        if (userNameError) {
                            Text(
                                text = "Username cannot be empty",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // --- PASSWORD INPUT ---
                        TMBTextField(
                            value = password,
                            onValueChange = {
                                password = it
                                passwordError = false // Clear error when typing
                            },
                            placeholder = "Password",
                            icon = Icons.Default.Lock,
                            // ✅ NEW: Toggles dots vs plain text
                            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            // ✅ NEW: Adds the clickable Eye icon
                            trailingIcon = {
                                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                    Icon(imageVector = image, contentDescription = "Toggle password visibility", tint = TextSecondary)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                        // ✅ NEW: Inline Password Error
                        if (passwordError) {
                            Text(
                                text = "Password cannot be empty",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // --- LOGIN BUTTON ---
                        Button(
                            onClick = {
                                // ✅ NEW: Local validation before calling ViewModel
                                userNameError = userName.isBlank()
                                passwordError = password.isBlank()

                                if (!userNameError && !passwordError) {
                                    viewModel.login(userName, password)
                                }
                            },
                            enabled = state !is LoginState.Loading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .shadow(8.dp, RoundedCornerShape(14.dp), spotColor = BrandBlue.copy(alpha = 0.5f)),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BrandBlue,
                                disabledContainerColor = BrandBlue.copy(alpha = 0.7f),
                                contentColor = Color.White,
                                disabledContentColor = Color.White
                            )
                        ) {
                            AnimatedContent(
                                targetState = state is LoginState.Loading,
                                transitionSpec = {
                                    fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                                },
                                label = "login_button_animation"
                            ) { isLoading ->
                                if (isLoading) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(24.dp),
                                            color = Color.White,
                                            strokeWidth = 2.5.dp
                                        )
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Text(
                                            text = "LOGGING IN...",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
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
                        }
                    }
                }

                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}