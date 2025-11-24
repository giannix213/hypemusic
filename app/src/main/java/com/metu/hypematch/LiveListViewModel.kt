package com.metu.hypematch

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel para gestionar la lista de Lives activos.
 * Observa Firestore en tiempo real y actualiza la UI automáticamente.
 */
class LiveListViewModel(
    private val firebaseManager: FirebaseManager = FirebaseManager()
) : ViewModel() {
    
    // Estado privado mutable
    private val _liveSessions = MutableStateFlow<List<LiveSession>>(emptyList())
    
    // Estado público inmutable para la UI
    val liveSessions: StateFlow<List<LiveSession>> = _liveSessions.asStateFlow()
    
    // Listener de Firestore
    private var liveListener: ListenerRegistration? = null
    
    init {
        android.util.Log.d("LiveListViewModel", "🎬 Inicializando ViewModel de Lives")
        startObservingLives()
    }
    
    /**
     * Inicia la observación de Lives activos en Firestore
     */
    private fun startObservingLives() {
        // Limpiar listener anterior si existe
        liveListener?.remove()
        
        android.util.Log.d("LiveListViewModel", "👀 Iniciando observación de Lives...")
        
        // Crear nuevo listener
        liveListener = firebaseManager.observeLiveSessions { sessions ->
            android.util.Log.d("LiveListViewModel", "📡 Actualización recibida: ${sessions.size} Lives")
            _liveSessions.value = sessions
        }
    }
    
    /**
     * Recargar manualmente la lista de Lives
     */
    fun refresh() {
        android.util.Log.d("LiveListViewModel", "🔄 Recargando Lives...")
        startObservingLives()
    }
    
    /**
     * Limpieza: Se llama cuando el ViewModel está a punto de ser destruido.
     * Es crucial detener la escucha de Firestore para evitar pérdidas de memoria.
     */
    override fun onCleared() {
        super.onCleared()
        android.util.Log.d("LiveListViewModel", "🧹 Limpiando ViewModel - Deteniendo listener")
        liveListener?.remove()
        liveListener = null
    }
}
