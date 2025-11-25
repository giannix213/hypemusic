package com.metu.hypematch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
 * Pantalla temporal de Live Streaming
 * Pendiente: Migrar de Agora a ZegoCloud
 * Ver: MIGRACION_ZEGOCLOUD_PENDIENTE.md
 */
@Composable
fun LiveRecordingScreen(
    sessionId: String,
    channelName: String,
    agoraToken: String,
    onStreamStarted: () -> Unit,
    onStreamEnded: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PopArtColors.Black)
    ) {
        // Botón de cerrar
        IconButton(
            onClick = onStreamEnded,
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
        
        // Mensaje central
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "🎥",
                fontSize = 80.sp
            )
            
            Spacer(Modifier.height(24.dp))
            
            Text(
                "Live Streaming",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = PopArtColors.Yellow
            )
            
            Spacer(Modifier.height(16.dp))
            
            Text(
                "Migrando de Agora a ZegoCloud",
                fontSize = 18.sp,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                "Próximamente disponible",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
            
            Spacer(Modifier.height(32.dp))
            
            // Información de configuración
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = PopArtColors.Purple.copy(alpha = 0.2f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Configuración ZegoCloud",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PopArtColors.Cyan
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Text(
                        "✅ App ID: ${ZegoConfig.APP_ID}",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    
                    Text(
                        "✅ App Sign: Configurado",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Text(
                        "⏳ Pendiente: Integrar SDK de ZegoCloud",
                        fontSize = 12.sp,
                        color = PopArtColors.Yellow.copy(alpha = 0.8f)
                    )
                    
                    Text(
                        "📖 Ver: MIGRACION_ZEGOCLOUD_PENDIENTE.md",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            Button(
                onClick = onStreamEnded,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PopArtColors.Pink
                )
            ) {
                Text("Volver")
            }
        }
    }
}
