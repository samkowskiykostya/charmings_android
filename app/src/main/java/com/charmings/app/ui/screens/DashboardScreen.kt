package com.charmings.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.charmings.app.data.PetsData
import com.charmings.app.data.repository.WalkingHistoryEntry
import com.charmings.app.domain.formatSteps
import com.charmings.app.domain.minutesWord
import com.charmings.app.domain.stepsWord
import com.charmings.app.ui.components.PetImage
import com.charmings.app.ui.components.SpeechBubble
import com.charmings.app.ui.components.StepRing
import com.charmings.app.ui.components.breathing
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import com.charmings.app.ui.components.ChunkyButton
import com.charmings.app.ui.components.Paw
import com.charmings.app.ui.components.cozyCard
import com.charmings.app.ui.components.drawHeart
import com.charmings.app.ui.theme.Apricot
import com.charmings.app.ui.theme.Cocoa
import com.charmings.app.ui.theme.CocoaSoft
import com.charmings.app.ui.theme.Honey
import com.charmings.app.ui.theme.Oat
import com.charmings.app.ui.theme.Rose
import com.charmings.app.ui.theme.Stitch
import java.util.Calendar
import com.charmings.app.ui.viewmodel.DashboardState
import com.charmings.app.ui.viewmodel.TrackingStatus
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val FULL_WALK_MINUTES = 30f

