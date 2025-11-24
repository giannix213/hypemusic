package com.metu.hypematch.auth

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.metu.hypematch.AnimatedMusicLogo
import com.metu.hypematch.AuthManager
import com.metu.hypematch.DevConfig
import com.metu.hypematch.PopArtColors
import com.metu.hypematch.auth.components.*

@Composable
fun AuthScreenOptimized(
    onAuthSuccess: () -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val authManager = remember { AuthManager(context) }
    val viewModel = remember { AuthViewModel(authManager) }
    val uiState by viewModel.uiState.collectAsState()
    
    var showForgotPassword by remember { mutableStateOf(false) }

    // Launcher para Google Sign-In
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        viewModel.handleGoogleSignInResult(result.data, onAuthSuccess)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PopArtColors.Black)
            .statusBarsPadding()
            .navigationBarsPadding()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = when (uiState.stage) {
                is AuthStage.Initial -> Arrangement.Center
                else -> Arrangement.Top
            },
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(40.dp)
        ) {
            // Espaciador superior cuando no está en modo inicial
            if (uiState.stage !is AuthStage.Initial) {
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
                when (uiState.stage) {
                    is AuthStage.SignUp -> "Crea tu cuenta"
                    is AuthStage.Login -> "Inicia sesión"
                    else -> "Inicia sesión para descubrir música"
                },
                fontSize = 16.sp,
                color = PopArtColors.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            // Contenido según el estado
            when (val stage = uiState.stage) {
                is AuthStage.Initial -> {
                    AuthInitialScreen(
                        isGoogleSignInAvailable = uiState.isGoogleSignInAvailable,
                        isLoading = uiState.isLoading,
                        onSignUpClick = { viewModel.navigateToSignUp() },
                        onLoginClick = { viewModel.navigateToLogin() },
                        onGoogleSignInClick = {
                            viewModel.getGoogleSignInIntent()?.let { intent ->
                                googleSignInLauncher.launch(intent)
                            }
                        },
                        onGuestClick = { viewModel.signInAnonymously(onAuthSuccess) }
                    )
                }

                is AuthStage.Login, is AuthStage.SignUp -> {
                    AuthEmailForm(
                        isSignUp = stage is AuthStage.SignUp,
                        email = uiState.email,
                        password = uiState.password,
                        confirmPassword = uiState.confirmPassword,
                        rememberMe = uiState.rememberMe,
                        isLoading = uiState.isLoading,
                        onEmailChange = viewModel::onEmailChange,
                        onPasswordChange = viewModel::onPasswordChange,
                        onConfirmPasswordChange = viewModel::onConfirmPasswordChange,
                        onRememberMeChange = viewModel::onRememberMeChange,
                        onForgotPassword = { showForgotPassword = true },
                        onSubmit = {
                            if (stage is AuthStage.SignUp) {
                                viewModel.signUpWithEmail(onAuthSuccess)
                            } else {
                                viewModel.signInWithEmail(onAuthSuccess)
                            }
                        },
                        onBack = { viewModel.navigateToInitial() },
                        onSwitchMode = { viewModel.switchAuthMode() }
                    )
                }

                is AuthStage.EmailVerification -> {
                    EmailVerificationScreen(
                        email = stage.email,
                        isLoading = uiState.isLoading,
                        onCheckVerification = {
                            // Modo desarrollo: saltar verificación
                            if (DevConfig.SKIP_EMAIL_VERIFICATION) {
                                onAuthSuccess()
                            } else {
                                viewModel.checkEmailVerification(onAuthSuccess)
                            }
                        },
                        onResendEmail = { viewModel.resendVerificationEmail() },
                        onBack = {
                            viewModel.signOut()
                            viewModel.navigateToInitial()
                        }
                    )
                }
            }

            // Indicador de carga
            if (uiState.isLoading) {
                Spacer(Modifier.height(24.dp))
                CircularProgressIndicator(
                    color = PopArtColors.Yellow,
                    modifier = Modifier.size(40.dp)
                )
            }

            // Mensaje de error
            if (uiState.errorMessage.isNotEmpty()) {
                Spacer(Modifier.height(24.dp))
                Text(
                    uiState.errorMessage,
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
            onDismiss = { showForgotPassword = false },
            onSendEmail = { email ->
                viewModel.resetPassword(email) {
                    showForgotPassword = false
                }
            }
        )
    }
}
