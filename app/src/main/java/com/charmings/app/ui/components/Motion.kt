package com.charmings.app.ui.components

import android.provider.Settings
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
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
import kotlin.math.sin

/** Respects the system "Remove animations" setting (animator duration scale = 0). */
@Composable
fun rememberMotionEnabled(): Boolean {
    val context = LocalContext.current
    return remember {
        Settings.Global.getFloat(context.contentResolver, Settings.Global.ANIMATOR_DURATION_SCALE, 1f) > 0f
    }
}

/**
 * Gentle idle "breathing": a soft squash, a little bob and a tiny wobble.
 * [seed] (the pet id) gives every charming its own rhythm. [speed] > 1 for "walking" excitement.
 * The animated value is read inside graphicsLayer, so only the draw phase re-runs per frame.
 */
@Composable
fun Modifier.breathing(seed: Int, speed: Float = 1f, intensity: Float = 1f): Modifier {
    if (!rememberMotionEnabled()) return this
    val periodMs = ((2600 + (seed * 137) % 900) / speed).toInt().coerceAtLeast(400)
    val phase = (seed * 0.61f) % 1f
    val transition = rememberInfiniteTransition(label = "breath")
    val t = transition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(periodMs, easing = LinearEasing)),
        label = "breathT"
    )
    val bobPx = with(LocalDensity.current) { 4f * density } * intensity
    return graphicsLayer {
        val a = 2f * PI.toFloat() * (t.value + phase)
        val s = sin(a)
        scaleY = 1f + 0.03f * intensity * s
        scaleX = 1f - 0.015f * intensity * s
        translationY = -bobPx * (0.5f + 0.5f * s)
        rotationZ = 1.6f * intensity * sin(a / 2f + seed)
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
