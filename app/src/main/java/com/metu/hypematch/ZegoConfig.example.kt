package com.metu.hypematch

/**
 * Configuración de ZegoCloud para Live Streaming
 * 
 * INSTRUCCIONES:
 * 1. Copia este archivo y renómbralo a "ZegoConfig.kt"
 * 2. Ve a https://console.zegocloud.com
 * 3. Crea un proyecto o selecciona uno existente
 * 4. Copia el App ID y App Sign
 * 5. Reemplaza los valores en ZegoConfig.kt
 * 6. NO subas ZegoConfig.kt a GitHub (está en .gitignore)
 * 
 * Para obtener tus credenciales:
 * 1. Ve a https://console.zegocloud.com
 * 2. Selecciona tu proyecto
 * 3. Ve a "Project Management" > "Project Information"
 * 4. Copia el App ID y App Sign
 */
object ZegoConfig {
    // App ID de ZegoCloud (público, puede estar en el cliente)
    const val APP_ID: Long = 0L // Reemplaza con tu App ID (número largo)
    
    // App Sign de ZegoCloud (debe estar en el backend en producción)
    const val APP_SIGN: String = "TU_APP_SIGN_AQUI" // Reemplaza con tu App Sign (string de 64 caracteres)
    
    // Configuración de escenarios
    const val SCENARIO_GENERAL = 0
    const val SCENARIO_COMMUNICATION = 1
    const val SCENARIO_LIVE = 2
    
    // Configuración de video
    const val VIDEO_RESOLUTION_WIDTH = 720
    const val VIDEO_RESOLUTION_HEIGHT = 1280
    const val VIDEO_BITRATE = 1500
    const val VIDEO_FPS = 30
    
    // Configuración de audio
    const val AUDIO_BITRATE = 48
}
