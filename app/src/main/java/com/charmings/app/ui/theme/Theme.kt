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
 * "Cocoa & honey" — a cosy storybook: cream paper, cocoa ink, apricot and honey for warmth,
 * rose blush and sage as tiny accents. No pure black, no pure white, no cold greys.
 */
val Cream = Color(0xFFFFF4E6)       // page background (warm paper)
val Oat = Color(0xFFF6E4CC)         // quiet surfaces, chips, tracks
val Apricot = Color(0xFFF4A47A)     // primary action, progress (cocoa text on top)
val ApricotDeep = Color(0xFFD9805A) // pressed edge of chunky buttons, strong accents
val Cocoa = Color(0xFF4A2F27)       // ink
val CocoaSoft = Color(0xFF7D5E50)   // secondary text (≥4.5:1 on Cream)
val Rose = Color(0xFFF2A7A7)        // blush accent
val Honey = Color(0xFFFFCB69)       // stars, "new"
val Sage = Color(0xFFB5CDA3)
val Paper = Color(0xFFFFFBF5)       // cards (warm white)
val Stitch = Color(0xFFEBCFAF)      // dashed "felt patch" stitching

@OptIn(ExperimentalTextApi::class)
private fun variable(res: Int, weight: Int) = Font(
    res,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight))
)

/* Lora: a bookish serif for headings — gives the storybook voice. */
val Lora = FontFamily(listOf(400, 500, 600, 700).map { variable(R.font.lora, it) })

/* Golos Text: a calm grotesque drawn for Cyrillic, for body copy and labels. */
val Golos = FontFamily(listOf(400, 500, 600, 700).map { variable(R.font.golos_text, it) })

private val serif = TextStyle(fontFamily = Lora, color = Cocoa)
private val sans = TextStyle(fontFamily = Golos, color = Cocoa)

val CharmingsTypography = Typography(
    displayLarge = serif.copy(fontWeight = FontWeight.SemiBold, fontSize = 40.sp, lineHeight = 46.sp, letterSpacing = (-0.5).sp),
    headlineLarge = serif.copy(fontWeight = FontWeight.SemiBold, fontSize = 30.sp, lineHeight = 36.sp, letterSpacing = (-0.3).sp),
    headlineMedium = serif.copy(fontWeight = FontWeight.SemiBold, fontSize = 25.sp, lineHeight = 31.sp, letterSpacing = (-0.2).sp),
    titleLarge = serif.copy(fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = serif.copy(fontWeight = FontWeight.SemiBold, fontSize = 17.sp, lineHeight = 22.sp),
    titleSmall = sans.copy(fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 20.sp),
    bodyLarge = sans.copy(fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = sans.copy(fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.sp),
    bodySmall = sans.copy(fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp, color = CocoaSoft),
    labelLarge = sans.copy(fontWeight = FontWeight.SemiBold, fontSize = 15.sp, lineHeight = 20.sp, letterSpacing = 0.1.sp),
    labelMedium = sans.copy(fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.2.sp),
)

private val ColorScheme = lightColorScheme(
    primary = Apricot,
    onPrimary = Cocoa,
    primaryContainer = Oat,
    onPrimaryContainer = Cocoa,
    secondary = Rose,
    onSecondary = Cocoa,
    tertiary = Honey,
    onTertiary = Cocoa,
    background = Cream,
    onBackground = Cocoa,
    surface = Paper,
    onSurface = Cocoa,
    surfaceVariant = Oat,
    onSurfaceVariant = CocoaSoft,
    outline = CocoaSoft
)

@Composable
fun CharmingsTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = ColorScheme, typography = CharmingsTypography, content = content)
}
