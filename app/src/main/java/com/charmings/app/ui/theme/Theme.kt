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
private fun nunito(weight: Int) = Font(
    R.font.nunito,
    weight = FontWeight(weight),
    variationSettings = FontVariation.Settings(FontVariation.weight(weight))
)

val Nunito = FontFamily(nunito(400), nunito(600), nunito(700), nunito(800), nunito(900))

private val base = TextStyle(fontFamily = Nunito, color = Cocoa)

val CharmingsTypography = Typography(
    displayLarge = base.copy(fontWeight = FontWeight.Black, fontSize = 40.sp, lineHeight = 44.sp),
    headlineLarge = base.copy(fontWeight = FontWeight.Black, fontSize = 32.sp, lineHeight = 38.sp),
    headlineMedium = base.copy(fontWeight = FontWeight.Black, fontSize = 26.sp, lineHeight = 32.sp),
    titleLarge = base.copy(fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, lineHeight = 26.sp),
    titleMedium = base.copy(fontWeight = FontWeight.ExtraBold, fontSize = 17.sp, lineHeight = 22.sp),
    titleSmall = base.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp, lineHeight = 20.sp),
    bodyLarge = base.copy(fontWeight = FontWeight.Normal, fontSize = 17.sp, lineHeight = 26.sp),
    bodyMedium = base.copy(fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.sp),
    bodySmall = base.copy(fontWeight = FontWeight.SemiBold, fontSize = 13.sp, lineHeight = 18.sp, color = CocoaSoft),
    labelLarge = base.copy(fontWeight = FontWeight.ExtraBold, fontSize = 15.sp, lineHeight = 20.sp),
    labelMedium = base.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp, lineHeight = 16.sp),
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
