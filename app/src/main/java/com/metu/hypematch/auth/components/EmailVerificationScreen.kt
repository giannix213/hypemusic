package com.metu.hypematch.auth.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metu.hypematch.DevConfig
import com.metu.hypematch.PopArtColors

@Composable
fun EmailVerificationScreen(
    email: String,
    isLoading: Boolean,
    onCheckVerification: () -> Unit,
    onResendEmail: () -> Unit,
    onBack: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Banner de modo desarrollo
        if (DevConfig.SKIP_EMAIL_VERIFICATION) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = PopArtColors.Pink.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Warning,
                        contentDescription = null,
                        tint = PopArtColors.Pink,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "MODO DESARROLLO\nVerificación desactivada",
                        fontSize = 12.sp,
                        color = PopArtColors.White,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        // Icono de email
        Icon(
            Icons.Default.Email,
            contentDescription = null,
            tint = PopArtColors.Yellow,
            modifier = Modifier.size(80.dp)
        )

        Spacer(Modifier.height(24.dp))

        Text(
            "Verifica tu email",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = PopArtColors.White
        )

        Spacer(Modifier.height(16.dp))

        Text(
            "Hemos enviado un email de verificación a:",
            fontSize = 14.sp,
            color = PopArtColors.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            email,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = PopArtColors.Cyan,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        Text(
            "Por favor revisa tu bandeja de entrada y haz clic en el enlace de verificación.",
            fontSize = 14.sp,
            color = PopArtColors.White.copy(alpha = 0.8f),
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )

        Spacer(Modifier.height(32.dp))

        // Botón para verificar
        Button(
            onClick = onCheckVerification,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PopArtColors.Yellow),
            shape = RoundedCornerShape(30.dp),
            enabled = !isLoading
        ) {
            Text(
                "YA VERIFIQUÉ MI EMAIL",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = PopArtColors.Black
            )
        }

        Spacer(Modifier.height(16.dp))

        // Botón para reenviar email
        OutlinedButton(
            onClick = onResendEmail,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = PopArtColors.White),
            shape = RoundedCornerShape(30.dp),
            border = BorderStroke(2.dp, PopArtColors.White),
            enabled = !isLoading
        ) {
            Icon(
                Icons.Default.Refresh,
                contentDescription = null,
                tint = PopArtColors.White,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "REENVIAR EMAIL",
                fontSize = 16.sp,
                fontWeight = FontWeight.Black,
                color = PopArtColors.White
            )
        }

        Spacer(Modifier.height(24.dp))

        // Botón volver
        TextButton(onClick = onBack, enabled = !isLoading) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = null,
                tint = PopArtColors.Cyan,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("Volver al inicio", fontSize = 14.sp, color = PopArtColors.Cyan)
        }

        Spacer(Modifier.height(16.dp))

        // Nota sobre spam
        Card(
            colors = CardDefaults.cardColors(
                containerColor = PopArtColors.Yellow.copy(alpha = 0.15f)
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "⚠️ REVISA TU SPAM",
                    fontSize = 14.sp,
                    color = PopArtColors.Yellow,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Los emails de verificación suelen llegar a la carpeta de Correo no deseado o Spam",
                    fontSize = 12.sp,
                    color = PopArtColors.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Busca: noreply@hype-13966.firebaseapp.com",
                    fontSize = 11.sp,
                    color = PopArtColors.White.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
