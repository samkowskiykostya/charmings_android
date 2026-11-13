package com.charmings.app.ui.components

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.withFrameNanos
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos

/** Respects the system "Remove animations" setting (animator duration scale = 0). */
@Composable
fun rememberMotionEnabled(): Boolean {
    val context = LocalContext.current
    return remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
    }
}

/**
 * Gentle idle "breathing": a soft squash, a little bob and a tiny sway, played as a true boomerang.
 * Every transform is a function of one eased value `e` that glides 0 → 1 → 0 (a cosine), so the
 * motion runs forward then exactly in reverse with zero velocity at both ends: no seam, no jump.
 * Phase is accumulated from the frame clock, so changing [speed] (e.g. walking) never restarts it.
 * [seed] (the pet id) gives every charming its own rhythm and starting point.
 * The animated value is read inside graphicsLayer, so only the draw phase re-runs per frame.
 */
@Composable
fun Modifier.breathing(seed: Int, speed: Float = 1f, intensity: Float = 1f): Modifier {
    if (!rememberMotionEnabled()) return this
    val periodMs = ((2600 + (seed * 137) % 900) / speed).coerceAtLeast(400f)
    val currentPeriod by rememberUpdatedState(periodMs)
    val phase = remember(seed) { mutableFloatStateOf((seed * 0.61f) % 1f) }
    LaunchedEffect(seed) {
        var last = withFrameNanos { it }
        while (true) {
            withFrameNanos { now ->
                val dtMs = (now - last) / 1_000_000f
                last = now
                phase.floatValue = (phase.floatValue + dtMs / currentPeriod) % 1f
            }
        }
    }
    val bobPx = with(LocalDensity.current) { 4f * density } * intensity
    val swayDir = if (seed % 2 == 0) 1f else -1f
    return graphicsLayer {
        // 0 → 1 → 0 over one period, smooth (ease-in-out) at both turning points.
        val e = 0.5f - 0.5f * cos(2f * PI.toFloat() * phase.floatValue)
        val c = 2f * e - 1f // -1 … 1
        scaleY = 1f + 0.03f * intensity * c
        scaleX = 1f - 0.015f * intensity * c
        translationY = -bobPx * e
        rotationZ = 1.6f * intensity * swayDir * c
        transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 1f)
    }
}

/** Squash-and-stretch on demand (e.g. when a pet is tapped). */
@Stable
class SquashState(private val scope: CoroutineScope) {
    val scaleX = Animatable(1f)
    val scaleY = Animatable(1f)

    fun play() {
        scope.launch {
            launch { scaleX.animateTo(1.12f, tween(90)); scaleX.animateTo(1f, spring(Spring.DampingRatioHighBouncy, Spring.StiffnessMediumLow)) }
            launch { scaleY.animateTo(0.86f, tween(90)); scaleY.animateTo(1f, spring(Spring.DampingRatioHighBouncy, Spring.StiffnessMediumLow)) }
        }
    }
}

@Composable
fun rememberSquashState(): SquashState {
    val scope = rememberCoroutineScope()
    return remember { SquashState(scope) }
}

fun Modifier.squash(state: SquashState): Modifier = graphicsLayer {
    scaleX = state.scaleX.value
    scaleY = state.scaleY.value
    transformOrigin = androidx.compose.ui.graphics.TransformOrigin(0.5f, 1f)
}
