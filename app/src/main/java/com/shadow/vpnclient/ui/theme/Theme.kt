package com.shadow.vpnclient.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = ShadowBlack,
    secondary = NeonCyanDim,
    background = ShadowBlack,
    surface = ShadowSurface,
    error = DangerRed,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun ShadowVPNTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkScheme,
        typography = MaterialTheme.typography,
        content = content
    )
}
