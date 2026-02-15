package com.example.bussetu.core.ui.loginscreen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import com.example.bussetu.core.ui.components.TMBButton
import com.example.bussetu.core.ui.components.TMBTextField
import com.example.bussetu.core.ui.components.TMBTopBar
import com.example.bussetu.ui.theme.BrandBlue
import com.example.bussetu.ui.theme.TextPrimary
import com.example.trackmybus.R

@Composable
fun LoginScreen(
    onLoginClick: () -> Unit
//    onSignUpClick: () -> Unit,
//    onForgotPasswordClick: () -> Unit
) {
    var phoneNumber by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            // This will show the centered Logo by default
            TMBTopBar(
                titleContent = {
                    Text(
                        text = "BusSetu",
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandBlue
                    )
                }
            )
        }
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(bottom = 24.dp) // Add some bottom padding
        ) {

            item {
                // Add some space between the top bar and the illustration
                Spacer(modifier = Modifier.height(30.dp))

                // --- Illustration ---
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.driver_passanger_image),
                        contentDescription = "Bus Illustration",
                        modifier = Modifier
                            .fillMaxSize()
                            .height(200.dp), // Adjust height as needed
                        contentScale = ContentScale.Fit // Ensure the image fits within the bounds
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                // --- Title ---
                Text(
                    text = "Login here",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // --- Text Fields ---
                TMBTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    placeholder = "Phone Number",
                    icon = Icons.Default.Phone,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                TMBTextField(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "Password",
                    icon = Icons.Default.Lock,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // --- Forgot Password ---
//                Box(
//                    modifier = Modifier.fillMaxWidth(),
//                    contentAlignment = Alignment.CenterEnd
//                ) {
//                    Text(
//                        text = "Forgot Password?",
//                        color = TextSecondary,
//                        fontSize = 14.sp,
//                        modifier = Modifier.clickable { onForgotPasswordClick() }
//                    )
//                }
//
//                Spacer(modifier = Modifier.height(24.dp))

                // --- Login Button ---
                TMBButton(
                    text = "Login",
                    onClick = onLoginClick,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // --- Sign Up Row ---
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    Text(
//                        text = "Don't have an account? ",
//                        color = TextSecondary,
//                        fontSize = 14.sp
//                    )
//                    Text(
//                        text = "Sign Up",
//                        color = BrandBlue,
//                        fontWeight = FontWeight.Bold,
//                        fontSize = 14.sp,
//                        modifier = Modifier.clickable { onSignUpClick() }
//                    )
//                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewLoginScreen() {
    LoginScreen(
        onLoginClick = {},
//        onSignUpClick = {},
//        onForgotPasswordClick = {}
    )
}