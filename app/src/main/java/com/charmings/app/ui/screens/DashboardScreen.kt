package com.charmings.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.charmings.app.data.PetsData
import com.charmings.app.ui.theme.*
import com.charmings.app.ui.viewmodel.DashboardState
import com.charmings.app.ui.components.CircularProgressIndicator as StepProgress

@Composable
fun DashboardScreen(
    state: DashboardState,
    onNewCatchClick: (Int) -> Unit,
    onStartTracking: () -> Unit = {}
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    
    // Adaptive sizing based on screen height
    val isSmallScreen = screenHeight < 680.dp
    val isMediumScreen = screenHeight in 680.dp..780.dp
    
    val stepProgressSize = when {
        isSmallScreen -> 180.dp
        isMediumScreen -> 220.dp
        else -> 260.dp
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        LightPrimary,
                        Color(0xFFF5F0FF),
                        LightPrimary.copy(alpha = 0.8f)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Spacer(modifier = Modifier.height(if (isSmallScreen) 8.dp else 16.dp))
            
            // Step counter circle
            StepProgress(
                steps = state.totalSteps,
                modifier = Modifier.size(stepProgressSize)
            )
            
            // Start tracking button centered below the arc
            if (!state.isServiceRunning) {
                Spacer(modifier = Modifier.height(if (isSmallScreen) 8.dp else 16.dp))
                Button(
                    onClick = onStartTracking,
                    modifier = Modifier
                        .height(48.dp)
                        .shadow(8.dp, RoundedCornerShape(24.dp)),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Primary
                    ),
                    shape = RoundedCornerShape(24.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 4.dp
                    )
                ) {
                    Text(
                        text = "✨ Почати трекінг",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(if (isSmallScreen) 12.dp else 20.dp))
            
            // Encouragement message card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(6.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White.copy(alpha = 0.9f)
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    Primary.copy(alpha = 0.05f),
                                    Secondary.copy(alpha = 0.08f),
                                    Primary.copy(alpha = 0.05f)
                                )
                            )
                        )
                        .padding(vertical = if (isSmallScreen) 14.dp else 20.dp, horizontal = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.encouragement,
                        fontSize = when {
                            isSmallScreen -> 17.sp
                            isMediumScreen -> 19.sp
                            else -> 22.sp
                        },
                        color = DarkGray,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.SemiBold,
                        lineHeight = if (isSmallScreen) 22.sp else 28.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(if (isSmallScreen) 12.dp else 20.dp))
            
            // Date and holiday card
            Card(
                modifier = Modifier
                    .shadow(4.dp, RoundedCornerShape(20.dp)),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color.White
                )
            ) {
                Column(
                    modifier = Modifier.padding(
                        horizontal = 24.dp,
                        vertical = if (isSmallScreen) 10.dp else 14.dp
                    ),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${state.currentDay}, ${state.currentDate}",
                        fontSize = if (isSmallScreen) 15.sp else 17.sp,
                        fontWeight = FontWeight.Medium,
                        color = Gray
                    )
                    state.todayHoliday?.let { holiday ->
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = holiday,
                            fontSize = if (isSmallScreen) 16.sp else 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(if (isSmallScreen) 12.dp else 20.dp))
            
            // New catches section
            if (state.newCatches.isNotEmpty()) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Нові знахідки",
                        fontSize = if (isSmallScreen) 14.sp else 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Gray,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        state.newCatches.take(3).forEach { petId ->
                            NewCatchCircle(
                                petId = petId,
                                onClick = { onNewCatchClick(petId) },
                                size = if (isSmallScreen) 70.dp else 80.dp
                            )
                        }
                    }
                }
            }
            
            // Bottom padding for scroll
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun NewCatchCircle(
    petId: Int,
    onClick: () -> Unit,
    size: androidx.compose.ui.unit.Dp = 80.dp
) {
    val context = LocalContext.current
    val pet = PetsData.getPetById(petId)
    
    pet?.let {
        val imageResId = context.resources.getIdentifier(
            pet.imageResName, "drawable", context.packageName
        )
        
        Box(
            modifier = Modifier
                .size(size)
                .shadow(6.dp, CircleShape)
                .clip(CircleShape)
                .border(
                    width = 4.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(Primary, Secondary, DarkSecondary)
                    ),
                    shape = CircleShape
                )
                .clickable { onClick() },
            contentAlignment = Alignment.Center
        ) {
            if (imageResId != 0) {
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = pet.name,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                // Placeholder
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.radialGradient(
                                colors = listOf(Primary, Secondary)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = pet.name.first().toString(),
                        color = White,
                        fontSize = (size.value * 0.3f).sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
