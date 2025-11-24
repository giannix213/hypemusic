package com.metu.hypematch

import android.content.Context
import android.net.Uri
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@Composable
fun CameraRecordingScreen(
    onBack: () -> Unit,
    onVideoRecorded: (Uri) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    var isRecording by remember { mutableStateOf(false) }
    var recordingTime by remember { mutableStateOf(0) }
    var videoCapture by remember { mutableStateOf<VideoCapture<Recorder>?>(null) }
    var recording by remember { mutableStateOf<Recording?>(null) }
    var isCameraReady by remember { mutableStateOf(false) }
    
    // Executor para operaciones de cámara
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }
    
    // Mantener la pantalla encendida durante la grabación
    DisposableEffect(isRecording) {
        val window = (context as? android.app.Activity)?.window
        if (isRecording) {
            window?.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            android.util.Log.d("CameraScreen", "🔆 Pantalla mantenida encendida")
        } else {
            window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            android.util.Log.d("CameraScreen", "🌙 Pantalla puede apagarse")
        }
        
        onDispose {
            window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }
    
    // Timer para grabación
    LaunchedEffect(isRecording) {
        if (isRecording) {
            while (isRecording && recordingTime < 60) {
                kotlinx.coroutines.delay(1000)
                recordingTime++
            }
            if (recordingTime >= 60) {
                // Detener automáticamente después de 60 segundos
                stopRecording(recording) { uri ->
                    isRecording = false
                    recordingTime = 0
                    recording = null
                    if (uri != null) {
                        onVideoRecorded(uri)
                    }
                }
            }
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(PopArtColors.Black)
    ) {
        // Vista de la cámara
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    android.util.Log.d("CameraScreen", "Creando PreviewView")
                    
                    // Configurar cámara inmediatamente
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        try {
                            val cameraProvider = cameraProviderFuture.get()
                            android.util.Log.d("CameraScreen", "CameraProvider obtenido")
                            
                            // Desvincular casos de uso anteriores
                            cameraProvider.unbindAll()
                            
                            // Configurar Preview
                            val preview = Preview.Builder().build()
                            preview.setSurfaceProvider(this.surfaceProvider)
                            
                            // Configurar VideoCapture
                            val recorder = Recorder.Builder()
                                .setQualitySelector(QualitySelector.from(Quality.HD))
                                .build()
                            val videoCaptureUseCase = VideoCapture.withOutput(recorder)
                            
                            // Seleccionar cámara
                            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                            
                            // Vincular al lifecycle
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview,
                                videoCaptureUseCase
                            )
                            
                            videoCapture = videoCaptureUseCase
                            isCameraReady = true
                            android.util.Log.d("CameraScreen", "Cámara configurada exitosamente")
                            
                        } catch (e: Exception) {
                            android.util.Log.e("CameraScreen", "Error configurando cámara: ${e.message}", e)
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        // Indicador de estado de cámara
        if (!isCameraReady) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = PopArtColors.Yellow,
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "Iniciando cámara...",
                        color = PopArtColors.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        // Header con botón de volver (siempre visible)
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
                .size(48.dp)
                .background(PopArtColors.Black.copy(alpha = 0.7f), CircleShape)
        ) {
            Icon(
                Icons.Default.ArrowBack,
                contentDescription = "Volver",
                tint = Color.White
            )
        }
        
        // Indicador de grabación (solo cuando está grabando)
        if (isRecording) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(
                        PopArtColors.Pink.copy(alpha = 0.9f),
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(Color.White, CircleShape)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "REC ${recordingTime}s",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
            }
        }
        
        // Controles de grabación (solo cuando la cámara esté lista)
        if (isCameraReady) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color.Transparent,
                                PopArtColors.Black.copy(alpha = 0.8f)
                            )
                        )
                    )
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Botón principal de grabación
                Button(
                    onClick = {
                        if (isRecording) {
                            // Detener grabación
                            stopRecording(recording) { uri ->
                                isRecording = false
                                recordingTime = 0
                                recording = null
                                if (uri != null) {
                                    onVideoRecorded(uri)
                                }
                            }
                        } else {
                            // Iniciar grabación
                            videoCapture?.let { capture ->
                                startRecording(
                                    context = context,
                                    videoCapture = capture,
                                    executor = cameraExecutor
                                ) { newRecording ->
                                    recording = newRecording
                                    isRecording = true
                                    recordingTime = 0
                                    android.util.Log.d("CameraScreen", "Grabación iniciada exitosamente")
                                }
                            }
                        }
                    },
                    modifier = Modifier.size(80.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRecording) PopArtColors.Pink else PopArtColors.Yellow
                    ),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    enabled = videoCapture != null
                ) {
                    Icon(
                        if (isRecording) Icons.Default.Close else Icons.Default.PlayArrow,
                        contentDescription = if (isRecording) "Detener" else "Grabar",
                        tint = if (isRecording) Color.White else PopArtColors.Black,
                        modifier = Modifier.size(32.dp)
                    )
                }
                
                Spacer(Modifier.height(16.dp))
                
                Text(
                    if (isRecording) "Toca para detener" else if (videoCapture != null) "Toca para grabar" else "Preparando...",
                    fontSize = 14.sp,
                    color = PopArtColors.White,
                    fontWeight = FontWeight.Bold
                )
                
                if (!isRecording && videoCapture != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Máximo 60 segundos",
                        fontSize = 12.sp,
                        color = PopArtColors.White.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}



