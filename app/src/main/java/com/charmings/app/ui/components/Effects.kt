package com.charmings.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.rotate
import com.charmings.app.data.model.AUTUMN
import com.charmings.app.data.model.Pet
import com.charmings.app.data.model.WINTER
import com.charmings.app.data.model.WeatherKind
import com.charmings.app.ui.theme.Blueberry
import com.charmings.app.ui.theme.Bubblegum
import com.charmings.app.ui.theme.Butter
import com.charmings.app.ui.theme.Lilac
import com.charmings.app.ui.theme.Mint
import com.charmings.app.ui.theme.White
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/* ---------- One-shot confetti (replaces the Lottie dependency + JSON asset) ---------- */

private class Bit(
    val angle: Float, val speed: Float, val color: Color, val size: Float,
    val spin: Float, val kind: Int
)

private val ConfettiColors = listOf(Blueberry, Bubblegum, Butter, Mint, Lilac)

/** A single celebratory burst from the upper middle. Plays once per [key]. */
@Composable
fun ConfettiBurst(key: Any, modifier: Modifier = Modifier) {
    if (!rememberMotionEnabled()) return
    val bits = remember(key) {
        val rnd = Random(key.hashCode())
        List(80) {
            Bit(
                angle = (-PI / 2 + (rnd.nextFloat() - 0.5f) * PI * 1.4).toFloat(),
                speed = 0.55f + rnd.nextFloat() * 0.75f,
                color = ConfettiColors[rnd.nextInt(ConfettiColors.size)],
                size = 0.012f + rnd.nextFloat() * 0.014f,
                spin = (rnd.nextFloat() - 0.5f) * 1440f,
                kind = rnd.nextInt(3)
            )
        }
    }
    val progress = remember(key) { Animatable(0f) }
    LaunchedEffect(key) { progress.animateTo(1f, tween(2400, easing = LinearEasing)) }

    Canvas(modifier) {
        val p = progress.value
        if (p >= 1f) return@Canvas
        val w = size.width
        val origin = Offset(w / 2f, size.height * 0.42f)
        val alpha = if (p < 0.7f) 1f else (1f - (p - 0.7f) / 0.3f)
        bits.forEach { b ->
            val t = p * 1.6f
            val x = origin.x + cos(b.angle) * b.speed * w * t
            val y = origin.y + sin(b.angle) * b.speed * w * t + 0.9f * w * t * t
            val s = b.size * w
            rotate(b.spin * p, Offset(x, y)) {
                when (b.kind) {
                    0 -> drawCircle(b.color.copy(alpha = alpha), s * 0.6f, Offset(x, y))
                    1 -> drawRoundRect(
                        b.color.copy(alpha = alpha), Offset(x - s, y - s * 0.4f), Size(s * 2, s * 0.8f),
                        CornerRadius(s * 0.4f)
                    )
                    else -> drawStar(Offset(x, y), s, b.color.copy(alpha = alpha))
                }
            }
        }
    }
}

/* ---------- Ambient weather / season particles behind a pet ---------- */

enum class Ambient(val sky: Color) {
    RAIN(Color(0xFFDCD8FF)),
    SNOW(Color(0xFFDDE7FF)),
    SPARKLE(Color(0xFFFFF0C7)),
    LEAVES(Color(0xFFFFE3D1)),
    NIGHT(Color(0xFF4A3A6B)),
    PLAIN(Lilac);

    companion object {
        fun of(pet: Pet): Ambient {
            val r = pet.requirements
            val w = r.weather.orEmpty()
            val night = r.hours?.let { (s, e) -> s >= 20 || (s > e) } == true
            return when {
                WeatherKind.RAIN in w -> RAIN
                WeatherKind.SNOW in w || WeatherKind.BLIZZARD in w -> SNOW
                night -> NIGHT
                WeatherKind.CLEAR in w || r.holiday != null -> SPARKLE
                r.months != null && r.months.all { it in WINTER } -> SNOW
                r.months != null && r.months.all { it in AUTUMN } -> LEAVES
                else -> PLAIN
            }
        }
    }
}

private class Flake(val x: Float, val y: Float, val speed: Int, val sway: Int, val phase: Float, val size: Float)

/**
 * Seamless looping particles. Every particle moves an *integer* number of laps per loop,
 * so the 0→1 restart is invisible. Positions are pure functions of time: no per-frame allocation.
 */
@Composable
fun AmbientParticles(ambient: Ambient, modifier: Modifier = Modifier, seed: Int = 0) {
    if (ambient == Ambient.PLAIN || !rememberMotionEnabled()) return
    val flakes = remember(ambient, seed) {
        val rnd = Random(seed * 31 + ambient.ordinal)
        val count = when (ambient) { Ambient.RAIN -> 34; Ambient.NIGHT -> 22; else -> 18 }
        List(count) {
            Flake(rnd.nextFloat(), rnd.nextFloat(), 1 + rnd.nextInt(2), 1 + rnd.nextInt(2), rnd.nextFloat(), 0.6f + rnd.nextFloat() * 0.8f)
        }
    }
    val duration = when (ambient) { Ambient.RAIN -> 2600; Ambient.SNOW, Ambient.LEAVES -> 14000; else -> 6000 }
    val t = rememberInfiniteTransition(label = "ambient").animateFloat(
        0f, 1f, infiniteRepeatable(tween(duration, easing = LinearEasing)), label = "ambientT"
    )

    Canvas(modifier) {
        val time = t.value
        val u = size.minDimension / 100f
        flakes.forEach { f ->
            val swayX = sin(2 * PI * (time * f.sway + f.phase)).toFloat()
            when (ambient) {
                Ambient.RAIN -> {
                    val y = ((f.y + time * f.speed) % 1f) * size.height
                    val x = f.x * size.width
                    drawLine(Blueberry.copy(alpha = 0.35f), Offset(x, y), Offset(x - 1.5f * u, y + 5f * u), 0.9f * u, StrokeCap.Round)
                }
                Ambient.SNOW -> {
                    val y = ((f.y + time * f.speed) % 1f) * size.height
                    drawCircle(White, 1.3f * u * f.size, Offset(f.x * size.width + swayX * 4f * u, y))
                }
                Ambient.LEAVES -> {
                    val y = ((f.y + time * f.speed) % 1f) * size.height
                    val c = if (f.speed == 1) Color(0xFFF2A65A) else Butter
                    rotate(swayX * 50f, Offset(f.x * size.width + swayX * 6f * u, y)) {
                        drawOval(c, Offset(f.x * size.width + swayX * 6f * u - 2f * u, y - u), Size(4f * u * f.size, 2f * u * f.size))
                    }
                }
                Ambient.SPARKLE, Ambient.NIGHT -> {
                    val twinkle = 0.5f + 0.5f * sin(2 * PI * (time * f.sway + f.phase)).toFloat()
                    val c = if (ambient == Ambient.NIGHT) Butter else White
                    drawStar(Offset(f.x * size.width, f.y * size.height), (1.2f + 1.4f * twinkle) * u * f.size, c.copy(alpha = 0.35f + 0.65f * twinkle))
                }
                Ambient.PLAIN -> Unit
            }
        }
    }
}
