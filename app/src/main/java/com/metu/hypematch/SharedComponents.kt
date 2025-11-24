package com.metu.hypematch

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale

// Logo animado con nota musical y ecualizador
@Composable
fun AnimatedMusicLogo(modifier: Modifier = Modifier) {
    // Animación de pulso (efecto ecualizador)
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "rotation"
    )

    Box(
        modifier = modifier
    ) {
        // Canvas para el fondo circular
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val width = size.width
            val height = size.height
            val centerX = width / 2
            val centerY = height / 2

            // Fondo circular amarillo
            drawCircle(
                color = PopArtColors.Yellow,
                radius = width / 2,
                center = Offset(centerX, centerY)
            )

            // Borde negro
            drawCircle(
                color = PopArtColors.Black,
                radius = width / 2,
                center = Offset(centerX, centerY),
                style = Stroke(width = width * 0.02f)
            )
        }
        
        // Imagen de la clave de sol - centrada con animación
        coil.compose.AsyncImage(
            model = R.drawable.treble_clef,
            contentDescription = "Clave de Sol",
            modifier = Modifier
                .fillMaxSize(0.75f)
                .align(Alignment.Center)
                .scale(scale)
                .rotate(rotation),
            contentScale = ContentScale.Fit
        )
    }
}
