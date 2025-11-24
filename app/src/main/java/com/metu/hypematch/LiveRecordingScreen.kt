package com.metu.hypematch

import android.Manifest
import android.content.pm.PackageManager
import android.view.SurfaceView
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtc2.video.VideoEncoderConfiguration

/**
 * Pantalla de grabación/transmisión de Live con Agora SDK.
 * El streamer transmite en vivo y puede ver su propia cámara.
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
    var isStreaming by remember { mutableStateOf(false) }
    var viewerCount by remember { mutableStateOf(0) }
    var isMuted by remember { mutableStateOf(false) }
    var isFrontCamera by remember { mutableStateOf(true) }
    var hasPermissions by remember { mutableStateOf(false) }
    var agoraEngine by remember { mutableStateOf<RtcEngine?>(null) }
    var localSurfaceView by remember { mutableStateOf<SurfaceView?>(null) }
    
    // Permisos necesarios
    val permissions = arrayOf(
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO
    )
    
    // Launcher para solicitar permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        hasPermissions = permissionsMap.values.all { it }
        if (hasPermissions) {
            android.util.Log.d("LiveRecording", "✅ Permisos concedidos")
        } else {
            android.util.Log.e("LiveRecording", "❌ Permisos denegados")
        }
    }
    
    // Verificar permisos al iniciar
    LaunchedEffect(Unit) {
        val allPermissionsGranted = permissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
        
        if (allPermissionsGranted) {
            hasPermissions = true
        } else {
            permissionLauncher.launch(permissions)
        }
    }
    
    // Inicializar Agora cuando tengamos permisos
    LaunchedEffect(hasPermissions) {
        if (hasPermissions && agoraEngine == null) {
            try {
                android.util.Log.d("LiveRecording", "🎥 Inicializando Agora SDK...")
                android.util.Log.d("LiveRecording", "   App ID: ${AgoraConfig.APP_ID}")
                android.util.Log.d("LiveRecording", "   Canal: $channelName")
                android.util.Log.d("LiveRecording", "   Token: ${agoraToken.take(20)}...")
                
                // Configurar Agora
                val config = RtcEngineConfig().apply {
                    mContext = context
                    mAppId = AgoraConfig.APP_ID
                    mEventHandler = object : IRtcEngineEventHandler() {
                        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
                            android.util.Log.d("LiveRecording", "✅ Canal unido exitosamente")
                            android.util.Log.d("LiveRecording", "   Canal: $channel")
                            android.util.Log.d("LiveRecording", "   UID: $uid")
                            isStreaming = true
                            onStreamStarted()
                        }
                        
                        override fun onUserJoined(uid: Int, elapsed: Int) {
                            android.util.Log.d("LiveRecording", "👁️ Nuevo espectador: $uid")
                            viewerCount++
                        }
                        
                        override fun onUserOffline(uid: Int, reason: Int) {
                            android.util.Log.d("LiveRecording", "👋 Espectador salió: $uid")
                            if (viewerCount > 0) viewerCount--
                        }
                        
                        override fun onError(err: Int) {
                            android.util.Log.e("LiveRecording", "❌ Error de Agora: $err")
                        }
                    }
                }
                
                // Crear engine
                val engine = RtcEngine.create(config)
                agoraEngine = engine
                
                // Configurar video
                engine.enableVideo()
                engine.setVideoEncoderConfiguration(
                    VideoEncoderConfiguration(
                        VideoEncoderConfiguration.VD_640x360,
                        VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_30,
                        VideoEncoderConfiguration.STANDARD_BITRATE,
                        VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
                    )
                )
                
                // Configurar audio
                engine.setAudioProfile(
                    Constants.AUDIO_PROFILE_MUSIC_HIGH_QUALITY,
                    Constants.AUDIO_SCENARIO_GAME_STREAMING
                )
                
                // Configurar como broadcaster
                val options = ChannelMediaOptions().apply {
                    channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
                    clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
                    autoSubscribeAudio = true
                    autoSubscribeVideo = true
                    publishCameraTrack = true
                    publishMicrophoneTrack = true
                }
                
                // Unirse al canal
                val result = engine.joinChannel(agoraToken, channelName, 0, options)
                
                if (result == 0) {
                    android.util.Log.d("LiveRecording", "📡 Uniéndose al canal...")
                } else {
                    android.util.Log.e("LiveRecording", "❌ Error al unirse al canal: $result")
                }
                
            } catch (e: Exception) {
                android.util.Log.e("LiveRecording", "❌ Error inicializando Agora: ${e.message}", e)
            }
        }
    }
    
    // Limpiar al salir
    DisposableEffect(Unit) {
        onDispose {
            android.util.Log.d("LiveRecording", "🧹 Limpiando recursos de Agora...")
            agoraEngine?.leaveChannel()
            RtcEngine.destroy()
            agoraEngine = null
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Vista de la cámara local (preview del streamer)
        if (hasPermissions && agoraEngine != null) {
            AndroidView(
                factory = { ctx ->
                    SurfaceView(ctx).also { surfaceView ->
                        localSurfaceView = surfaceView
                        // Configurar vista local
                        agoraEngine?.setupLocalVideo(
                            VideoCanvas(
                                surfaceView,
                                VideoCanvas.RENDER_MODE_HIDDEN,
                                0
                            )
                        )
                        agoraEngine?.startPreview()
                        android.util.Log.d("LiveRecording", "📹 Preview de cámara iniciado")
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        } else if (!hasPermissions) {
            // Mensaje de permisos
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "📷",
                    fontSize = 80.sp
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    "Permisos necesarios",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Necesitamos acceso a tu cámara y micrófono para transmitir",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(Modifier.height(32.dp))
                Button(
                    onClick = { permissionLauncher.launch(permissions) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PopArtColors.Pink
                    )
                ) {
                    Text("Conceder permisos")
                }
            }
        }
        
        // Overlay con información
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header con botón de cerrar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Indicador de LIVE con icono
                Surface(
                    color = Color.Red,
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_live),
                            contentDescription = "Live",
                            modifier = Modifier.size(16.dp),
                            colorFilter = ColorFilter.tint(Color.White)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(
                            "LIVE",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
                
                // Contador de espectadores
                Surface(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "👁️",
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "$viewerCount",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
                
                // Botón de cerrar
                IconButton(
                    onClick = {
                        android.util.Log.d("LiveRecording", "🛑 Finalizando transmisión...")
                        agoraEngine?.leaveChannel()
                        onStreamEnded()
                    }
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Finalizar Live",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            Spacer(Modifier.weight(1f))
            
            // Controles de transmisión
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botón de cambiar cámara
                FloatingActionButton(
                    onClick = {
                        isFrontCamera = !isFrontCamera
                        agoraEngine?.switchCamera()
                        android.util.Log.d("LiveRecording", "📷 Cámara cambiada: ${if (isFrontCamera) "Frontal" else "Trasera"}")
                    },
                    containerColor = Color.Black.copy(alpha = 0.6f),
                    contentColor = Color.White
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_camera_switch),
                        contentDescription = "Cambiar cámara",
                        modifier = Modifier.size(32.dp),
                        colorFilter = ColorFilter.tint(Color.White)
                    )
                }
                
                // Botón de mutear/desmutear
                FloatingActionButton(
                    onClick = {
                        isMuted = !isMuted
                        agoraEngine?.muteLocalAudioStream(isMuted)
                        android.util.Log.d("LiveRecording", "🎤 Audio ${if (isMuted) "muteado" else "activado"}")
                    },
                    containerColor = if (isMuted) Color.Red else Color.Black.copy(alpha = 0.6f),
                    contentColor = Color.White
                ) {
                    Image(
                        painter = painterResource(
                            id = if (isMuted) R.drawable.ic_mic_off else R.drawable.ic_mic_on
                        ),
                        contentDescription = if (isMuted) "Micrófono muteado" else "Micrófono activo",
                        modifier = Modifier.size(32.dp),
                        colorFilter = ColorFilter.tint(Color.White)
                    )
                }
            }
        }
    }
}
