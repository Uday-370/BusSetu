package com.example.bussetu.core.presentation.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.TextStyle

// Assuming these are defined in your theme
// import com.example.trackmybus.ui.theme.TextSecondary
// import com.example.trackmybus.ui.theme.BrandBlue

@Composable
fun TMBTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    textStyle: TextStyle = LocalTextStyle.current,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(text = placeholder) }, // Use color = TextSecondary if imported
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
        },
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color(0xFFF5F5F5),
            focusedBorderColor = Color.Blue, // Use BrandBlue if imported
            unfocusedBorderColor = Color(0xFFE0E0E0)
        ),
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        textStyle = textStyle,
        singleLine = true
    )
}

