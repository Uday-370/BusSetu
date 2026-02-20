package com.example.bussetu.core.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.bussetu.core.ui.theme.BrandBlue

@Composable
fun TMBButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = BrandBlue, // Default to Blue, can pass Green
    isEnabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = isEnabled,
        modifier = modifier
            .fillMaxWidth() // Matches design (Full Width)
            .height(56.dp), // Matches design (Tall button)
        shape = RoundedCornerShape(12.dp), // Soft rounded corners
        colors = ButtonDefaults.buttonColors(
            containerColor = backgroundColor,
            disabledContainerColor = Color.LightGray
        )
    ) {
        Text(text = text) // Typography will be picked up from Theme
    }
}

