package com.charmings.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.charmings.app.data.model.Pet
import com.charmings.app.ui.theme.*
import com.charmings.app.ui.viewmodel.UncaughtPetsState

@Composable
fun FieldScreen(
    state: UncaughtPetsState
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LightPrimary)
    ) {
        if (state.isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Primary
            )
        } else if (state.pets.isEmpty()) {
            Text(
                text = "Всі чарівнятка знайдені!",
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                fontSize = 18.sp,
                color = Gray,
                textAlign = TextAlign.Center
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.pets) { pet ->
                    UncaughtPetRow(pet = pet)
                }
            }
        }
    }
}

@Composable
fun UncaughtPetRow(pet: Pet) {
    val context = LocalContext.current
    val imageResId = context.resources.getIdentifier(
        pet.imageResName, "drawable", context.packageName
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Blurred image
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, DarkSecondary, RoundedCornerShape(8.dp))
        ) {
            if (imageResId != 0) {
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = pet.name,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(40.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Primary.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "?",
                        color = White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        // Name and requirements
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = pet.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = DarkGray
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LightPrimary, RoundedCornerShape(15.dp))
                    .border(1.dp, Secondary, RoundedCornerShape(15.dp))
                    .padding(8.dp)
            ) {
                Text(
                    text = pet.requirementsDescription,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    color = DarkGray
                )
            }
        }
    }
}
