package com.example.bussetu.core.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Note: You might need to import your R file if you use real resources in the preview
// import com.example.trackmybus.R

/**
 * A custom button component used on the Welcome Screen for selecting user roles (e.g., Driver, User).
 * It features a horizontal gradient background, an illustration, and large text.
 *
 * @param text The text title of the role.
 * @param imageRes The drawable resource ID for the role illustration.
 * @param gradientColors A list of colors to create the horizontal gradient background.
 * @param onClick The action to perform when the button is clicked.
 * @param modifier Modifier to be applied to the button container.
 */
@Composable
fun TMBRoleButton(
    text: String,
    @DrawableRes imageRes: Int,
    gradientColors: List<Color>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(110.dp) // Fixed height for consistency
            .clip(RoundedCornerShape(20.dp)) // Rounded corners
            .background(Brush.horizontalGradient(gradientColors))
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Role Illustration
            // Note: Ensure the provided imageRes fits well within this size.
            Image(
                painter = painterResource(id = imageRes),
                contentDescription = null, // Decorative image
                modifier = Modifier.size(90.dp),
                contentScale = ContentScale.Fit
            )

            Spacer(modifier = Modifier.width(32.dp))

            // Role Text
            Text(
                text = text,
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun TMBRoleButtonPreview() {
    // Previewing with dummy data.
    // Replace 'android.R.drawable.ic_menu_camera' with one of your actual drawable IDs to see the real image.
    TMBRoleButton(
        text = "Preview Role",
        imageRes = android.R.drawable.ic_menu_camera,
        gradientColors = listOf(Color(0xFF6CB6FF), Color(0xFF4A90E2)),
        onClick = {}
    )
}