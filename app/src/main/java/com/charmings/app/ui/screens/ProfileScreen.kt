package com.charmings.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.charmings.app.data.model.Pet
import com.charmings.app.ui.theme.*
import com.airbnb.lottie.compose.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    pet: Pet?,
    isNew: Boolean = false,
    onBack: () -> Unit
) {
    if (pet == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("Чарівнятко не знайдено")
        }
        return
    }
    
    val context = LocalContext.current
    val imageResId = context.resources.getIdentifier(
        pet.imageResName, "drawable", context.packageName
    )
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightPrimary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Top bar
            TopAppBar(
                title = { Text("Чарівнятко") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LightPrimary
                )
            )
            
            // Pet image with "Found" badge above
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(450.dp)
            ) {
                // Blurred background
                if (imageResId != 0) {
                    Image(
                        painter = painterResource(id = imageResId),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize()
                            .blur(20.dp),
                        contentScale = ContentScale.Crop
                    )
                }
                
                // Gradient overlay for depth
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(
                                    LightPrimary.copy(alpha = 0.3f),
                                    LightPrimary.copy(alpha = 0.6f)
                                )
                            )
                        )
                )
                
                // "Found" badge at top center
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 8.dp)
                        .zIndex(10f)
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Primary,
                                    Secondary
                                )
                            ),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(horizontal = 24.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "✨ Знайдено!",
                        color = White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Main pet image (sharp, centered)
                if (imageResId != 0) {
                    Image(
                        painter = painterResource(id = imageResId),
                        contentDescription = pet.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(400.dp)
                            .align(Alignment.Center),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = pet.name.first().toString(),
                            color = White,
                            fontSize = 72.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Firework animation for new catches
                if (isNew) {
                    FireworkAnimation(
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            
            // Profile content
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset(y = (-40).dp)
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp, bottomStart = 8.dp, bottomEnd = 8.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    
                    // Name
                    Text(
                        text = pet.name,
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold,
                        color = DarkGray,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Description
                    Text(
                        text = pet.description,
                        fontSize = 16.sp,
                        color = Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Requirements
                    Box(
                        modifier = Modifier
                            .background(LightPrimary, RoundedCornerShape(15.dp))
                            .border(1.dp, Secondary, RoundedCornerShape(15.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = pet.requirementsDescription,
                            fontSize = 16.sp,
                            lineHeight = 24.sp,
                            color = DarkGray,
                            textAlign = TextAlign.Center
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Story
                    Text(
                        text = pet.story.replace("\n", "\n\n"),
                        fontSize = 16.sp,
                        lineHeight = 24.sp,
                        color = Black,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
fun FireworkAnimation(modifier: Modifier = Modifier) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Asset("fireworks.json")
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever
    )
    
    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier
    )
}
