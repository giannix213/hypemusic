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
import kotlinx.coroutines.tasks.await

/**
 * Pantalla de visualización de Live Stream con Agora SDK.
 * El espectador ve la transmisión del streamer en tiempo real.
 */
@Composable
fun
        LiveStreamViewerScreen(
    sessionId: String,
    channelName: String,
    agoraToken: String,  // Token del emisor (NO se usará)
    streamerName: String = "Streamer",
    onExit: () -> Unit
) {
    val context = LocalContext.current
    val firebaseManager = remember { FirebaseManager() }
    
    var isConnected by remember { mutableStateOf(false) }
    var viewerCount by remember { mutableStateOf(0) }
    var hasPermissions by remember { mutableStateOf(false) }
    var agoraEngine by remember { mutableStateOf<RtcEngine?>(null) }
    var remoteSurfaceView by remember { mutableStateOf<SurfaceView?>(null) }
    var remoteUid by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    var viewerToken by remember { mutableStateOf<String?>(null) }
    var tokenError by remember { mutableStateOf<String?>(null) }
    
    // Generar token de espectador
    LaunchedEffect(channelName) {
        try {
            android.util.Log.d("LiveViewer", "🔑 Generando token de espectador...")
            android.util.Log.d("LiveViewer", "   Canal: $channelName")
            
            val functions = com.google.firebase.functions.FirebaseFunctions.getInstance()
            val data = hashMapOf(
                "channelName" to channelName,
                "role" to "subscriber",  // ← CRÍTICO: subscriber, no publisher
                "uid" to 0
            )
            
            val result = functions
                .getHttpsCallable("generateAgoraToken")
                .call(data)
                .await()
            
            val resultData = result.data as? Map<*, *>
            val token = resultData?.get("token") as? String
            
            if (token != null) {
                viewerToken = token
                android.util.Log.d("LiveViewer", "✅ Token de espectador recibido: ${token.take(20)}...")
            } else {
                tokenError = "No se recibió token"
                android.util.Log.e("LiveViewer", "❌ No se recibió token de espectador")
            }
        } catch (e: Exception) {
            tokenError = e.message
            android.util.Log.e("LiveViewer", "❌ Error generando token: ${e.message}", e)
        }
    }
    
    // Solo necesitamos permiso de audio para escuchar
    val permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
    
    // Launcher para solicitar permisos
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissionsMap ->
        hasPermissions = permissionsMap.values.all { it }
        if (hasPermissions) {
            android.util.Log.d("LiveViewer", "✅ Permisos concedidos")
        } else {
            android.util.Log.e("LiveViewer", "❌ Permisos denegados")
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
    
    // Inicializar Agora cuando tengamos permisos Y token de espectador
    LaunchedEffect(hasPermissions, viewerToken) {
        if (hasPermissions && viewerToken != null && agoraEngine == null) {
            try {
                android.util.Log.d("LiveViewer", "📺 Inicializando Agora SDK como espectador...")
                android.util.Log.d("LiveViewer", "   App ID: ${AgoraConfig.APP_ID}")
                android.util.Log.d("LiveViewer", "   Canal: $channelName")
                android.util.Log.d("LiveViewer", "   Token de espectador: ${viewerToken?.take(20)}...")
                
                // Configurar Agora
                val config = RtcEngineConfig().apply {
                    mContext = context
                    mAppId = AgoraConfig.APP_ID
                    mEventHandler = object : IRtcEngineEventHandler() {
                        override fun onJoinChannelSuccess(channel: String, uid: Int, elapsed: Int) {
                            android.util.Log.d("LiveViewer", "✅ Canal unido exitosamente")
                            android.util.Log.d("LiveViewer", "   Canal: $channel")
                            android.util.Log.d("LiveViewer", "   UID: $uid")
                            isConnected = true
                            isLoading = false
                        }
                        
                        override fun onUserJoined(uid: Int, elapsed: Int) {
                            android.util.Log.d("LiveViewer", "👤 Usuario unido: $uid")
                            remoteUid = uid
                            viewerCount++
                        }
                        
                        override fun onUserOffline(uid: Int, reason: Int) {
                            android.util.Log.d("LiveViewer", "👋 Usuario salió: $uid")
                            if (uid == remoteUid) {
                                remoteUid = 0
                            }
                            if (viewerCount > 0) viewerCount--
                        }
                        
                        override fun onRemoteVideoStateChanged(uid: Int, state: Int, reason: Int, elapsed: Int) {
                            android.util.Log.d("LiveViewer", "📹 Estado de video remoto cambió: uid=$uid, state=$state, reason=$reason")
                            if (state == 2) { // REMOTE_VIDEO_STATE_DECODING
                                android.util.Log.d("LiveViewer", "✅ Video remoto decodificando")
                                isLoading = false
                            }
                        }
                        
                        override fun onError(err: Int) {
                            android.util.Log.e("LiveViewer", "❌ Error de Agora: $err")
                            isLoading = false
                        }
                    }
                }
                
                // Crear engine
                val engine = RtcEngine.create(config)
                agoraEngine = engine
                
                // Configurar video
                engine.enableVideo()
                
                // Configurar como espectador (AUDIENCE)
                val options = ChannelMediaOptions().apply {
                    channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
                    clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
                    autoSubscribeAudio = true
                    autoSubscribeVideo = true
                }
                
                // Unirse al canal con el token de espectador
                val result = engine.joinChannel(viewerToken, channelName, 0, options)
                
                if (result == 0) {
                    android.util.Log.d("LiveViewer", "📡 Uniéndose al canal como espectador...")
                } else {
                    android.util.Log.e("LiveViewer", "❌ Error al unirse al canal: $result")
                    isLoading = false
                }
                
            } catch (e: Exception) {
                android.util.Log.e("LiveViewer", "❌ Error inicializando Agora: ${e.message}", e)
                isLoading = false
            }
        }
    }
    
    // Configurar vista remota cuando tengamos el UID del streamer
    LaunchedEffect(remoteUid, remoteSurfaceView) {
        if (remoteUid != 0 && remoteSurfaceView != null && agoraEngine != null) {
            android.util.Log.d("LiveViewer", "🎬 Configurando vista remota para UID: $remoteUid")
            agoraEngine?.setupRemoteVideo(
                VideoCanvas(
                    remoteSurfaceView,
                    VideoCanvas.RENDER_MODE_HIDDEN,
                    remoteUid
                )
            )
        }
    }
    
    // Limpiar al salir
    DisposableEffect(Unit) {
        onDispose {
            android.util.Log.d("LiveViewer", "🧹 Limpiando recursos de Agora...")
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
        // Vista del video remoto (streamer)
        if (hasPermissions && agoraEngine != null) {
            AndroidView(
                factory = { ctx ->
                    SurfaceView(ctx).also { surfaceView ->
                        remoteSurfaceView = surfaceView
                        android.util.Log.d("LiveViewer", "📺 SurfaceView creado para video remoto")
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
                    "🎧",
                    fontSize = 80.sp
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    "Permiso de audio necesario",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "Necesitamos acceso a tu micrófono para escuchar el audio",
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
                    Text("Conceder permiso")
                }
            }
        }
        
        // Indicador de carga
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = PopArtColors.Pink,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Conectando al Live...",
                        color = Color.White,
                        fontSize = 16.sp
                    )
                }
            }
        }
        
        // Overlay con información
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
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
                        android.util.Log.d("LiveViewer", "🚪 Saliendo del Live...")
                        agoraEngine?.leaveChannel()
                        onExit()
                    }
                ) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Salir del Live",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            // Nombre del streamer
            Surface(
                color = Color.Black.copy(alpha = 0.6f),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    streamerName,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}
