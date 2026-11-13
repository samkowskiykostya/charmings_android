package com.charmings.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.foundation.border
import com.charmings.app.ui.components.CocoaShadow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.charmings.app.data.model.Pet
import com.charmings.app.domain.pluralUk
import com.charmings.app.ui.components.MysteryImage
import com.charmings.app.ui.components.PetImage
import com.charmings.app.ui.components.stickerTilt
import com.charmings.app.ui.components.StripedProgress
import com.charmings.app.ui.components.cozyCard
import com.charmings.app.ui.theme.Apricot
import com.charmings.app.ui.theme.Honey
import com.charmings.app.ui.theme.Rose
import com.charmings.app.ui.viewmodel.CollectionState

/* ---------------- Знайдені: the album ---------------- */

@Composable
fun CaughtScreen(state: CollectionState, onPetClick: (Int) -> Unit) {
    if (state.isLoading) return Loading()
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 148.dp),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            ScreenHeader(
                title = "Знайдені",
                subtitle = "${state.caught.size} з ${state.total} чарівняток у твоєму альбомі",
                progress = state.caught.size / state.total.coerceAtLeast(1).toFloat()
            )
        }
        items(state.caught, key = { it.id }) { pet ->
            AlbumSticker(pet, isNew = pet.id in state.newIds, onClick = { onPetClick(pet.id) })
        }
    }
}

@Composable
private fun AlbumSticker(pet: Pet, isNew: Boolean, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(6.dp)
    ) {
        Box {
            PetImage(
                pet.id, pet.name,
                Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            )
            if (isNew) NewBadge(Modifier.align(Alignment.TopEnd).offset(x = (-2).dp, y = (-4).dp))
        }
        Spacer(Modifier.height(10.dp))
        Text(pet.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, textAlign = TextAlign.Center)
    }
}

/* ---------------- Невловимі: who's still hiding ---------------- */

@Composable
fun FieldScreen(state: CollectionState) {
    if (state.isLoading) return Loading()
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 320.dp),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item(span = { GridItemSpan(maxLineSpan) }) {
            ScreenHeader(
                title = "Ще ховаються",
                subtitle = if (state.hiding.isEmpty()) "Ти знайшов усіх! Чарівнятка пишаються тобою."
                else state.hiding.size.let { n ->
                    "$n ${pluralUk(n, "чарівнятко чекає", "чарівнятка чекають", "чарівняток чекають")} на зустріч. Ось де їх шукати."
                }
            )
        }
        items(state.hiding, key = { it.id }) { pet -> HidingRow(pet) }
    }
}

@Composable
private fun HidingRow(pet: Pet) {
    Row(
        Modifier
            .fillMaxWidth()
            .cozyCard()
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        MysteryImage(
            pet.id,
            Modifier
                .size(76.dp)
                .rotate(stickerTilt(pet.id))
                .border(4.dp, com.charmings.app.ui.theme.Paper, CircleShape)
                .shadow(3.dp, CircleShape, ambientColor = CocoaShadow, spotColor = CocoaShadow)
        )
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(pet.name, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(4.dp))
            Text(pet.requirementsDescription, style = MaterialTheme.typography.bodyMedium)
            if (pet.probability < 1.0) {
                Spacer(Modifier.height(6.dp))
                Text(
                    "✦ Рідкісне",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier
                        .background(Honey.copy(alpha = 0.45f), CircleShape)
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                )
            }
        }
    }
}

/* ---------------- shared ---------------- */

@Composable
fun ScreenHeader(title: String, subtitle: String, progress: Float? = null) {
    Column(Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 6.dp)) {
        Text(title, style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(4.dp))
        Text(subtitle, style = MaterialTheme.typography.bodyMedium)
        if (progress != null) {
            Spacer(Modifier.height(14.dp))
            StripedProgress(progress)
        }
    }
}

@Composable
private fun Loading() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = Apricot, trackColor = Rose.copy(alpha = 0.25f))
    }
}