// Variable global para manejar el callback del video
private var globalVideoSavedCallback: ((Uri?) -> Unit)? = null

private fun startRecording(
    context: Context,
    videoCapture: VideoCapture<Recorder>,
    executor: ExecutorService,
    onRecordingStarted: (Recording) -> Unit
) {
    // Crear archivo para el video
    val name = "HypeMatch_${SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis())}"
    val contentValues = android.content.ContentValues().apply {
        put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, name)
        put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
        put(android.provider.MediaStore.Video.Media.RELATIVE_PATH, "Movies/HypeMatch")
    }
    
    val mediaStoreOutputOptions = MediaStoreOutputOptions
        .Builder(context.contentResolver, android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
        .setContentValues(contentValues)
        .build()
    
    // Configurar grabación
    val recording = videoCapture.output
        .prepareRecording(context, mediaStoreOutputOptions)
        .apply {
            // Habilitar audio si el permiso está concedido
            if (androidx.core.content.ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.RECORD_AUDIO
                ) == android.content.pm.PackageManager.PERMISSION_GRANTED
            ) {
                withAudioEnabled()
            }
        }
        .start(executor) { recordEvent ->
            when (recordEvent) {
                is VideoRecordEvent.Start -> {
                    android.util.Log.d("CameraScreen", "Grabación iniciada correctamente")
                }
                is VideoRecordEvent.Finalize -> {
                    if (!recordEvent.hasError()) {
                        val videoUri = recordEvent.outputResults.outputUri
                        android.util.Log.d("CameraScreen", "Video guardado exitosamente en: $videoUri")
                        globalVideoSavedCallback?.invoke(videoUri)
                    } else {
                        android.util.Log.e("CameraScreen", "Error en grabación: ${recordEvent.error}")
                        globalVideoSavedCallback?.invoke(null)
                    }
                    globalVideoSavedCallback = null
                }
            }
        }
    
    onRecordingStarted(recording)
}

private fun stopRecording(
    recording: Recording?,
    onVideoSaved: (Uri?) -> Unit
) {
    if (recording != null) {
        android.util.Log.d("CameraScreen", "Deteniendo grabación...")
        
        // Configurar el callback para cuando el video esté guardado
        globalVideoSavedCallback = onVideoSaved
        
        // Detener la grabación
        recording.stop()
        android.util.Log.d("CameraScreen", "Comando de detener enviado, esperando finalización...")
    } else {
        android.util.Log.w("CameraScreen", "No hay grabación activa para detener")
        onVideoSaved(null)
    }
}