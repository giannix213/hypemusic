package com.metu.hypematch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Pantalla de Visualización de Live - Migración a ZegoCloud en Progreso
 */
@Composable
fun LiveStreamViewerScreen(
    sessionId: String,
    channelName: String,
    agoraToken: String,
    streamerName: String,
    onExit: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PopArtColors.Black)
    ) {
        // Botón de cerrar
        IconButton(
            onClick = onExit,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Cerrar",
                tint = Color.White
            )
        }
        
        // Contenido
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "📺",
                fontSize = 80.sp
            )
            
            Spacer(Modifier.height(24.dp))
            
            Text(
                "Ver Live Stream",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = PopArtColors.Cyan
            )
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                "Migración a ZegoCloud",
                fontSize = 18.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(Modifier.height(32.dp))
            
            // Card con información
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = PopArtColors.Purple.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp)
                ) {
                    Text(
                        "Información del Live",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PopArtColors.Cyan
                    )
                    
                    Spacer(Modifier.height(12.dp))
                    
                    Text(
                        "• Streamer: $streamerName",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    
                    Text(
                        "• Canal: $channelName",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                    
                    Spacer(Modifier.height(12.dp))
                    
                    Divider(color = Color.White.copy(alpha = 0.2f))
                    
                    Spacer(Modifier.height(12.dp))
                    
                    Text(
                        "⏳ SDK de ZegoCloud no disponible",
                        fontSize = 12.sp,
                        color = PopArtColors.Yellow.copy(alpha = 0.8f),
                        lineHeight = 18.sp
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Text(
                        "📖 Ver: ZEGOCLOUD_IMPLEMENTACION_FINAL.md",
                        fontSize = 11.sp,
                        color = PopArtColors.Cyan.copy(alpha = 0.8f)
                    )
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            Button(
                onClick = onExit,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PopArtColors.Pink
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Volver", fontSize = 16.sp)
            }
        }
    }
}
