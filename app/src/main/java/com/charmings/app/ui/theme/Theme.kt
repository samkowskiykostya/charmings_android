package com.charmings.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.charmings.app.R

/*
 * "Blueberry & milk" — a storybook sticker album.
 * Plum ink instead of grey/black, blueberry for actions, butter + bubblegum as tiny accents.
 */
val Milk = Color(0xFFFFF6FA)        // page background
val Lilac = Color(0xFFEEE7FF)       // quiet surfaces, chips, tracks
val Blueberry = Color(0xFF6B4FD8)   // primary action, progress
val Plum = Color(0xFF3D2C55)        // ink
val PlumSoft = Color(0xFF6F6188)    // secondary text (≥4.5:1 on Milk)
val Bubblegum = Color(0xFFFF8DB4)   // accent
val Butter = Color(0xFFFFD36E)      // stars, "new"
val Mint = Color(0xFF8EDCC3)
val White = Color(0xFFFFFFFF)

@OptIn(ExperimentalTextApi::class)
private fun nunito(weight: Int) = Font(
    R.font.nunito,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight))
)

val Nunito = FontFamily(nunito(400), nunito(600), nunito(700), nunito(800), nunito(900))

private val base = TextStyle(fontFamily = Nunito, color = Plum)

val CharmingsTypography = Typography(
    displayLarge = base.copy(fontWeight = FontWeight.Black, fontSize = 40.sp, lineHeight = 44.sp),
    headlineLarge = base.copy(fontWeight = FontWeight.Black, fontSize = 32.sp, lineHeight = 38.sp),
    headlineMedium = base.copy(fontWeight = FontWeight.Black, fontSize = 26.sp, lineHeight = 32.sp),
    titleLarge = base.copy(fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = base.copy(fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, lineHeight = 22.sp),
    titleSmall = base.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 20.sp),
    bodyLarge = base.copy(fontWeight = FontWeight.Normal, fontSize = 17.sp, lineHeight = 26.sp),
    bodyMedium = base.copy(fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.sp),
    bodySmall = base.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp, lineHeight = 18.sp, color = PlumSoft),
    labelLarge = base.copy(fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, lineHeight = 20.sp),
    labelMedium = base.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 16.sp),
)

private val ColorScheme = lightColorScheme(
    primary = Blueberry,
    onPrimary = White,
    primaryContainer = Lilac,
    onPrimaryContainer = Plum,
    secondary = Bubblegum,
    onSecondary = Plum,
    tertiary = Butter,
    onTertiary = Plum,
    background = Milk,
    onBackground = Plum,
    surface = White,
    onSurface = Plum,
    surfaceVariant = Lilac,
    onSurfaceVariant = PlumSoft,
    outline = PlumSoft
)

@Composable
fun CharmingsTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = ColorScheme, typography = CharmingsTypography, content = content)
}
