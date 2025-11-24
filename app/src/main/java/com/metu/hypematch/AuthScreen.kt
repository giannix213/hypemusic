package com.metu.hypematch

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun AuthScreen(
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val authManager = remember { AuthManager(context) }
    val focusManager = LocalFocusManager.current
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }
    var showEmailAuth by remember { mutableStateOf(false) }
    var isSignUp by remember { mutableStateOf(true) } // true = Sign Up, false = Login
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }
    var showForgotPassword by remember { mutableStateOf(false) }
    var showEmailVerification by remember { mutableStateOf(false) }
    var verificationEmail by remember { mutableStateOf("") }

    // Launcher para Google Sign-In
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isLoading = true
        scope.launch {
            try {
                authManager.handleGoogleSignInResult(result.data)
                isLoading = false
                onAuthSuccess()
            } catch (e: Exception) {
                errorMessage = "Error al iniciar sesión con Google:\n${e.message}"
                isLoading = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PopArtColors.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = if (showEmailAuth) Arrangement.Top else Arrangement.Center,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(40.dp)
        ) {
            // Espaciador superior cuando está en modo formulario
            if (showEmailAuth) {
                Spacer(Modifier.height(40.dp))
            }
            
            // Logo
            AnimatedMusicLogo(
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 24.dp)
            )

            Text(
                "HYPE",
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                color = PopArtColors.Yellow,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                if (showEmailAuth) {
                    if (isSignUp) "Crea tu cuenta" else "Inicia sesión"
                } else {
                    "Inicia sesión para descubrir música"
                },
                fontSize = 16.sp,
                color = PopArtColors.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            if (showEmailVerification) {
                // Pantalla de verificación de email
                EmailVerificationScreen(
                    email = verificationEmail,
                    onCheckVerification = {
                        // Modo de desarrollo: saltar verificación
                        if (DevConfig.SKIP_EMAIL_VERIFICATION) {
                            android.util.Log.w("AuthScreen", "⚠️ MODO DESARROLLO: Saltando verificación de email")
                            onAuthSuccess()
                            return@EmailVerificationScreen
                        }
                        
                        // Modo normal: verificar email
                        isLoading = true
                        errorMessage = ""
                        scope.launch {
                            try {
                                authManager.reloadUser()
                                if (authManager.isEmailVerified()) {
                                    isLoading = false
                                    onAuthSuccess()
                                } else {
                                    errorMessage = "El email aún no ha sido verificado. Por favor revisa tu bandeja de entrada."
                                    isLoading = false
                                }
                            } catch (e: Exception) {
                                errorMessage = "Error al verificar: ${e.message}"
                                isLoading = false
                            }
                        }
                    },
                    onResendEmail = {
                        isLoading = true
                        errorMessage = ""
                        scope.launch {
                            try {
                                authManager.sendEmailVerification()
                                errorMessage = "✅ Email de verificación reenviado a $verificationEmail"
                                isLoading = false
                            } catch (e: Exception) {
                                errorMessage = "Error al reenviar email: ${e.message}"
                                isLoading = false
                            }
                        }
                    },
                    onBack = {
                        authManager.signOut()
                        showEmailVerification = false
                        showEmailAuth = false
                        email = ""
                        password = ""
                        confirmPassword = ""
                        errorMessage = ""
                    },
                    isLoading = isLoading
                )
            } else if (!showEmailAuth) {
                // Pantalla inicial con opciones
                
                // Botón Sign Up
                Button(
                    onClick = {
                        showEmailAuth = true
                        isSignUp = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PopArtColors.Yellow),
                    shape = RoundedCornerShape(30.dp)
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
                    onClick = {
                        showEmailAuth = true
                        isSignUp = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = PopArtColors.White
                    ),
                    shape = RoundedCornerShape(30.dp),
                    border = androidx.compose.foundation.BorderStroke(2.dp, PopArtColors.White)
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
                    Divider(
                        modifier = Modifier.weight(1f),
                        color = PopArtColors.White.copy(alpha = 0.3f)
                    )
                    Text(
                        "  O  ",
                        color = PopArtColors.White.copy(alpha = 0.5f),
                        fontSize = 14.sp
                    )
                    Divider(
                        modifier = Modifier.weight(1f),
                        color = PopArtColors.White.copy(alpha = 0.3f)
                    )
                }

                Spacer(Modifier.height(24.dp))

                // Botón Google
                Button(
                    onClick = {
                        isLoading = true
                        errorMessage = ""
                        try {
                            if (authManager.isGoogleSignInAvailable()) {
                                val signInIntent = authManager.getGoogleSignInIntent()
                                googleSignInLauncher.launch(signInIntent)
                            } else {
                                errorMessage = "Google Sign-In no está configurado. Por favor configura tu proyecto en Firebase."
                                isLoading = false
                            }
                        } catch (e: Exception) {
                            errorMessage = "Error al iniciar Google Sign-In: ${e.message}"
                            isLoading = false
                        }
                    },
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

                // Botón Invitado
                TextButton(
                    onClick = {
                        isLoading = true
                        errorMessage = ""
                        scope.launch {
                            try {
                                val user = authManager.signInAnonymously()
                                if (user != null) {
                                    isLoading = false
                                    onAuthSuccess()
                                } else {
                                    errorMessage = "Error: Usuario nulo después de login"
                                    isLoading = false
                                }
                            } catch (e: Exception) {
                                errorMessage = "Error al iniciar sesión:\n${e.message}"
                                isLoading = false
                                android.util.Log.e("AuthScreen", "Error en login anónimo", e)
                            }
                        }
                    },
                    enabled = !isLoading
                ) {
                    Text(
                        "Continuar como invitado",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PopArtColors.Cyan
                    )
                }
            } else {
                // Formulario de Email/Password
                EmailAuthForm(
                    isSignUp = isSignUp,
                    email = email,
                    password = password,
                    confirmPassword = confirmPassword,
                    rememberMe = rememberMe,
                    onEmailChange = { email = it },
                    onPasswordChange = { password = it },
                    onConfirmPasswordChange = { confirmPassword = it },
                    onRememberMeChange = { rememberMe = it },
                    onForgotPassword = { showForgotPassword = true },
                    onSubmit = {
                        // Validar formato de email
                        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
                        if (!email.matches(emailPattern.toRegex())) {
                            errorMessage = "Por favor ingresa un email válido (ejemplo: usuario@gmail.com)"
                            return@EmailAuthForm
                        }
                        
                        if (password.length < 6) {
                            errorMessage = "La contraseña debe tener al menos 6 caracteres"
                            return@EmailAuthForm
                        }
                        
                        isLoading = true
                        errorMessage = ""
                        android.util.Log.d("AuthScreen", "Iniciando ${if (isSignUp) "registro" else "login"} con email: $email")
                        scope.launch {
                            try {
                                if (isSignUp) {
                                    if (password != confirmPassword) {
                                        errorMessage = "Las contraseñas no coinciden"
                                        isLoading = false
                                        android.util.Log.e("AuthScreen", "Las contraseñas no coinciden")
                                        return@launch
                                    }
                                    android.util.Log.d("AuthScreen", "Llamando a signUpWithEmail...")
                                    val user = authManager.signUpWithEmail(email, password)
                                    android.util.Log.d("AuthScreen", "Usuario creado: ${user?.uid}")
                                    
                                    // Mostrar pantalla de verificación de email
                                    verificationEmail = email
                                    showEmailVerification = true
                                    showEmailAuth = false
                                    isLoading = false
                                } else {
                                    android.util.Log.d("AuthScreen", "Llamando a signInWithEmail...")
                                    val user = authManager.signInWithEmail(email, password)
                                    android.util.Log.d("AuthScreen", "Usuario autenticado: ${user?.uid}")
                                    
                                    // Verificar si el email está verificado
                                    if (!authManager.isEmailVerified()) {
                                        verificationEmail = email
                                        showEmailVerification = true
                                        showEmailAuth = false
                                        isLoading = false
                                    } else {
                                        isLoading = false
                                        android.util.Log.d("AuthScreen", "Llamando a onAuthSuccess()")
                                        onAuthSuccess()
                                    }
                                }
                            } catch (e: Exception) {
                                // Traducir errores comunes de Firebase
                                errorMessage = when {
                                    e.message?.contains("badly formatted") == true -> 
                                        "Email inválido. Usa el formato: usuario@dominio.com"
                                    e.message?.contains("already in use") == true -> 
                                        "Este email ya está registrado. Intenta iniciar sesión."
                                    e.message?.contains("user not found") == true -> 
                                        "No existe una cuenta con este email"
                                    e.message?.contains("wrong password") == true -> 
                                        "Contraseña incorrecta"
                                    e.message?.contains("weak password") == true -> 
                                        "La contraseña es muy débil. Usa al menos 6 caracteres."
                                    else -> e.message ?: "Error desconocido"
                                }
                                isLoading = false
                                android.util.Log.e("AuthScreen", "Error en auth: ${e.message}", e)
                            }
                        }
                    },
                    onBack = {
                        showEmailAuth = false
                        email = ""
                        password = ""
                        confirmPassword = ""
                        errorMessage = ""
                    },
                    onSwitchMode = {
                        isSignUp = !isSignUp
                        errorMessage = ""
                    },
                    isLoading = isLoading
                )
            }

            // Indicador de carga
            if (isLoading) {
                Spacer(Modifier.height(24.dp))
                CircularProgressIndicator(
                    color = PopArtColors.Yellow,
                    modifier = Modifier.size(40.dp)
                )
            }

            // Mensaje de error
            if (errorMessage.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                Text(
                    errorMessage,
                    fontSize = 14.sp,
                    color = PopArtColors.Pink,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(32.dp))

            // Términos y condiciones
            Text(
                "Al iniciar sesión, aceptas nuestros\nTérminos y Política de privacidad",
                fontSize = 12.sp,
                color = PopArtColors.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )
        }
    }
    
    // Diálogo de recuperación de contraseña
    if (showForgotPassword) {
        ForgotPasswordDialog(
            onDismiss = { 
                showForgotPassword = false
                errorMessage = "" // Limpiar mensaje al cerrar
            },
            onSendEmail = { resetEmail ->
                scope.launch {
                    try {
                        isLoading = true
                        authManager.resetPassword(resetEmail)
                        isLoading = false
                        // El diálogo mostrará el mensaje de éxito internamente
                    } catch (e: Exception) {
                        showForgotPassword = false
                        errorMessage = when {
                            e.message?.contains("user not found") == true -> 
                                "No existe una cuenta con este email"
                            e.message?.contains("invalid email") == true -> 
                                "Email inválido. Usa el formato: usuario@dominio.com"
                            else -> "Error al enviar email: ${e.message}"
                        }
                        isLoading = false
                    }
                }
            }
        )
    }
}

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
                    Text(
                        "Entendido",
                        color = PopArtColors.Black,
                        fontWeight = FontWeight.Bold
                    )
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
                    Text(
                        "Enviar Email",
                        color = PopArtColors.Black,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        dismissButton = {
            if (!emailSent) {
                TextButton(onClick = onDismiss) {
                    Text(
                        "Cancelar",
                        color = PopArtColors.Black.copy(alpha = 0.7f)
                    )
                }
            }
        },
        containerColor = PopArtColors.White
    )
}

