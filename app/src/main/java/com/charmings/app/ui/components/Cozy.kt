package com.charmings.app.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.charmings.app.ui.theme.Apricot
import com.charmings.app.ui.theme.ApricotDeep
import com.charmings.app.ui.theme.Cocoa
import com.charmings.app.ui.theme.Cream
import com.charmings.app.ui.theme.Honey
import com.charmings.app.ui.theme.Oat
import com.charmings.app.ui.theme.Paper
import com.charmings.app.ui.theme.Rose
import com.charmings.app.ui.theme.Stitch
import kotlin.math.cos
import kotlin.math.sin

/** Warm, never-grey shadow colour. */
val CocoaShadow = Color(0xFF8A4B2A)

/**
 * Cream paper with a faint polka-dot texture and two soft sun blobs in the corners.
 * Drawn once per size; it sits behind every tab screen.
 */
@Composable
fun CozyBackground(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    Box(
        modifier
            .background(Cream)
            .drawBehind {
                drawCircle(Honey.copy(alpha = 0.16f), size.width * 0.55f, Offset(size.width * 1.02f, -size.width * 0.05f))
                drawCircle(Rose.copy(alpha = 0.12f), size.width * 0.5f, Offset(-size.width * 0.12f, size.height * 0.98f))
                val step = 26.dp.toPx()
                val r = 1.3.dp.toPx()
                val dot = Color(0xFFE9D3B8).copy(alpha = 0.55f)
                var row = 0
                var y = step / 2
                while (y < size.height) {
                    var x = if (row % 2 == 0) step / 2 else step
                    while (x < size.width) { drawCircle(dot, r, Offset(x, y)); x += step }
                    y += step * 0.87f; row++
                }
            },
        content = content
    )
}

/** A dashed "stitched" outline inset inside a rounded rect — like a felt patch. */
fun DrawScope.drawStitch(corner: Dp, inset: Dp = 6.dp, color: Color = Stitch) {
    val i = inset.toPx()
    val r = (corner - inset).toPx().coerceAtLeast(0f)
    drawRoundRect(
        color,
        topLeft = Offset(i, i),
        size = Size(size.width - 2 * i, size.height - 2 * i),
        cornerRadius = CornerRadius(r),
        style = Stroke(1.6.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(7.dp.toPx(), 5.dp.toPx())))
    )
}

/** Warm paper card with a soft cocoa shadow and stitching. */
fun Modifier.cozyCard(
    corner: Dp = 26.dp,
    color: Color = Paper,
    elevation: Dp = 5.dp,
    stitch: Boolean = true
): Modifier {
    val shape = RoundedCornerShape(corner)
    return this
        .shadow(elevation, shape, ambientColor = CocoaShadow.copy(alpha = 0.35f), spotColor = CocoaShadow.copy(alpha = 0.3f))
        .background(color, shape)
        .then(if (stitch) Modifier.drawBehind { drawStitch(corner) } else Modifier)
}

/**
 * A squishy toy button: a darker "lip" underneath that the face sinks into when pressed.
 */
@Composable
fun ChunkyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Apricot,
    edge: Color = ApricotDeep,
    contentColor: Color = Cocoa,
    contentPadding: PaddingValues = PaddingValues(horizontal = 20.dp, vertical = 9.dp),
    content: @Composable RowScope.() -> Unit
) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val lip = 4.dp
    val sink by animateDpAsState(if (pressed) lip else 0.dp, spring(stiffness = 900f), label = "sink")
    Box(
        modifier
            .clip(CircleShape)
            .clickable(interactionSource = source, indication = null, role = Role.Button, onClick = onClick)
    ) {
        Box(Modifier.matchParentSize().padding(top = lip).background(edge, CircleShape))
        Row(
            Modifier
                .padding(bottom = lip)
                .offset(y = sink)
                .heightIn(min = 40.dp)
                .background(color, CircleShape)
                .padding(contentPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            androidx.compose.runtime.CompositionLocalProvider(
                androidx.compose.material3.LocalContentColor provides contentColor
            ) { content() }
        }
    }
}

/** A tiny paw print: one pad and four toes. [center] is the pad centre, [s] its radius. */
fun DrawScope.drawPaw(center: Offset, s: Float, color: Color, angleDeg: Float = 0f) {
    rotate(angleDeg, center) {
        drawOval(color, Offset(center.x - s * 1.05f, center.y - s * 0.6f), Size(s * 2.1f, s * 1.75f))
        val toes = listOf(-1.12f to -0.88f, -0.4f to -1.32f, 0.4f to -1.32f, 1.12f to -0.88f)
        toes.forEach { (dx, dy) -> drawCircle(color, s * 0.4f, Offset(center.x + dx * s, center.y + dy * s)) }
    }
}

@Composable
fun Paw(color: Color, modifier: Modifier = Modifier, angle: Float = 0f) {
    Canvas(modifier) { drawPaw(Offset(size.width / 2, size.height * 0.62f), size.minDimension * 0.24f, color, angle) }
}

/** A tiny hand-drawn heart. */
fun DrawScope.drawHeart(center: Offset, s: Float, color: Color) {
    val p = Path().apply {
        moveTo(center.x, center.y + s * 0.9f)
        cubicTo(center.x - s * 1.6f, center.y - s * 0.1f, center.x - s * 0.8f, center.y - s * 1.3f, center.x, center.y - s * 0.45f)
        cubicTo(center.x + s * 0.8f, center.y - s * 1.3f, center.x + s * 1.6f, center.y - s * 0.1f, center.x, center.y + s * 0.9f)
        close()
    }
    drawPath(p, color)
}

/** A candy-striped progress bar. */
@Composable
fun StripedProgress(progress: Float, modifier: Modifier = Modifier, height: Dp = 14.dp) {
    val animated by animateFloatAsState(progress.coerceIn(0f, 1f), spring(stiffness = 80f), label = "bar")
    Box(
        modifier
            .fillMaxWidth()
            .height(height)
            .clip(CircleShape)
            .background(Oat)
    ) {
        Canvas(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(animated.coerceAtLeast(0.04f))
                .clip(CircleShape)
        ) {
            drawRect(Apricot)
            val w = 10.dp.toPx()
            clipRect {
                var x = -size.height
                while (x < size.width + size.height) {
                    val path = Path().apply {
                        moveTo(x, size.height); lineTo(x + w, size.height)
                        lineTo(x + w + size.height, 0f); lineTo(x + size.height, 0f); close()
                    }
                    drawPath(path, Honey.copy(alpha = 0.55f))
                    x += w * 2
                }
            }
            // a little shine along the top
            drawRoundRect(
                Color.White.copy(alpha = 0.35f), Offset(size.height * 0.4f, size.height * 0.18f),
                Size((size.width - size.height * 0.8f).coerceAtLeast(0f), size.height * 0.22f), CornerRadius(size.height)
            )
        }
    }
}

/** Points on a circle, for decorations. */
internal fun polar(center: Offset, r: Float, deg: Float) =
    Offset(center.x + r * cos(Math.toRadians(deg.toDouble())).toFloat(), center.y + r * sin(Math.toRadians(deg.toDouble())).toFloat())
