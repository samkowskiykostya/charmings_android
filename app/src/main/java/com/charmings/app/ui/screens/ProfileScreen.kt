package com.charmings.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.charmings.app.data.model.Pet
import com.charmings.app.ui.components.Ambient
import com.charmings.app.ui.components.AmbientParticles
import com.charmings.app.ui.components.ConfettiBurst
import com.charmings.app.ui.components.PetImage
import com.charmings.app.ui.components.SpeechBubble
import com.charmings.app.ui.components.breathing
import com.charmings.app.ui.components.rememberSquashState
import com.charmings.app.ui.components.squash
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import com.charmings.app.ui.components.CocoaShadow
import com.charmings.app.ui.components.cozyCard
import com.charmings.app.ui.components.drawStitch
import com.charmings.app.ui.theme.Cocoa
import com.charmings.app.ui.theme.Honey
import com.charmings.app.ui.theme.Oat
import com.charmings.app.ui.theme.Paper

@Composable
fun ProfileScreen(pet: Pet?, isNew: Boolean, onBack: () -> Unit) {
    if (pet == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Це чарівнятко ще ховається", style = MaterialTheme.typography.titleMedium)
        }
        return
    }
    val ambient = remember(pet.id) { Ambient.of(pet) }
    // -1 = the pet hasn't said anything yet; taps cycle through its phrases.
    var phraseIndex by rememberSaveable(pet.id) { mutableIntStateOf(-1) }
    val squash = rememberSquashState()

    BoxWithConstraints(Modifier.fillMaxSize()) {
        val screenH = LocalConfiguration.current.screenHeightDp.dp
        val hero = minOf(maxWidth - 72.dp, screenH * 0.42f).coerceIn(180.dp, 420.dp)

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Room for the pinned back button overlaid below.
            Spacer(Modifier.height(56.dp))

            if (isNew) {
                Text(
                    "✦ Нове знайомство! ✦",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier
                        .background(Honey, CircleShape)
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                )
                Spacer(Modifier.height(8.dp))
            }

            // Hero: a coloured "sky" blob with this pet's weather, the pet breathing right on it.
            Box(Modifier.size(hero + 48.dp), contentAlignment = Alignment.Center) {
                Box(
                    Modifier
                        .size(hero + 40.dp)
                        .background(ambient.sky, CircleShape)
                        .drawBehind {
                            // a stitched seam around the "sky", like an embroidery hoop
                            val inset = 9.dp.toPx()
                            drawCircle(
                                Paper.copy(alpha = 0.8f), size.minDimension / 2 - inset,
                                style = Stroke(2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(9.dp.toPx(), 7.dp.toPx())))
                            )
                        }
                )
                AmbientParticles(ambient, Modifier.size(hero + 40.dp), seed = pet.id)
                PetImage(
                    pet.id, pet.name,
                    Modifier
                        .size(hero * 0.82f)
                        .breathing(seed = pet.id)
                        .squash(squash)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClickLabel = "Поговорити"
                        ) {
                            squash.play()
                            if (pet.phrases.isNotEmpty()) phraseIndex = (phraseIndex + 1) % pet.phrases.size
                        }
                )
                if (isNew) ConfettiBurst(key = pet.id, modifier = Modifier.matchParentSize())
            }

            Spacer(Modifier.height(6.dp))
            AnimatedContent(
                targetState = phraseIndex,
                transitionSpec = { (scaleIn(spring(dampingRatio = 0.55f), initialScale = 0.85f) + fadeIn()) togetherWith fadeOut() },
                label = "phrase"
            ) { idx ->
                if (idx < 0) {
                    Text(
                        "Торкнись чарівнятка, щоб привітатися",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 18.dp, bottom = 8.dp)
                    )
                } else {
                    SpeechBubble(
                        pet.phrases[idx],
                        Modifier.padding(horizontal = 32.dp).widthIn(max = 420.dp)
                    )
                }
            }

            Column(
                Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(Modifier.height(18.dp))
                Text(pet.name, style = MaterialTheme.typography.headlineLarge, textAlign = TextAlign.Center)
                Spacer(Modifier.height(4.dp))
                Text(
                    pet.description,
                    style = MaterialTheme.typography.bodyLarge.copy(color = MaterialTheme.colorScheme.onSurfaceVariant),
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    pet.requirementsDescription,
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .background(Oat, RoundedCornerShape(18.dp))
                        .drawBehind { drawStitch(18.dp, inset = 4.dp) }
                        .padding(horizontal = 18.dp, vertical = 12.dp)
                )
                Spacer(Modifier.height(20.dp))
                Text(
                    pet.story,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier
                        .fillMaxWidth()
                        .cozyCard(corner = 28.dp)
                        .padding(horizontal = 24.dp, vertical = 22.dp)
                )
                Spacer(Modifier.height(32.dp))
            }
        }

        // Pinned outside the scrolling column so it stays on screen while scrolling.
        IconButton(
            onClick = onBack,
            colors = IconButtonDefaults.iconButtonColors(containerColor = Paper, contentColor = Cocoa),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 8.dp, top = 8.dp)
                .shadow(4.dp, CircleShape, ambientColor = CocoaShadow, spotColor = CocoaShadow)
        ) {
            Icon(Icons.Rounded.ArrowBack, contentDescription = "Назад")
        }
    }
}
