package com.example.bussetu.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import com.example.bussetu.ui.theme.TextPrimary
import com.example.trackmybus.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TMBTopBar(
    modifier: Modifier = Modifier,
    title: String? = null,
    showBackButton: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    // Custom slot if you need something complex in the center besides text or the default logo
    titleContent: (@Composable () -> Unit)? = null,
    // NEW PARAMETER: Allows passing a custom icon (like Menu) for the left side
    navigationIcon: (@Composable () -> Unit)? = null
) {
    CenterAlignedTopAppBar(
        modifier = modifier,
        title = {
            // Logic to determine what to display in the center
            if (titleContent != null) {
                // 1. Priority: Custom content if provided
                titleContent()
            } else if (title != null) {
                // 2. Fallback: Simple text title if provided
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = TextPrimary
                )
            } else {
                // 3. Default: Show the Logo if no title or content is provided
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "TrackMyBus Logo",
                    // Use a specific height for app bar icons, don't fillMaxSize
                    modifier = Modifier.height(50.dp)
                )
            }
        },
        navigationIcon = {
            // UPDATED LOGIC:
            if (navigationIcon != null) {
                // Priority 1: Show the custom icon passed (e.g., Menu)
                navigationIcon()
            } else if (showBackButton && onBackClick != null) {
                // Priority 2: Show the Back Arrow
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
            }
        },
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = Color.White,
            titleContentColor = TextPrimary,
            navigationIconContentColor = TextPrimary
        )
    )
}

// ================= PREVIEWS =================

@Preview(showBackground = true, name = "Scenario 1: Logo Only (Login Screen)")
@Composable
fun PreviewTMBTopBarLogo() {
    TMBTopBar()
}

@Preview(showBackground = true, name = "Scenario 2: Text and Back (Dashboard)")
@Composable
fun PreviewTMBTopBarTextAndBack() {
    TMBTopBar(
        title = "Driver Dashboard",
        showBackButton = true,
        onBackClick = {}
    )
}

@Preview(showBackground = true, name = "Scenario 3: With Menu Icon (User Dashboard)")
@Composable
fun PreviewTMBTopBarMenu() {
    TMBTopBar(
        title = "TrackMyBus",
        navigationIcon = {
            IconButton(onClick = {}) {
                Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
        }
    )
}