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
    val isSmallScreen = screenHeight < 700.dp
    val stepProgressSize = if (isSmallScreen) 200.dp else 260.dp
    val verticalSpacing = if (isSmallScreen) 12.dp else 24.dp
    val smallSpacing = if (isSmallScreen) 8.dp else 16.dp
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightPrimary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(smallSpacing))
            
            // Step counter circle
            StepProgress(
                steps = state.totalSteps,
                modifier = Modifier.size(stepProgressSize)
            )
            
            Spacer(modifier = Modifier.height(smallSpacing))
            
            // Start tracking button centered below the arc
            if (!state.isServiceRunning) {
                Button(
                    onClick = onStartTracking,
                    modifier = Modifier
                        .height(48.dp),
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
            
            Spacer(modifier = Modifier.height(verticalSpacing))
            
            // Encouragement message
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(LightPrimary, RoundedCornerShape(25.dp))
                    .padding(if (isSmallScreen) 12.dp else 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.encouragement,
                    fontSize = if (isSmallScreen) 18.sp else 22.sp,
                    color = Black,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(verticalSpacing))
            
            // Date and holiday
            Box(
                modifier = Modifier
                    .background(
                        Color.White.copy(alpha = 0.8f),
                        RoundedCornerShape(25.dp)
                    )
                    .border(1.dp, White, RoundedCornerShape(25.dp))
                    .padding(if (isSmallScreen) 10.dp else 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${state.currentDay}, ${state.currentDate}",
                        fontSize = if (isSmallScreen) 16.sp else 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Black
                    )
                    state.todayHoliday?.let { holiday ->
                        Spacer(modifier = Modifier.height(if (isSmallScreen) 4.dp else 8.dp))
                        Text(
                            text = holiday,
                            fontSize = if (isSmallScreen) 18.sp else 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(verticalSpacing))
            
            // New catches
            if (state.newCatches.isNotEmpty()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    state.newCatches.take(3).forEach { petId ->
                        NewCatchCircle(
                            petId = petId,
                            onClick = { onNewCatchClick(petId) }
                        )
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
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val pet = PetsData.getPetById(petId)
    
    pet?.let {
        val imageResId = context.resources.getIdentifier(
            pet.imageResName, "drawable", context.packageName
        )
        
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .border(5.dp, DarkSecondary, CircleShape)
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
                        .background(Primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = pet.name.first().toString(),
                        color = White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
