package com.example.freshtrack.ui.components


import androidx.compose.foundation.Image
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.freshtrack.R

@Composable
fun BackgroundImage(isDarkMode: Boolean) {
    val backgroundRes = if (isDarkMode) {
        R.drawable.dark_background_pattern // Make sure this is your dark image
    } else {
        R.drawable.background_pattern
    }

    Image(
        painter = painterResource(id = backgroundRes),
        contentDescription = null,
        modifier = Modifier.fillMaxSize(),
        contentScale = ContentScale.Crop
    )
}
