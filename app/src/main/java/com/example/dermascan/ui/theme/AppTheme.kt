package com.example.dermascan.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun DermascanTheme(
    darkMode: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val scheme = if (darkMode) {
        darkColorScheme(
            primary = Teal,
            secondary = Cyan,
            background = androidx.compose.ui.graphics.Color(0xFF0F172A),
            surface = androidx.compose.ui.graphics.Color(0xFF111827),
        )
    } else {
        lightColorScheme(
            primary = Teal,
            secondary = Cyan,
            background = LightBackground,
            surface = androidx.compose.ui.graphics.Color.White,
        )
    }

    MaterialTheme(colorScheme = scheme) {
        Surface(modifier = modifier, color = MaterialTheme.colorScheme.background) {
            content()
        }
    }
}
