package com.metu.hypematch

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Estados posibles de la vista antes de iniciar el Live
sealed class LiveState {
    object Idle : LiveState()
    object Loading : LiveState()
    data class Error(val message: String) : LiveState()
    data class SessionReady(val session: LiveSession) : LiveState()
}

class LiveViewModel(
    private val firebaseManager: FirebaseManager,
    private val currentUserId: String,
    private val currentUsername: String,
    private val currentUserProfilePic: String
) : ViewModel() {
    
    private val _liveState = MutableStateFlow<LiveState>(LiveState.Idle)
    val liveState: StateFlow<LiveState> = _liveState
    
    /**
     * Llama a Firebase para crear una nueva sesión de Live y obtener el token de Agora.
     */
    fun startLiveSetup(title: String = "Mi Live en Hype Match") {
        if (_liveState.value is LiveState.Loading) return
        
        _liveState.value = LiveState.Loading
        android.util.Log.d("LiveViewModel", "🎬 Iniciando setup de Live...")
        
        viewModelScope.launch {
            try {
                val session = firebaseManager.startNewLiveSession(
                    userId = currentUserId,
                    username = currentUsername,
                    profileImageUrl = currentUserProfilePic,
                    title = title
                )
                
                if (session != null) {
                    // Si Firebase devuelve la sesión (incluyendo el token de Agora)
                    android.util.Log.d("LiveViewModel", "✅ Sesión creada: ${session.sessionId}")
                    android.util.Log.d("LiveViewModel", "📺 Canal Agora: ${session.agoraChannelName}")
                    _liveState.value = LiveState.SessionReady(session)
                } else {
                    // Si la llamada a Firebase falló
                    android.util.Log.e("LiveViewModel", "❌ Fallo al crear sesión")
                    _liveState.value = LiveState.Error("No se pudo iniciar la sesión de Live")
                }
            } catch (e: Exception) {
                android.util.Log.e("LiveViewModel", "❌ Error en startLiveSetup: ${e.message}", e)
                _liveState.value = LiveState.Error("Error: ${e.message ?: "Desconocido"}")
            }
        }
    }
    
    /**
     * Llama a Firebase para finalizar el Live.
     */
    fun endLive(sessionId: String) {
        android.util.Log.d("LiveViewModel", "🛑 Finalizando Live: $sessionId")
        viewModelScope.launch {
            try {
                firebaseManager.endLiveSession(sessionId)
                android.util.Log.d("LiveViewModel", "✅ Live finalizado correctamente")
                // Resetear el estado para que el usuario pueda volver a iniciar un Live
                _liveState.value = LiveState.Idle
            } catch (e: Exception) {
                android.util.Log.e("LiveViewModel", "❌ Error al finalizar Live: ${e.message}", e)
                // Aún así reseteamos el estado
                _liveState.value = LiveState.Idle
            }
        }
    }
    
    /**
     * Resetear el estado a Idle
     */
    fun reset() {
        _liveState.value = LiveState.Idle
    }
}
