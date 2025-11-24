package com.metu.hypematch.auth.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metu.hypematch.PopArtColors

@Composable
fun ForgotPasswordDialog(
    onDismiss: () -> Unit,
    onSendEmail: (String) -> Unit
) {
    var resetEmail by remember { mutableStateOf("") }
    var emailSent by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                if (emailSent) "Email Enviado" else "Recuperar Contraseña",
                fontWeight = FontWeight.Bold,
                color = PopArtColors.Black
            )
        },
        text = {
            Column {
                if (emailSent) {
                    Icon(
                        Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = PopArtColors.Yellow,
                        modifier = Modifier
                            .size(48.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Hemos enviado un enlace de recuperación a:",
                        color = PopArtColors.Black,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        resetEmail,
                        color = PopArtColors.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Por favor:\n1. Revisa tu bandeja de entrada\n2. Abre el enlace del email\n3. Crea tu nueva contraseña\n4. Regresa aquí e inicia sesión",
                        color = PopArtColors.Black.copy(alpha = 0.8f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(Modifier.height(12.dp))
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = PopArtColors.Yellow.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                "⚠️ IMPORTANTE",
                                color = PopArtColors.Black,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "El email probablemente está en tu carpeta de SPAM o Correo no deseado",
                                color = PopArtColors.Black.copy(alpha = 0.8f),
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 16.sp
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Busca emails de: noreply@hype-13966.firebaseapp.com",
                                color = PopArtColors.Black.copy(alpha = 0.6f),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Text(
                        "Ingresa tu email y te enviaremos un enlace para restablecer tu contraseña.",
                        color = PopArtColors.Black,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    OutlinedTextField(
                        value = resetEmail,
                        onValueChange = { resetEmail = it },
                        label = { Text("Email") },
                        placeholder = { Text("usuario@ejemplo.com") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PopArtColors.Yellow,
                            unfocusedBorderColor = PopArtColors.Black.copy(alpha = 0.5f),
                            focusedLabelColor = PopArtColors.Yellow,
                            unfocusedLabelColor = PopArtColors.Black.copy(alpha = 0.7f),
                            cursorColor = PopArtColors.Yellow
                        )
                    )
                }
            }
        },
        confirmButton = {
            if (emailSent) {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = PopArtColors.Yellow)
                ) {
                    Text("Entendido", color = PopArtColors.Black, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = {
                        if (resetEmail.isNotBlank()) {
                            onSendEmail(resetEmail)
                            emailSent = true
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PopArtColors.Yellow),
                    enabled = resetEmail.isNotBlank()
                ) {
                    Text("Enviar Email", color = PopArtColors.Black, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            if (!emailSent) {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar", color = PopArtColors.Black.copy(alpha = 0.7f))
                }
            }
        },
        containerColor = PopArtColors.White
    )
}
