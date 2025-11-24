package com.metu.hypematch.auth

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.metu.hypematch.AuthManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authManager: AuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(isGoogleSignInAvailable = authManager.isGoogleSignInAvailable()) }
    }

    // Eventos de UI
    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, errorMessage = "") }
    }

    fun onPasswordChange(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = "") }
    }

    fun onConfirmPasswordChange(confirmPassword: String) {
        _uiState.update { it.copy(confirmPassword = confirmPassword, errorMessage = "") }
    }

    fun onRememberMeChange(rememberMe: Boolean) {
        _uiState.update { it.copy(rememberMe = rememberMe) }
    }

    fun navigateToLogin() {
        _uiState.update { it.copy(stage = AuthStage.Login, errorMessage = "") }
    }

    fun navigateToSignUp() {
        _uiState.update { it.copy(stage = AuthStage.SignUp, errorMessage = "") }
    }

    fun navigateToInitial() {
        _uiState.update {
            AuthUiState(isGoogleSignInAvailable = authManager.isGoogleSignInAvailable())
        }
    }

    fun switchAuthMode() {
        _uiState.update {
            it.copy(
                stage = if (it.stage is AuthStage.Login) AuthStage.SignUp else AuthStage.Login,
                errorMessage = ""
            )
        }
    }

    // Validaciones
    private fun validateEmail(email: String): String? {
        if (email.isBlank()) return "El email no puede estar vacío"
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        if (!email.matches(emailPattern.toRegex())) {
            return "Email inválido. Usa el formato: usuario@dominio.com"
        }
        return null
    }

    private fun validatePassword(password: String): String? {
        if (password.isBlank()) return "La contraseña no puede estar vacía"
        if (password.length < 6) return "La contraseña debe tener al menos 6 caracteres"
        return null
    }

    // Acciones de autenticación
    fun signInAnonymously(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = "") }
            try {
                val user = authManager.signInAnonymously()
                if (user != null) {
                    onSuccess()
                } else {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Error: Usuario nulo") }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Error al iniciar sesión: ${e.message}")
                }
            }
        }
    }

    fun getGoogleSignInIntent(): Intent? {
        return try {
            authManager.getGoogleSignInIntent()
        } catch (e: Exception) {
            _uiState.update { it.copy(errorMessage = "Error al iniciar Google Sign-In: ${e.message}") }
            null
        }
    }

    fun handleGoogleSignInResult(data: Intent?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = "") }
            try {
                authManager.handleGoogleSignInResult(data)
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Error con Google: ${e.message}")
                }
            }
        }
    }

    fun signInWithEmail(onSuccess: () -> Unit) {
        val state = _uiState.value
        
        // Validar
        validateEmail(state.email)?.let { error ->
            _uiState.update { it.copy(errorMessage = error) }
            return
        }
        validatePassword(state.password)?.let { error ->
            _uiState.update { it.copy(errorMessage = error) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = "") }
            try {
                authManager.signInWithEmail(state.email, state.password)
                
                // Verificar si el email está verificado
                if (!authManager.isEmailVerified()) {
                    _uiState.update {
                        it.copy(
                            stage = AuthStage.EmailVerification(state.email),
                            isLoading = false
                        )
                    }
                } else {
                    onSuccess()
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = translateFirebaseError(e))
                }
            }
        }
    }

    fun signUpWithEmail(onSuccess: () -> Unit) {
        val state = _uiState.value
        
        // Validar
        validateEmail(state.email)?.let { error ->
            _uiState.update { it.copy(errorMessage = error) }
            return
        }
        validatePassword(state.password)?.let { error ->
            _uiState.update { it.copy(errorMessage = error) }
            return
        }
        if (state.password != state.confirmPassword) {
            _uiState.update { it.copy(errorMessage = "Las contraseñas no coinciden") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = "") }
            try {
                authManager.signUpWithEmail(state.email, state.password)
                _uiState.update {
                    it.copy(
                        stage = AuthStage.EmailVerification(state.email),
                        isLoading = false
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = translateFirebaseError(e))
                }
            }
        }
    }

    fun checkEmailVerification(onSuccess: () -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = "") }
            try {
                authManager.reloadUser()
                if (authManager.isEmailVerified()) {
                    onSuccess()
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "El email aún no ha sido verificado. Revisa tu bandeja de entrada."
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Error al verificar: ${e.message}")
                }
            }
        }
    }

    fun resendVerificationEmail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = "") }
            try {
                authManager.sendEmailVerification()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "✅ Email de verificación reenviado"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Error: ${e.message}")
                }
            }
        }
    }

    fun resetPassword(email: String, onSuccess: () -> Unit) {
        validateEmail(email)?.let { error ->
            _uiState.update { it.copy(errorMessage = error) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = "") }
            try {
                authManager.resetPassword(email)
                _uiState.update { it.copy(isLoading = false) }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = translateFirebaseError(e))
                }
            }
        }
    }

    fun signOut() {
        authManager.signOut()
        navigateToInitial()
    }

    private fun translateFirebaseError(e: Exception): String {
        return when {
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
            e.message?.contains("invalid email") == true ->
                "Email inválido. Usa el formato: usuario@dominio.com"
            else -> e.message ?: "Error desconocido"
        }
    }
}
