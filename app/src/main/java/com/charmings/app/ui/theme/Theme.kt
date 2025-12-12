package com.charmings.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Primary = Color(0xFF7444C0)
val Secondary = Color(0xFF5636B8)
val DarkSecondary = Color(0xFF4c2fa3)
val LightPrimary = Color(0xFFe8dff6)
val White = Color(0xFFFFFFFF)
val Gray = Color(0xFF757E90)
val DarkGray = Color(0xFF363636)
val Black = Color(0xFF000000)
val LightYellow = Color(0xFFF5F9AA)

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    secondary = Secondary,
    tertiary = DarkSecondary,
    background = LightPrimary,
    surface = White,
    onPrimary = White,
    onSecondary = White,
    onTertiary = White,
    onBackground = DarkGray,
    onSurface = DarkGray
)

@Composable
fun CharmingsTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content
    )
}
