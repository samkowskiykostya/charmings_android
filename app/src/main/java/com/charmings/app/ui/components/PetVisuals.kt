package com.charmings.app.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.charmings.app.ui.images.PetImages
import com.charmings.app.ui.theme.Cocoa
import com.charmings.app.ui.theme.Oat
import com.charmings.app.ui.theme.Paper
import com.charmings.app.ui.theme.Stitch

/**
 * A caught pet's picture. Full image comes from the APK (pet 0) or the network/disk cache;
 * the bundled 24 px thumbnail is the placeholder and the offline fallback.
 * Coil downsamples to the view size, so grids never decode 1024² bitmaps.
 */
@Composable
fun PetImage(petId: Int, contentDescription: String?, modifier: Modifier = Modifier) {
    // Bundled art (the companion) loads synchronously: no placeholder flash on every launch.
    PetImages.bundled(petId)?.let { res ->
        Image(painterResource(res), contentDescription, modifier, contentScale = ContentScale.Crop)
        return
    }
    val thumb = painterResource(PetImages.thumb(petId))
    AsyncImage(
        model = PetImages.url(petId),
        contentDescription = contentDescription,
        placeholder = thumb,
        error = thumb,
        fallback = thumb,
        contentScale = ContentScale.Crop,
        modifier = modifier
    )
}

/**
 * An uncaught pet: a tiny colour blob stretched up is naturally blurry on every Android version
 * (Modifier.blur needs API 31; minSdk is 30) and needs no extra assets.
 */
@Composable
fun MysteryImage(petId: Int, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    // Shrink the thumbnail further to a 6×6 colour blob: stretched back up with bilinear filtering
    // it reads as a soft coloured mist — a hint of the pet's palette, not its silhouette.
    val blob = remember(petId) {
        val src = BitmapFactory.decodeResource(context.resources, PetImages.thumb(petId))
        BitmapPainter(Bitmap.createScaledBitmap(src, 6, 6, true).asImageBitmap(), filterQuality = FilterQuality.Low)
    }
    // Thumbnails are transparent cut-outs: back the mist with paper so it reads as one soft disc.
    BoxWithConstraints(modifier.clip(CircleShape).background(Paper), contentAlignment = Alignment.Center) {
        Image(
            painter = blob,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(Modifier.fillMaxSize().background(Oat.copy(alpha = 0.40f)))
        Text(
            "?",
            fontWeight = FontWeight.Black,
            fontSize = (maxWidth.value * 0.42f).sp,
            style = MaterialTheme.typography.displayLarge.copy(color = Paper)
        )
    }
}

/** Deterministic little tilt per pet, so the mystery discs look hand-placed but never reshuffle. */
fun stickerTilt(petId: Int): Float = ((petId * 37) % 7 - 3) * 0.8f

/** Rounded speech bubble with a tail pointing up at [tailCenterFraction] of the width. */
class BubbleShape(
    private val corner: Dp = 22.dp,
    private val tailWidth: Dp = 22.dp,
    private val tailHeight: Dp = 12.dp,
    private val tailCenterFraction: Float = 0.5f
) : Shape {
    override fun createOutline(size: Size, layoutDirection: LayoutDirection, density: Density): Outline {
        val r = with(density) { corner.toPx() }
        val tw = with(density) { tailWidth.toPx() }
        val th = with(density) { tailHeight.toPx() }
        val cx = size.width * tailCenterFraction
        val top = th
        val path = Path().apply {
            moveTo(r, top)
            lineTo(cx - tw / 2, top)
            quadraticBezierTo(cx - tw / 6, top, cx, 0f)
            quadraticBezierTo(cx + tw / 6, top, cx + tw / 2, top)
            lineTo(size.width - r, top)
            quadraticBezierTo(size.width, top, size.width, top + r)
            lineTo(size.width, size.height - r)
            quadraticBezierTo(size.width, size.height, size.width - r, size.height)
            lineTo(r, size.height)
            quadraticBezierTo(0f, size.height, 0f, size.height - r)
            lineTo(0f, top + r)
            quadraticBezierTo(0f, top, r, top)
            close()
        }
        return Outline.Generic(path)
    }
}

@Composable
fun SpeechBubble(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Paper
) {
    val shape = BubbleShape()
    Box(
        modifier
            .shadow(5.dp, shape, ambientColor = CocoaShadow.copy(alpha = 0.35f), spotColor = CocoaShadow.copy(alpha = 0.3f))
            .background(color, shape)
            .border(1.5.dp, Stitch, shape)
            .padding(PaddingValues(start = 22.dp, end = 22.dp, top = 12.dp + 14.dp, bottom = 16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text,
            style = MaterialTheme.typography.titleMedium,
            color = Cocoa,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
