package com.charmings.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.charmings.app.ui.theme.Black
import com.charmings.app.ui.theme.Primary
import com.charmings.app.ui.theme.Secondary
import com.charmings.app.ui.theme.White

@Composable
fun CircularProgressIndicator(
    steps: Int,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 14.dp,
    backgroundStrokeWidth: Dp = 16.dp
) {
    val hunPerc = 1000
    val fillPercentage = (steps % hunPerc) * 100f / hunPerc
    val sweepAngle = 220f
    val startAngle = -200f // Rotated to start from bottom-left
    
    val gradientColors = listOf(
        Color(0xFF7444C0),  // Primary
        Color(0xFF5636B8),  // Secondary
        Color(0xFF9B6DD6)   // Lighter accent
    )
    val backgroundColor = Color(0xFFd4c8ed)
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val diameter = size.minDimension * 0.85f
            val topLeft = Offset(
                (size.width - diameter) / 2,
                (size.height - diameter) / 2
            )
            
            // Background arc with softer color
            drawArc(
                color = backgroundColor,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(
                    width = backgroundStrokeWidth.toPx(),
                    cap = StrokeCap.Round
                )
            )
            
            // Progress arc with gradient
            val progressSweep = sweepAngle * (fillPercentage / 100f)
            drawArc(
                brush = Brush.sweepGradient(
                    colors = gradientColors,
                    center = Offset(size.width / 2, size.height / 2)
                ),
                startAngle = startAngle,
                sweepAngle = progressSweep,
                useCenter = false,
                topLeft = topLeft,
                size = Size(diameter, diameter),
                style = Stroke(
                    width = strokeWidth.toPx(),
                    cap = StrokeCap.Round
                )
            )
        }
        
        // Center text with shadow effect
        Box(
            modifier = Modifier
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(White, Color(0xFFF8F6FC))
                    ),
                    shape = RoundedCornerShape(30.dp)
                )
                .border(
                    width = 2.dp,
                    brush = Brush.horizontalGradient(
                        colors = listOf(Primary.copy(alpha = 0.3f), Secondary.copy(alpha = 0.3f))
                    ),
                    shape = RoundedCornerShape(30.dp)
                )
                .padding(horizontal = 24.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$steps кроків",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Primary
            )
        }
    }
}
