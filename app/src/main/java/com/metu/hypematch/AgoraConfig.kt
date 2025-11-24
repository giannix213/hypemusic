package com.metu.hypematch

/**
 * Configuración de Agora para Live Streaming
 * 
 * IMPORTANTE: En producción, el App Certificate debe estar SOLO en el backend
 * y los tokens deben generarse mediante Cloud Functions.
 */
object AgoraConfig {
    // App ID de Agora (público, puede estar en el cliente)
    const val APP_ID = "72117baf2c874766b556e6f83ac9c58d"
    
    // NOTA: El App Certificate (f907826ae8ff4c00b7057d15b6f2e628) 
    // NO debe estar aquí. Debe estar solo en tu Cloud Function.
    
    // Configuración de canal
    const val CHANNEL_PROFILE_LIVE_BROADCASTING = 1
    const val CLIENT_ROLE_BROADCASTER = 1
    const val CLIENT_ROLE_AUDIENCE = 2
    
    // Configuración de video
    const val VIDEO_ENCODING_WIDTH = 720
    const val VIDEO_ENCODING_HEIGHT = 1280
    const val VIDEO_ENCODING_BITRATE = 2000
    const val VIDEO_ENCODING_FRAME_RATE = 30
    
    // Configuración de audio
    const val AUDIO_SCENARIO_GAME_STREAMING = 3
}
