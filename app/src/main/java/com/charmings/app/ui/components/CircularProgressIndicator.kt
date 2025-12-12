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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.charmings.app.ui.theme.Black
import com.charmings.app.ui.theme.Primary
import com.charmings.app.ui.theme.White

@Composable
fun CircularProgressIndicator(
    steps: Int,
    modifier: Modifier = Modifier,
    strokeWidth: Dp = 10.dp,
    backgroundStrokeWidth: Dp = 12.dp
) {
    val hunPerc = 1000
    val fillPercentage = (steps % hunPerc) * 100f / hunPerc
    val sweepAngle = 200f
    val startAngle = -190f // Rotated to start from bottom-left
    
    val primaryColor = Color(0xFF5636B8)
    val backgroundColor = Color(0xFFab9ade)
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val diameter = size.minDimension
            val radius = diameter / 2
            val topLeft = Offset(
                (size.width - diameter) / 2,
                (size.height - diameter) / 2
            )
            
            // Background arc
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
            
            // Progress arc
            val progressSweep = sweepAngle * (fillPercentage / 100f)
            drawArc(
                color = primaryColor,
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
        
        // Center text
        Box(
            modifier = Modifier
                .background(White, RoundedCornerShape(25.dp))
                .border(1.dp, Black, RoundedCornerShape(25.dp))
                .padding(horizontal = 20.dp, vertical = 10.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$steps кроків",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Black
            )
        }
    }
}
