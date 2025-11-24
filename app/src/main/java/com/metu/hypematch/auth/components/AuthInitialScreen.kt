package com.metu.hypematch.auth.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metu.hypematch.PopArtColors

@Composable
fun AuthInitialScreen(
    isGoogleSignInAvailable: Boolean,
    isLoading: Boolean,
    onSignUpClick: () -> Unit,
    onLoginClick: () -> Unit,
    onGoogleSignInClick: () -> Unit,
    onGuestClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            "Inicia sesión para descubrir música",
            fontSize = 16.sp,
            color = PopArtColors.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        // Botón Sign Up
        Button(
            onClick = onSignUpClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PopArtColors.Yellow),
            shape = RoundedCornerShape(30.dp),
            enabled = !isLoading
        ) {
            Text(
                "CREAR CUENTA",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = PopArtColors.Black
            )
        }

        Spacer(Modifier.height(16.dp))

        // Botón Login
        OutlinedButton(
            onClick = onLoginClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = PopArtColors.White),
            shape = RoundedCornerShape(30.dp),
            border = BorderStroke(2.dp, PopArtColors.White),
            enabled = !isLoading
        ) {
            Text(
                "INICIAR SESIÓN",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = PopArtColors.White
            )
        }

        Spacer(Modifier.height(24.dp))

        // Divider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = PopArtColors.White.copy(alpha = 0.3f)
            )
            Text(
                "  O  ",
                color = PopArtColors.White.copy(alpha = 0.5f),
                fontSize = 14.sp
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = PopArtColors.White.copy(alpha = 0.3f)
            )
        }

        Spacer(Modifier.height(24.dp))

        // Botón Google
        if (isGoogleSignInAvailable) {
            Button(
                onClick = onGoogleSignInClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PopArtColors.White),
                shape = RoundedCornerShape(30.dp),
                enabled = !isLoading
            ) {
                Icon(
                    Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = PopArtColors.Black,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "Continuar con Google",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PopArtColors.Black
                )
            }

            Spacer(Modifier.height(16.dp))
        }

        // Botón Invitado
        TextButton(
            onClick = onGuestClick,
            enabled = !isLoading
        ) {
            Text(
                "Continuar como invitado",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = PopArtColors.Cyan
            )
        }
    }
}
