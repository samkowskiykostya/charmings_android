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
import com.charmings.app.ui.components.sticker
import com.charmings.app.ui.components.stickerTilt
import com.charmings.app.ui.theme.Blueberry
import com.charmings.app.ui.theme.Bubblegum
import com.charmings.app.ui.theme.Butter
import com.charmings.app.ui.theme.Lilac
import com.charmings.app.ui.theme.Plum
import com.charmings.app.ui.theme.PlumSoft
import com.charmings.app.ui.theme.White
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
        Text(
            "Чарівнятка",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.weight(1f)
        )
        val active = status == TrackingStatus.ACTIVE
        Button(
            onClick = if (active) onStop else onStart,
            modifier = Modifier.heightIn(min = 40.dp),
            shape = CircleShape,
            colors = ButtonDefaults.buttonColors(
                containerColor = if (active) Lilac else Blueberry,
                contentColor = if (active) Plum else White
            ),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp)
        ) {
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
            PetImage(
                petId = 0,
                contentDescription = null,
                modifier = Modifier
                    .size(ring * 0.42f)
                    .breathing(seed = 0, speed = if (walk.isWalking) 2.4f else 1f, intensity = if (walk.isWalking) 1.4f else 1f)
                    .sticker(rotation = -3f, corner = ring * 0.12f, border = 4.dp, elevation = 4.dp)
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
            Chip("${walk.sessionMinutes} хв ходьби", Bubblegum.copy(alpha = 0.22f))
        }
        Chip("Сьогодні ${formatSteps(walk.daySteps)}", Lilac)
    }
}

@Composable
private fun Chip(text: String, color: androidx.compose.ui.graphics.Color) {
    Text(
        text,
        style = MaterialTheme.typography.labelLarge,
        modifier = Modifier
            .background(color, CircleShape)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    )
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp)
    )
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
                            .sticker(rotation = stickerTilt(id), corner = 42.dp, border = 4.dp)
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
        color = Plum,
        modifier = modifier
            .background(Butter, CircleShape)
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}

@Composable
private fun HistoryList(history: List<WalkingHistoryEntry>) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    Column(
        Modifier
            .fillMaxWidth()
            .background(White, RoundedCornerShape(20.dp))
            .padding(horizontal = 18.dp, vertical = 8.dp)
    ) {
        history.forEachIndexed { i, entry ->
            Row(
                Modifier.fillMaxWidth().padding(vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${formatSteps(entry.steps)} ${stepsWord(entry.steps)}",
                    style = MaterialTheme.typography.titleMedium,
                    color = Blueberry,
                    modifier = Modifier.weight(1f)
                )
                if (entry.durationMinutes > 0) {
                    Text(
                        "${entry.durationMinutes} ${minutesWord(entry.durationMinutes)}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PlumSoft
                    )
                    Spacer(Modifier.size(14.dp))
                }
                Text(timeFormat.format(Date(entry.startTime)), style = MaterialTheme.typography.bodyMedium, color = PlumSoft)
            }
            if (i < history.lastIndex) Box(Modifier.fillMaxWidth().height(1.dp).background(Lilac))
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
                    .background(Bubblegum.copy(alpha = 0.25f), CircleShape)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            )
        }
    }
}
