package com.metu.hypematch

/**
 * Representa una sesión de Live activa con toda la información necesaria
 * para conectarse a Agora y gestionar la transmisión.
 * 
 * IMPORTANTE: Todos los parámetros tienen valores por defecto para que
 * Firestore pueda deserializar correctamente los documentos.
 */
data class LiveSession(
    val sessionId: String = "",              // ID único de la sesión en Firestore
    val userId: String = "",                 // ID del usuario que transmite
    val username: String = "",               // Nombre del usuario
    val profileImageUrl: String = "",        // URL de la foto de perfil
    val title: String = "",                  // Título del Live
    val agoraChannelName: String = "",       // Nombre del canal de Agora
    val agoraToken: String = "",             // Token de Agora para autenticación
    val startTime: Long = 0L,                // Timestamp de inicio
    val isActive: Boolean = false,           // Si la sesión está activa
    val viewerCount: Int = 0                 // Número de espectadores actuales
)