@Composable
fun DashboardScreen(
    state: DashboardState,
    onNewCatchClick: (Int) -> Unit,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit
) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        // Size the hero from the space we actually have (not the raw screen height).
        val ring: Dp = minOf(maxWidth * 0.74f, maxHeight * 0.42f).coerceIn(180.dp, 340.dp)

        val viewportHeight = maxHeight
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                // Fill at least the screen so a short page centres its content instead of
                // leaving the bottom half empty on tall phones; longer pages simply scroll.
                .heightIn(min = viewportHeight),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Header(state.status, onStartTracking, onStopTracking)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(8.dp))
            CompanionRing(state, ring)

            Spacer(Modifier.height(12.dp))
            StatChips(state)

            Spacer(Modifier.height(18.dp))
            AnimatedContent(
                targetState = state.message,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "bubble"
            ) { msg ->
                SpeechBubble(msg, Modifier.fillMaxWidth().widthIn(max = 520.dp))
            }

            if (state.newCatches.isNotEmpty()) {
                Spacer(Modifier.height(26.dp))
                SectionTitle("Нові знахідки")
                NewCatchesRow(state.newCatches, onNewCatchClick)
            }

            if (state.history.isNotEmpty()) {
                Spacer(Modifier.height(22.dp))
                SectionTitle("Останні прогулянки")
                HistoryList(state.history)
            }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Spacer(Modifier.height(22.dp))
                DateLine(state.dateLabel, state.holiday)
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun Header(status: TrackingStatus, onStart: () -> Unit, onStop: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(top = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(greeting(), style = MaterialTheme.typography.bodySmall)
            Text("Чарівнятка", style = MaterialTheme.typography.headlineMedium)
        }
        val active = status == TrackingStatus.ACTIVE
        ChunkyButton(
            onClick = if (active) onStop else onStart,
            color = if (active) Oat else Apricot,
            edge = if (active) Stitch else com.charmings.app.ui.theme.ApricotDeep
        ) {
            if (status == TrackingStatus.PAUSED) {
                Paw(Cocoa, Modifier.size(18.dp), angle = -15f)
                Spacer(Modifier.size(6.dp))
            }
            Text(
                when (status) {
                    TrackingStatus.ACTIVE -> "Пауза"
                    TrackingStatus.PAUSED -> "Гуляти"
                    TrackingStatus.NO_PERMISSION -> "Дозволити"
                },
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

private fun greeting(): String = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
    in 5..11 -> "Доброго ранку"
    in 12..17 -> "Гарного дня"
    in 18..22 -> "Затишного вечора"
    else -> "Солодких снів"
}

/** The hero: Ось lives inside the walk ring and bounces faster while you walk. */
@Composable
private fun CompanionRing(state: DashboardState, ring: Dp) {
    val walk = state.walk
    val steps = walk.liveSessionSteps
    val progress = walk.sessionMinutes / FULL_WALK_MINUTES
    val density = LocalDensity.current
    val numberSize = with(density) { (ring * 0.15f).toSp() } // bounded by the ring, ignores font scale

    StepRing(
        progress = progress,
        modifier = Modifier
            .size(ring)
            .semantics { contentDescription = "${formatSteps(steps)} ${stepsWord(steps)} у прогулянці" }
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            // The companion art is a transparent cut-out: no card, Ось stands right on the glow.
            PetImage(
                petId = 0,
                contentDescription = null,
                modifier = Modifier
                    .size(ring * 0.42f)
                    .breathing(seed = 0, speed = if (walk.isWalking) 2.4f else 1f, intensity = if (walk.isWalking) 1.4f else 1f)
            )
            Spacer(Modifier.height(ring * 0.03f))
            Text(
                formatSteps(steps),
                style = MaterialTheme.typography.displayLarge.copy(fontSize = numberSize, lineHeight = numberSize),
                maxLines = 1,
                softWrap = false
            )
            Text(
                when {
                    walk.isWalking -> stepsWord(steps) + " у прогулянці"
                    steps > 0 -> stepsWord(steps) + " — розходжуємось"
                    else -> "чекаємо на прогулянку"
                },
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun StatChips(state: DashboardState) {
    val walk = state.walk
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (walk.isWalking) {
            Chip("${walk.sessionMinutes} хв ходьби", Rose.copy(alpha = 0.35f), heart = true)
        }
        Chip("Сьогодні ${formatSteps(walk.daySteps)}", Oat)
    }
}

@Composable
private fun Chip(text: String, color: androidx.compose.ui.graphics.Color, heart: Boolean = false) {
    Row(
        Modifier
            .background(color, CircleShape)
            .padding(start = 10.dp, end = 14.dp, top = 7.dp, bottom = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (heart) {
            Canvas(Modifier.size(14.dp)) { drawHeart(Offset(size.width / 2, size.height / 2), size.minDimension * 0.42f, Rose) }
        } else {
            Paw(Apricot, Modifier.size(16.dp), angle = 12f)
        }
        Spacer(Modifier.size(6.dp))
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Row(
        Modifier.fillMaxWidth().padding(bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(Modifier.size(18.dp)) { drawHeart(Offset(size.width / 2, size.height / 2), size.minDimension * 0.42f, Honey) }
        Spacer(Modifier.size(8.dp))
        Text(text, style = MaterialTheme.typography.titleLarge)
    }
}

@Composable
private fun NewCatchesRow(ids: List<Int>, onClick: (Int) -> Unit) {
    LazyRow(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(vertical = 8.dp, horizontal = 4.dp)
    ) {
        items(ids, key = { it }) { id ->
            val pet = PetsData.getPetById(id) ?: return@items
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .clickable { onClick(id) }
                    .padding(4.dp)
            ) {
                Box {
                    PetImage(
                        id, pet.name,
                        Modifier
                            .size(84.dp)
                            .breathing(seed = id, intensity = 0.6f)
                    )
                    NewBadge(Modifier.align(Alignment.TopEnd))
                }
                Spacer(Modifier.height(6.dp))
                Text(pet.name, style = MaterialTheme.typography.titleSmall, maxLines = 1)
            }
        }
    }
}

@Composable
fun NewBadge(modifier: Modifier = Modifier) {
    Text(
        "нове",
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Black),
        color = Cocoa,
        modifier = modifier
            .background(Honey, CircleShape)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}

@Composable
private fun HistoryList(history: List<WalkingHistoryEntry>) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    Column(
        Modifier
            .fillMaxWidth()
            .cozyCard()
            .padding(horizontal = 18.dp, vertical = 10.dp)
    ) {
        history.forEachIndexed { i, entry ->
            Row(
                Modifier.fillMaxWidth().padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(34.dp).background(if (i % 2 == 0) Oat else Rose.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) { Paw(Apricot, Modifier.size(20.dp), angle = if (i % 2 == 0) -14f else 14f) }
                Spacer(Modifier.size(12.dp))
                Text(
                    "${formatSteps(entry.steps)} ${stepsWord(entry.steps)}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                if (entry.durationMinutes > 0) {
                    Text(
                        "${entry.durationMinutes} ${minutesWord(entry.durationMinutes)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = CocoaSoft
                    )
                    Spacer(Modifier.size(14.dp))
                }
                Text(timeFormat.format(Date(entry.startTime)), style = MaterialTheme.typography.bodyMedium, color = CocoaSoft)
            }
            if (i < history.lastIndex) Box(Modifier.fillMaxWidth().padding(start = 46.dp).height(1.5.dp).background(Oat, CircleShape))
        }
    }
}

@Composable
private fun DateLine(date: String, holiday: String?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(date, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
        if (holiday != null) {
            Spacer(Modifier.height(6.dp))
            Text(
                holiday,
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier
                    .background(Rose.copy(alpha = 0.3f), CircleShape)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            )
        }
    }
}
