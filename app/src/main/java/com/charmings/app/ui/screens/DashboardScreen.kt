package com.charmings.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
    onNewCatchClick: (Int) -> Unit
) {
    val context = LocalContext.current
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightPrimary)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))
            
            // Step counter circle
            StepProgress(
                steps = state.totalSteps,
                modifier = Modifier.size(280.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Encouragement message
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .background(LightPrimary, RoundedCornerShape(25.dp))
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = state.encouragement,
                    fontSize = 22.sp,
                    color = Black,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Date and holiday
            Box(
                modifier = Modifier
                    .background(
                        Color.White.copy(alpha = 0.8f),
                        RoundedCornerShape(25.dp)
                    )
                    .border(1.dp, White, RoundedCornerShape(25.dp))
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${state.currentDay}, ${state.currentDate}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Black
                    )
                    state.todayHoliday?.let { holiday ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = holiday,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Primary
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
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
