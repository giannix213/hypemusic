package com.metu.hypematch

import android.content.Context
import android.content.Intent
import androidx.activity.result.ActivityResultLauncher
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class AuthManager(private val context: Context) {
    private val auth = FirebaseAuth.getInstance()
    private var googleSignInClient: GoogleSignInClient? = null

    init {
        // Configurar Google Sign-In - ACTIVADO con SHA configurados
        try {
            val clientId = context.getString(R.string.default_web_client_id)
            if (clientId != "YOUR_WEB_CLIENT_ID_HERE") {
                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(clientId)
                    .requestEmail()
                    .build()
                
                googleSignInClient = GoogleSignIn.getClient(context, gso)
                android.util.Log.d("AuthManager", "✅ Google Sign-In configurado correctamente")
            } else {
                googleSignInClient = null
                android.util.Log.w("AuthManager", "⚠️ Web Client ID no configurado")
            }
        } catch (e: Exception) {
            googleSignInClient = null
            android.util.Log.e("AuthManager", "❌ Error configurando Google Sign-In: ${e.message}", e)
        }
    }
    
    // Verificar si Google Sign-In está disponible
    fun isGoogleSignInAvailable(): Boolean {
        return googleSignInClient != null
    }

    // Obtener usuario actual
    fun getCurrentUser(): FirebaseUser? {
        return auth.currentUser
    }

    // Verificar si hay sesión activa
    fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    // Iniciar sesión anónima (para invitados)
    suspend fun signInAnonymously(): FirebaseUser? {
        return try {
            val result = auth.signInAnonymously().await()
            result.user
        } catch (e: Exception) {
            // Log detallado del error
            android.util.Log.e("AuthManager", "Error en signInAnonymously: ${e.message}", e)
            throw Exception("Error al iniciar sesión anónima: ${e.javaClass.simpleName} - ${e.message}")
        }
    }

    // Obtener Intent para Google Sign-In
    fun getGoogleSignInIntent(): Intent {
        return googleSignInClient?.signInIntent ?: throw Exception("Google Sign-In no configurado")
    }

    // Procesar resultado de Google Sign-In
    suspend fun handleGoogleSignInResult(data: Intent?): FirebaseUser? {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account)
        } catch (e: ApiException) {
            throw Exception("Error en Google Sign-In: ${e.message}")
        }
    }

    // Autenticar con Firebase usando credenciales de Google
    private suspend fun firebaseAuthWithGoogle(account: GoogleSignInAccount?): FirebaseUser? {
        if (account == null) throw Exception("Cuenta de Google nula")
        
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        val result = auth.signInWithCredential(credential).await()
        return result.user
    }

    // Obtener ID del usuario
    fun getUserId(): String? {
        return auth.currentUser?.uid
    }

    // Obtener nombre del usuario
    fun getUserName(): String {
        return auth.currentUser?.displayName ?: "Usuario"
    }

    // Obtener email del usuario
    fun getUserEmail(): String? {
        return auth.currentUser?.email
    }

    // Verificar si es usuario anónimo
    fun isAnonymous(): Boolean {
        return auth.currentUser?.isAnonymous ?: false
    }

    // Registrarse con email y contraseña
    suspend fun signUpWithEmail(email: String, password: String): FirebaseUser? {
        return try {
            if (email.isBlank() || password.isBlank()) {
                throw Exception("Email y contraseña no pueden estar vacíos")
            }
            if (password.length < 6) {
                throw Exception("La contraseña debe tener al menos 6 caracteres")
            }
            
            android.util.Log.d("AuthManager", "Creando usuario con email: $email")
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            android.util.Log.d("AuthManager", "Usuario creado exitosamente: ${result.user?.uid}")
            
            // Enviar email de verificación automáticamente
            android.util.Log.d("AuthManager", "Enviando email de verificación a: ${result.user?.email}")
            result.user?.sendEmailVerification()?.await()
            android.util.Log.d("AuthManager", "✅ Email de verificación enviado exitosamente")
            
            result.user
        } catch (e: Exception) {
            android.util.Log.e("AuthManager", "❌ Error en signUpWithEmail: ${e.message}", e)
            throw Exception("Error al crear cuenta: ${e.message}")
        }
    }

    // Iniciar sesión con email y contraseña
    suspend fun signInWithEmail(email: String, password: String): FirebaseUser? {
        return try {
            if (email.isBlank() || password.isBlank()) {
                throw Exception("Email y contraseña no pueden estar vacíos")
            }
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user
        } catch (e: Exception) {
            android.util.Log.e("AuthManager", "Error en signInWithEmail: ${e.message}", e)
            throw Exception("Error al iniciar sesión: ${e.message}")
        }
    }
    
    // Recuperar contraseña
    suspend fun resetPassword(email: String) {
        try {
            if (email.isBlank()) {
                throw Exception("El email no puede estar vacío")
            }
            
            android.util.Log.d("AuthManager", "🔄 Intentando enviar email de recuperación a: $email")
            auth.sendPasswordResetEmail(email).await()
            android.util.Log.d("AuthManager", "✅ Email de recuperación enviado exitosamente a: $email")
            android.util.Log.d("AuthManager", "📧 Revisa tu bandeja de entrada y spam")
        } catch (e: Exception) {
            android.util.Log.e("AuthManager", "❌ Error en resetPassword para $email: ${e.message}", e)
            android.util.Log.e("AuthManager", "Tipo de error: ${e.javaClass.simpleName}")
            throw Exception("Error al enviar email de recuperación: ${e.message}")
        }
    }
    
    // Verificar si el email está verificado
    fun isEmailVerified(): Boolean {
        return auth.currentUser?.isEmailVerified ?: false
    }
    
    // Reenviar email de verificación
    suspend fun sendEmailVerification() {
        try {
            val user = auth.currentUser
            if (user == null) {
                android.util.Log.e("AuthManager", "❌ No hay usuario autenticado")
                throw Exception("No hay usuario autenticado")
            }
            if (user.isEmailVerified) {
                android.util.Log.w("AuthManager", "⚠️ El email ya está verificado")
                throw Exception("El email ya está verificado")
            }
            
            android.util.Log.d("AuthManager", "Reenviando email de verificación a: ${user.email}")
            user.sendEmailVerification().await()
            android.util.Log.d("AuthManager", "✅ Email de verificación reenviado exitosamente")
        } catch (e: Exception) {
            android.util.Log.e("AuthManager", "❌ Error en sendEmailVerification: ${e.message}", e)
            throw Exception("Error al enviar email de verificación: ${e.message}")
        }
    }
    
    // Recargar información del usuario (para actualizar estado de verificación)
    suspend fun reloadUser() {
        try {
            auth.currentUser?.reload()?.await()
        } catch (e: Exception) {
            android.util.Log.e("AuthManager", "Error en reloadUser: ${e.message}", e)
            throw Exception("Error al actualizar información del usuario: ${e.message}")
        }
    }
    
    // Eliminar cuenta de autenticación
    suspend fun deleteAccount() {
        try {
            auth.currentUser?.delete()?.await()
            android.util.Log.d("AuthManager", "✅ Cuenta de autenticación eliminada")
        } catch (e: Exception) {
            android.util.Log.e("AuthManager", "❌ Error eliminando cuenta de autenticación: ${e.message}", e)
            throw Exception("Error al eliminar cuenta: ${e.message}")
        }
    }
    
    // Cerrar sesión
    fun signOut() {
        try {
            // Cerrar sesión de Firebase
            auth.signOut()
            
            // Cerrar sesión de Google (si está configurado)
            googleSignInClient?.let { client ->
                try {
                    client.signOut()
                } catch (e: Exception) {
                    android.util.Log.w("AuthManager", "No se pudo cerrar sesión de Google: ${e.message}")
                }
            }
            
            android.util.Log.d("AuthManager", "✅ Sesión cerrada correctamente")
        } catch (e: Exception) {
            android.util.Log.e("AuthManager", "❌ Error cerrando sesión: ${e.message}", e)
        }
    }
}
