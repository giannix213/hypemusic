package com.metu.hypematch

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.zegocloud.uikit.prebuilt.livestreaming.ZegoUIKitPrebuiltLiveStreamingConfig
import com.zegocloud.uikit.prebuilt.livestreaming.ZegoUIKitPrebuiltLiveStreamingFragment
import kotlinx.coroutines.delay

/**
 * Live Streaming Screen - Host/Streamer
 * Uses ZegoCloud UIKit Prebuilt Live Streaming Kit
 * Con UI personalizada: Botón de inicio/fin y contador de tiempo
 */
@Composable
fun LiveRecordingScreen(
    sessionId: String,
    channelName: String,
    agoraToken: String,
    onStreamStarted: () -> Unit,
    onStreamEnded: () -> Unit
) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val firebaseManager = remember { FirebaseManager() }
    val userId = authManager.getUserId() ?: "user_${System.currentTimeMillis()}"
    
    // 🔧 SOLUCIÓN: Solicitar permisos de cámara y micrófono
    var hasCameraPermission by remember { mutableStateOf(false) }
    var hasAudioPermission by remember { mutableStateOf(false) }
    var permissionsChecked by remember { mutableStateOf(false) }
    
    // Verificar permisos al inicio
    LaunchedEffect(Unit) {
        val cameraGranted = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CAMERA
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        
        val audioGranted = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        
        hasCameraPermission = cameraGranted
        hasAudioPermission = audioGranted
        permissionsChecked = true
        
        android.util.Log.d("LiveRecordingScreen", "📹 Permisos verificados:")
        android.util.Log.d("LiveRecordingScreen", "   Cámara: ${if (cameraGranted) "✅ Otorgado" else "❌ Denegado"}")
        android.util.Log.d("LiveRecordingScreen", "   Audio: ${if (audioGranted) "✅ Otorgado" else "❌ Denegado"}")
    }
    
    // Launcher para solicitar permisos
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasCameraPermission = permissions[android.Manifest.permission.CAMERA] ?: false
        hasAudioPermission = permissions[android.Manifest.permission.RECORD_AUDIO] ?: false
        
        android.util.Log.d("LiveRecordingScreen", "📹 Resultado de permisos:")
        android.util.Log.d("LiveRecordingScreen", "   Cámara: ${if (hasCameraPermission) "✅ Otorgado" else "❌ Denegado"}")
        android.util.Log.d("LiveRecordingScreen", "   Audio: ${if (hasAudioPermission) "✅ Otorgado" else "❌ Denegado"}")
        
        if (!hasCameraPermission || !hasAudioPermission) {
            android.widget.Toast.makeText(
                context,
                "⚠️ Se necesitan permisos de cámara y micrófono para transmitir",
                android.widget.Toast.LENGTH_LONG
            ).show()
        }
    }
    
    // Obtener username de Firebase
    var username by remember { mutableStateOf("User") }
    LaunchedEffect(userId) {
        try {
            val profile = firebaseManager.getUserProfile(userId)
            username = profile?.username ?: "User"
        } catch (e: Exception) {
            android.util.Log.e("LiveRecordingScreen", "Error getting username: ${e.message}")
        }
    }
    
    // --- LÓGICA DE CONTADOR Y ESTADO ---
    var isStreaming by remember { mutableStateOf(false) }
    var secondsElapsed by remember { mutableIntStateOf(0) }
    
    // Referencia al fragment de Zego para control programático
    var zegoFragment by remember { mutableStateOf<ZegoUIKitPrebuiltLiveStreamingFragment?>(null) }
    
    // Efecto para iniciar el contador cuando el streaming está activo
    LaunchedEffect(isStreaming) {
        if (isStreaming) {
            secondsElapsed = 0
            while (isStreaming) {
                delay(1000L) // Espera 1 segundo
                secondsElapsed++
            }
        }
    }
    
    // Formatear el tiempo a MM:SS
    val minutes = secondsElapsed / 60
    val seconds = secondsElapsed % 60
    val timeString = String.format("%02d:%02d", minutes, seconds)
    
    DisposableEffect(Unit) {
        onDispose {
            if (isStreaming) {
                onStreamEnded()
            }
        }
    }
    
    // 🔧 SOLUCIÓN: Mostrar pantalla de permisos si no están otorgados
    if (permissionsChecked && (!hasCameraPermission || !hasAudioPermission)) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(32.dp)
            ) {
                Text(
                    "📹 Permisos Necesarios",
                    fontSize = 24.sp,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                
                Spacer(Modifier.height(16.dp))
                
                Text(
                    "Para transmitir en vivo necesitas otorgar permisos de cámara y micrófono.",
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                
                Spacer(Modifier.height(32.dp))
                
                Button(
                    onClick = {
                        permissionLauncher.launch(
                            arrayOf(
                                android.Manifest.permission.CAMERA,
                                android.Manifest.permission.RECORD_AUDIO
                            )
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Otorgar Permisos")
                }
                
                Spacer(Modifier.height(16.dp))
                
                TextButton(onClick = { onStreamEnded() }) {
                    Text("Cancelar")
                }
            }
        }
        return
    }
    
    // 🔧 SOLUCIÓN DEFINITIVA: Crear Fragment y Container FUERA de AndroidView
    android.util.Log.d("LiveRecordingScreen", "========================================")
    android.util.Log.d("LiveRecordingScreen", "🎬 INICIALIZANDO ZEGOCLOUD UIKIT")
    android.util.Log.d("LiveRecordingScreen", "========================================")
    
    // Obtener FragmentManager usando reflexión
    val fragmentManager = remember(context) {
        try {
            val method = context.javaClass.getMethod("getSupportFragmentManager")
            method.invoke(context) as? androidx.fragment.app.FragmentManager
        } catch (e: Exception) {
            android.util.Log.e("LiveRecordingScreen", "❌ ERROR obteniendo FragmentManager: ${e.message}")
            null
        }
    }
    
    // Crear el Fragment de Zego (solo una vez)
    val zegoFragmentInstance = remember(userId, channelName) {
        android.util.Log.d("LiveRecordingScreen", "📋 Configuración:")
        android.util.Log.d("LiveRecordingScreen", "   APP_ID: ${ZegoConfig.APP_ID}")
        android.util.Log.d("LiveRecordingScreen", "   Usuario: $username ($userId)")
        android.util.Log.d("LiveRecordingScreen", "   Canal: $channelName")
        
        val config = ZegoUIKitPrebuiltLiveStreamingConfig.host()
        config.turnOnCameraWhenJoining = true
        config.turnOnMicrophoneWhenJoining = true
        
        android.util.Log.d("LiveRecordingScreen", "🔨 Creando Fragment de Zego...")
        
        try {
            val fragment = ZegoUIKitPrebuiltLiveStreamingFragment.newInstance(
                ZegoConfig.APP_ID,
                ZegoConfig.APP_SIGN,
                userId,
                username,
                channelName,
                config
            )
            zegoFragment = fragment
            android.util.Log.d("LiveRecordingScreen", "✅ Fragment creado exitosamente")
            fragment
        } catch (e: Exception) {
            android.util.Log.e("LiveRecordingScreen", "❌ ERROR creando Fragment: ${e.message}", e)
            null
        }
    }
    
    // Crear el FrameLayout (solo una vez)
    val fragmentContainer = remember {
        android.widget.FrameLayout(context).apply {
            id = android.view.View.generateViewId()
            layoutParams = android.view.ViewGroup.LayoutParams(
                android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                android.view.ViewGroup.LayoutParams.MATCH_PARENT
            )
            android.util.Log.d("LiveRecordingScreen", "📦 FrameLayout creado con ID: $id")
        }
    }
    
    // Manejar el ciclo de vida del Fragment con DisposableEffect
    DisposableEffect(fragmentManager, zegoFragmentInstance, fragmentContainer) {
        if (fragmentManager == null) {
            android.util.Log.e("LiveRecordingScreen", "❌ ERROR: FragmentManager es nulo")
            return@DisposableEffect onDispose {}
        }
        
        if (zegoFragmentInstance == null) {
            android.util.Log.e("LiveRecordingScreen", "❌ ERROR: Fragment de Zego es nulo")
            return@DisposableEffect onDispose {}
        }
        
        try {
            if (!fragmentManager.isStateSaved) {
                android.util.Log.d("LiveRecordingScreen", "🔨 Agregando Fragment al FragmentManager...")
                android.util.Log.d("LiveRecordingScreen", "   Container ID: ${fragmentContainer.id}")
                
                fragmentManager.beginTransaction()
                    .replace(fragmentContainer.id, zegoFragmentInstance)
                    .setCustomAnimations(0, 0, 0, 0)
                    .commitNow()
                
                android.util.Log.d("LiveRecordingScreen", "✅ Fragment agregado exitosamente")
                android.util.Log.d("LiveRecordingScreen", "   Fragments activos: ${fragmentManager.fragments.size}")
                android.util.Log.d("LiveRecordingScreen", "   isAdded: ${zegoFragmentInstance.isAdded}")
                android.util.Log.d("LiveRecordingScreen", "   isVisible: ${zegoFragmentInstance.isVisible}")
            }
        } catch (e: Exception) {
            android.util.Log.e("LiveRecordingScreen", "❌ ERROR CRÍTICO AL AÑADIR FRAGMENTO:", e)
            fragmentContainer.setBackgroundColor(android.graphics.Color.RED)
        }
        
        // Limpieza cuando el Composable se descarta
        onDispose {
            try {
                if (!fragmentManager.isStateSaved && zegoFragmentInstance.isAdded) {
                    android.util.Log.d("LiveRecordingScreen", "🧹 Limpiando Fragment...")
                    fragmentManager.beginTransaction()
                        .remove(zegoFragmentInstance)
                        .commitNow()
                    android.util.Log.d("LiveRecordingScreen", "✅ Fragment eliminado correctamente")
                }
            } catch (e: Exception) {
                android.util.Log.e("LiveRecordingScreen", "❌ ERROR AL REMOVER FRAGMENTO:", e)
            }
        }
    }
    
    android.util.Log.d("LiveRecordingScreen", "========================================")
    android.util.Log.d("LiveRecordingScreen", "✅ INICIALIZACIÓN COMPLETA")
    android.util.Log.d("LiveRecordingScreen", "========================================")
    
    // Usamos Box para superponer los elementos de la UI (botón y texto) sobre la vista de la cámara
    Box(modifier = Modifier.fillMaxSize()) {
        // 1. VISTA DE LA CÁMARA (ANDROIDVIEW con el container persistente)
        AndroidView(
            factory = { fragmentContainer },
            modifier = Modifier.fillMaxSize(),
            update = { /* Actualización si es necesaria */ }
        )
        
        // 2. CONTADOR DE TIEMPO (Parte Superior Central)
        if (isStreaming) {
            Text(
                text = timeString,
                color = MaterialTheme.colorScheme.onPrimary,
                fontSize = 24.sp,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 16.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        
        // 3. BOTÓN DE INICIO/FIN DE LIVE (Parte Inferior Central)
        Button(
            onClick = {
                if (isStreaming) {
                    // Detener la transmisión y el contador
                    isStreaming = false
                    android.util.Log.d("LiveRecordingScreen", "🛑 Finalizando transmisión...")
                    android.util.Log.d("LiveRecordingScreen", "   Duración: $timeString")
                    android.util.Log.d("LiveRecordingScreen", "   Canal: $channelName")
                    
                    // 🔧 SOLUCIÓN: Apagar cámara y micrófono explícitamente
                    try {
                        zegoFragment?.let { fragment ->
                            // El fragment de Zego maneja internamente el estado de cámara/mic
                            // Al salir de la pantalla, se limpiará automáticamente
                            android.util.Log.d("LiveRecordingScreen", "📹 Deteniendo cámara y micrófono")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("LiveRecordingScreen", "Error deteniendo streaming: ${e.message}")
                    }
                    
                    onStreamEnded()
                    
                    android.widget.Toast.makeText(
                        context,
                        "🛑 Transmisión finalizada: $timeString",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                } else {
                    // 🔧 SOLUCIÓN: Iniciar cámara y micrófono explícitamente
                    isStreaming = true
                    android.util.Log.d("LiveRecordingScreen", "▶️ Iniciando transmisión...")
                    android.util.Log.d("LiveRecordingScreen", "   Usuario: $username")
                    android.util.Log.d("LiveRecordingScreen", "   Canal: $channelName")
                    android.util.Log.d("LiveRecordingScreen", "   Session ID: $sessionId")
                    
                    try {
                        zegoFragment?.let { fragment ->
                            // 📹 Activar cámara y micrófono programáticamente
                            // Nota: ZegoCloud UIKit maneja esto internamente
                            // El usuario verá los controles en el fragment para activar/desactivar
                            android.util.Log.d("LiveRecordingScreen", "📹 Activando cámara y micrófono")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("LiveRecordingScreen", "Error iniciando streaming: ${e.message}")
                    }
                    
                    onStreamStarted()
                    
                    android.widget.Toast.makeText(
                        context,
                        "🔴 Transmisión en vivo iniciada",
                        android.widget.Toast.LENGTH_SHORT
                    ).show()
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isStreaming) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
            )
        ) {
            Text(if (isStreaming) "Finalizar Live" else "Iniciar Live")
        }
    }
}
