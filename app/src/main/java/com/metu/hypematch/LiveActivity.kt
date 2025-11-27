package com.metu.hypematch

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.zegocloud.uikit.prebuilt.livestreaming.ZegoUIKitPrebuiltLiveStreamingConfig
import com.zegocloud.uikit.prebuilt.livestreaming.ZegoUIKitPrebuiltLiveStreamingFragment

/**
 * Activity dedicada para Live Streaming con ZegoCloud
 * 
 * Esta Activity maneja el Fragment de ZegoCloud de forma nativa,
 * evitando problemas de integración con Compose.
 */
class LiveActivity : FragmentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        android.util.Log.d("LiveActivity", "========================================")
        android.util.Log.d("LiveActivity", "🎬 INICIANDO LIVE ACTIVITY")
        android.util.Log.d("LiveActivity", "========================================")
        
        // Verificar permisos antes de continuar
        val cameraPermission = checkSelfPermission(android.Manifest.permission.CAMERA)
        val audioPermission = checkSelfPermission(android.Manifest.permission.RECORD_AUDIO)
        
        android.util.Log.d("LiveActivity", "📹 Estado de permisos:")
        android.util.Log.d("LiveActivity", "   Cámara: ${if (cameraPermission == android.content.pm.PackageManager.PERMISSION_GRANTED) "✅ Otorgado" else "❌ Denegado"}")
        android.util.Log.d("LiveActivity", "   Audio: ${if (audioPermission == android.content.pm.PackageManager.PERMISSION_GRANTED) "✅ Otorgado" else "❌ Denegado"}")
        
        if (cameraPermission != android.content.pm.PackageManager.PERMISSION_GRANTED || 
            audioPermission != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            android.util.Log.e("LiveActivity", "❌ ERROR: Permisos no otorgados, cerrando Activity")
            android.widget.Toast.makeText(this, "Se necesitan permisos de cámara y audio", android.widget.Toast.LENGTH_LONG).show()
            finish()
            return
        }
        
        setContentView(R.layout.activity_live_container)
        
        // Obtener datos del Intent
        val userId = intent.getStringExtra("userId") ?: run {
            android.util.Log.e("LiveActivity", "❌ ERROR: userId no proporcionado")
            finish()
            return
        }
        
        val username = intent.getStringExtra("username") ?: run {
            android.util.Log.e("LiveActivity", "❌ ERROR: username no proporcionado")
            finish()
            return
        }
        
        val channelName = intent.getStringExtra("channelName") ?: run {
            android.util.Log.e("LiveActivity", "❌ ERROR: channelName no proporcionado")
            finish()
            return
        }
        
        val sessionId = intent.getStringExtra("sessionId") ?: ""
        
        android.util.Log.d("LiveActivity", "📋 Configuración:")
        android.util.Log.d("LiveActivity", "   APP_ID: ${ZegoConfig.APP_ID}")
        android.util.Log.d("LiveActivity", "   Usuario: $username")
        android.util.Log.d("LiveActivity", "   UserId: $userId")
        android.util.Log.d("LiveActivity", "   Canal: $channelName")
        android.util.Log.d("LiveActivity", "   SessionId: $sessionId")
        
        // Configurar como HOST
        val config = ZegoUIKitPrebuiltLiveStreamingConfig.host()
        config.turnOnCameraWhenJoining = true
        config.turnOnMicrophoneWhenJoining = true
        
        android.util.Log.d("LiveActivity", "✅ Configuración HOST creada")
        
        // Crear el Fragment de Zego
        android.util.Log.d("LiveActivity", "🔨 Creando Fragment de ZegoCloud...")
        android.util.Log.d("LiveActivity", "   Parámetros:")
        android.util.Log.d("LiveActivity", "   - APP_ID: ${ZegoConfig.APP_ID}")
        android.util.Log.d("LiveActivity", "   - APP_SIGN: ${ZegoConfig.APP_SIGN.take(20)}...")
        android.util.Log.d("LiveActivity", "   - userId: $userId")
        android.util.Log.d("LiveActivity", "   - username: $username")
        android.util.Log.d("LiveActivity", "   - channelName: $channelName")
        
        val fragment = try {
            val frag = ZegoUIKitPrebuiltLiveStreamingFragment.newInstance(
                ZegoConfig.APP_ID,
                ZegoConfig.APP_SIGN,
                userId,
                username,
                channelName,
                config
            )
            android.util.Log.d("LiveActivity", "✅ Fragment creado exitosamente")
            frag
        } catch (e: Exception) {
            android.util.Log.e("LiveActivity", "❌ ERROR creando Fragment:")
            android.util.Log.e("LiveActivity", "   Mensaje: ${e.message}")
            android.util.Log.e("LiveActivity", "   Tipo: ${e.javaClass.simpleName}")
            android.util.Log.e("LiveActivity", "   Stack trace:", e)
            android.widget.Toast.makeText(this, "Error al iniciar live: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            finish()
            return
        }
        
        // Agregar el Fragment al container
        android.util.Log.d("LiveActivity", "📦 Agregando Fragment al container...")
        try {
            val transaction = supportFragmentManager.beginTransaction()
            transaction.replace(R.id.liveFragmentContainer, fragment)
            transaction.commitNow()
            
            android.util.Log.d("LiveActivity", "✅ Fragment agregado exitosamente")
            android.util.Log.d("LiveActivity", "========================================")
            android.util.Log.d("LiveActivity", "✅ LIVE ACTIVITY LISTA - ESPERANDO CÁMARA")
            android.util.Log.d("LiveActivity", "========================================")
        } catch (e: Exception) {
            android.util.Log.e("LiveActivity", "❌ ERROR agregando Fragment:")
            android.util.Log.e("LiveActivity", "   Mensaje: ${e.message}")
            android.util.Log.e("LiveActivity", "   Stack trace:", e)
            android.widget.Toast.makeText(this, "Error al mostrar cámara: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
            finish()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        android.util.Log.d("LiveActivity", "🛑 Live Activity destruida")
    }
}
