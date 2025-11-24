package com.metu.hypematch.auth

// Máquina de estados para la pantalla de autenticación
sealed class AuthStage {
    object Initial : AuthStage()
    object Login : AuthStage()
    object SignUp : AuthStage()
    data class EmailVerification(val email: String) : AuthStage()
}

// Estado de la UI
data class AuthUiState(
    val stage: AuthStage = AuthStage.Initial,
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val rememberMe: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String = "",
    val isGoogleSignInAvailable: Boolean = false
)
