package com.charmings.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import com.charmings.app.ui.viewmodel.CaughtPetsState

@Composable
fun CaughtScreen(
    state: CaughtPetsState,
    onPetClick: (Int) -> Unit
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
                text = "Ще немає знайдених чарівняток",
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(16.dp),
                fontSize = 18.sp,
                color = Gray,
                textAlign = TextAlign.Center
            )
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(state.pets) { pet ->
                    PetCard(
                        pet = pet,
                        onClick = { onPetClick(pet.id) }
                    )
                }
            }
        }
    }
}

@Composable
fun PetCard(
    pet: Pet,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val imageResId = context.resources.getIdentifier(
        pet.imageResName, "drawable", context.packageName
    )
    
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            if (imageResId != 0) {
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = pet.name,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                        .background(Primary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = pet.name.first().toString(),
                        color = White,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = pet.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = DarkGray
            )
        }
    }
}
