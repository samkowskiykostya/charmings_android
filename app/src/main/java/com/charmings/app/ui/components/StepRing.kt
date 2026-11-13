package com.charmings.app.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import com.charmings.app.ui.theme.Apricot
import com.charmings.app.ui.theme.Cocoa
import com.charmings.app.ui.theme.Honey
import com.charmings.app.ui.theme.Rose
import com.charmings.app.ui.theme.Stitch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

private const val START_ANGLE = 120f   // opening at the bottom, like a horseshoe
private const val SWEEP = 300f

/**
 * Walk progress ring. The track is a trail of little paw prints; the filled arc ends in a honey star.
 * [progress] is 0..1 (walk minutes towards a full 30-minute walk).
 */
@Composable
fun StepRing(
    progress: Float,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), spring(stiffness = 60f), label = "ring")
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.matchParentSize()) {
            val stroke = size.minDimension * 0.055f
            val radius = size.minDimension / 2f - stroke
            val center = Offset(size.width / 2f, size.height / 2f)

            // A warm sunny glow behind the companion
            drawCircle(
                Brush.radialGradient(
                    0f to Honey.copy(alpha = 0.28f), 0.7f to Honey.copy(alpha = 0.10f), 1f to Honey.copy(alpha = 0f),
                    center = center, radius = radius
                ),
                radius, center
            )

            // Paw-print trail, alternating left/right like real footsteps
            val paws = 22
            for (i in 0..paws) {
                val deg = START_ANGLE + SWEEP * i / paws
                val a = Math.toRadians(deg.toDouble())
                val side = if (i % 2 == 0) -1f else 1f
                val rr = radius + side * stroke * 0.32f
                drawPaw(
                    Offset(center.x + rr * cos(a).toFloat(), center.y + rr * sin(a).toFloat()),
                    stroke * 0.28f, Stitch.copy(red = 0.88f, green = 0.73f, blue = 0.58f), angleDeg = deg + 90f
                )
            }

            if (animated > 0.001f) {
                // Rotate so the sweep gradient seam sits in the bottom gap.
                rotate(START_ANGLE, center) {
                    drawArc(
                        brush = Brush.sweepGradient(
                            0f to Honey, SWEEP / 720f to Apricot, SWEEP / 360f to Rose, 1f to Honey, center = center
                        ),
                        startAngle = 0f,
                        sweepAngle = SWEEP * animated,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = Size(radius * 2, radius * 2),
                        style = Stroke(stroke, cap = StrokeCap.Round)
                    )
                }
                val headA = Math.toRadians((START_ANGLE + SWEEP * animated).toDouble())
                val head = Offset(center.x + radius * cos(headA).toFloat(), center.y + radius * sin(headA).toFloat())
                drawStar(head, stroke * 1.3f, Honey, Cocoa)
            }
        }
        content()
    }
}

/** A soft five-point star with a thin ink outline. */
fun DrawScope.drawStar(center: Offset, outerRadius: Float, fill: androidx.compose.ui.graphics.Color, outline: androidx.compose.ui.graphics.Color? = null) {
    val inner = outerRadius * 0.5f
    val path = Path()
    for (i in 0 until 10) {
        val r = if (i % 2 == 0) outerRadius else inner
        val a = -PI / 2 + i * PI / 5
        val p = Offset(center.x + (r * cos(a)).toFloat(), center.y + (r * sin(a)).toFloat())
        if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
    }
    path.close()
    drawPath(path, fill)
    if (outline != null) drawPath(path, outline, style = Stroke(min(outerRadius * 0.14f, 4f), cap = StrokeCap.Round))
}
