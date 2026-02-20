package com.example.bussetu.core.presentation.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun TMBCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        onClick = onClick ?: {},
        enabled = onClick != null,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp), // Matches the "Bus 12" card style
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp) // Subtle shadow
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            content()
        }
    }
}