@Composable
fun EmailVerificationScreen(
    email: String,
    onCheckVerification: () -> Unit,
    onResendEmail: () -> Unit,
    onBack: () -> Unit,
    isLoading: Boolean
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
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = PopArtColors.White
            ),
            shape = RoundedCornerShape(30.dp),
            border = androidx.compose.foundation.BorderStroke(2.dp, PopArtColors.White),
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
        TextButton(
            onClick = onBack,
            enabled = !isLoading
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = null,
                tint = PopArtColors.Cyan,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "Volver al inicio",
                fontSize = 14.sp,
                color = PopArtColors.Cyan
            )
        }
        
        Spacer(Modifier.height(16.dp))
        
        // Nota sobre spam - MÁS VISIBLE
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

@Composable
fun EmailAuthForm(
    isSignUp: Boolean,
    email: String,
    password: String,
    confirmPassword: String,
    rememberMe: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onConfirmPasswordChange: (String) -> Unit,
    onRememberMeChange: (Boolean) -> Unit,
    onForgotPassword: () -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
    onSwitchMode: () -> Unit,
    isLoading: Boolean
) {
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectTapGestures(onTap = {
                    focusManager.clearFocus()
                })
            }
    ) {
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
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = androidx.compose.ui.text.input.KeyboardType.Email,
                imeAction = androidx.compose.ui.text.input.ImeAction.Next
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
            visualTransformation = if (passwordVisible) 
                androidx.compose.ui.text.input.VisualTransformation.None 
            else 
                androidx.compose.ui.text.input.PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        painter = painterResource(
                            id = if (passwordVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility
                        ),
                        contentDescription = if (passwordVisible) "Ocultar contraseña" else "Mostrar contraseña",
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
                visualTransformation = if (confirmPasswordVisible) 
                    androidx.compose.ui.text.input.VisualTransformation.None 
                else 
                    androidx.compose.ui.text.input.PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                        Icon(
                            painter = painterResource(
                                id = if (confirmPasswordVisible) R.drawable.ic_visibility_off else R.drawable.ic_visibility
                            ),
                            contentDescription = if (confirmPasswordVisible) "Ocultar contraseña" else "Mostrar contraseña",
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
                // Checkbox Recordarme
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
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
                    Text(
                        "Recordarme",
                        color = PopArtColors.White,
                        fontSize = 14.sp
                    )
                }
                
                // Enlace Olvidaste contraseña
                TextButton(
                    onClick = onForgotPassword
                ) {
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
