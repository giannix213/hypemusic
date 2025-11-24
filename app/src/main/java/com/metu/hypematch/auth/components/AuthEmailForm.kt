package com.metu.hypematch.auth.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metu.hypematch.PopArtColors
import com.metu.hypematch.R

@Composable
fun AuthEmailForm(
    isSignUp: Boolean,
    email: String,
    password: String,
    confirmPassword: String,
    rememberMe: Boolean,
    isLoading: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onRememberMeChange: (Boolean) -> Unit,
    onForgotPassword: () -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
    onSwitchMode: () -> Unit
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Botón volver
        IconButton(
            onClick = onBack,
            modifier = Modifier.align(Alignment.Start)
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint = PopArtColors.White
            )
        }

        Spacer(Modifier.height(16.dp))

        // Campo Email
        OutlinedTextField(
            value = email,
            onValueChange = onEmailChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Email") },
            placeholder = { Text("usuario@ejemplo.com", color = PopArtColors.White.copy(alpha = 0.5f)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = PopArtColors.White,
                unfocusedTextColor = PopArtColors.White,
                focusedBorderColor = PopArtColors.Yellow,
                unfocusedBorderColor = PopArtColors.White,
                focusedLabelColor = PopArtColors.Yellow,
                unfocusedLabelColor = PopArtColors.White,
                cursorColor = PopArtColors.Yellow
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(16.dp))

        // Campo Password
        OutlinedTextField(
            value = password,
            onValueChange = onPasswordChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Contraseña") },
            singleLine = true,
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        painter = painterResource(
                            id = if (passwordVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility
                        ),
                        contentDescription = if (passwordVisible) "Ocultar" else "Mostrar",
                        tint = PopArtColors.White
                    )
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = PopArtColors.White,
                unfocusedTextColor = PopArtColors.White,
                focusedBorderColor = PopArtColors.Yellow,
                unfocusedBorderColor = PopArtColors.White,
                focusedLabelColor = PopArtColors.Yellow,
                unfocusedLabelColor = PopArtColors.White,
                cursorColor = PopArtColors.Yellow
            ),
            shape = RoundedCornerShape(12.dp)
        )

        // Campo Confirmar Password (solo en Sign Up)
        if (isSignUp) {
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = confirmPassword,
                onValueChange = onConfirmPasswordChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Confirmar contraseña") },
                singleLine = true,
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            painter = painterResource(
                                id = if (confirmPasswordVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility
                            ),
                            contentDescription = if (confirmPasswordVisible) "Ocultar" else "Mostrar",
                            tint = PopArtColors.White
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = PopArtColors.White,
                    unfocusedTextColor = PopArtColors.White,
                    focusedBorderColor = PopArtColors.Yellow,
                    unfocusedBorderColor = PopArtColors.White,
                    focusedLabelColor = PopArtColors.Yellow,
                    unfocusedLabelColor = PopArtColors.White,
                    cursorColor = PopArtColors.Yellow
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        // Recordarme y Olvidaste contraseña (solo en Login)
        if (!isSignUp) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = onRememberMeChange,
                        colors = CheckboxDefaults.colors(
                            checkedColor = PopArtColors.Yellow,
                            uncheckedColor = PopArtColors.White,
                            checkmarkColor = PopArtColors.Black
                        )
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Recordarme", color = PopArtColors.White, fontSize = 14.sp)
                }

                TextButton(onClick = onForgotPassword) {
                    Text(
                        "¿Olvidaste tu contraseña?",
                        color = PopArtColors.Yellow,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Botón Submit
        Button(
            onClick = onSubmit,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            colors = ButtonDefaults.buttonColors(containerColor = PopArtColors.Yellow),
            shape = RoundedCornerShape(30.dp),
            enabled = !isLoading && email.isNotEmpty() && password.isNotEmpty() &&
                    (!isSignUp || confirmPassword.isNotEmpty())
        ) {
            Text(
                if (isSignUp) "CREAR CUENTA" else "INICIAR SESIÓN",
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = PopArtColors.Black
            )
        }

        Spacer(Modifier.height(16.dp))

        // Cambiar entre Sign Up y Login
        TextButton(
            onClick = onSwitchMode,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text(
                if (isSignUp) "¿Ya tienes cuenta? Inicia sesión" else "¿No tienes cuenta? Regístrate",
                fontSize = 14.sp,
                color = PopArtColors.Cyan
            )
        }
    }
}
