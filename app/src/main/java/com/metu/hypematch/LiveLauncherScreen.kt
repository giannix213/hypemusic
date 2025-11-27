package com.metu.hypematch

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Pantalla de lanzamiento de Live que maneja el flujo completo:
 * 1. Carga inicial (obtener token de Agora)
 * 2. Manejo de errores
 * 3. Inicio de la transmisión
 */
@Composable
fun LiveLauncherScreen(
    onClose: () -> Unit, // Función para cerrar toda la experiencia de Live
    onStartBroadcast: (sessionId: String, channelName: String, token: String) -> Unit = { _, _, _ -> } // Callback cuando se obtiene el token
) {
    val context = LocalContext.current
    val firebaseManager = remember { FirebaseManager() }
    val authManager = remember { AuthManager(context) }
    
    // Obtener datos del usuario actual
    val currentUserId = authManager.getUserId() ?: ""
    val currentUsername = authManager.getUserName()
    
    // Estados locales
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    // Obtener foto de perfil del usuario
    var profileImageUrl by remember { mutableStateOf("") }
    LaunchedEffect(currentUserId) {
        if (currentUserId.isNotEmpty()) {
            try {
                val profile = firebaseManager.getUserProfile(currentUserId)
                profileImageUrl = profile?.profileImageUrl ?: ""
            } catch (e: Exception) {
                android.util.Log.w("LiveLauncher", "No se pudo obtener foto de perfil: ${e.message}")
            }
        }
    }
    
    // 🔑 PASO CLAVE: Lanzador de Permisos
    val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val cameraGranted = permissions[android.Manifest.permission.CAMERA] == true
        val audioGranted = permissions[android.Manifest.permission.RECORD_AUDIO] == true
        
        android.util.Log.d("LiveLauncher", "📹 Resultado de permisos:")
        android.util.Log.d("LiveLauncher", "   Cámara: ${if (cameraGranted) "✅ Otorgado" else "❌ Denegado"}")
        android.util.Log.d("LiveLauncher", "   Audio: ${if (audioGranted) "✅ Otorgado" else "❌ Denegado"}")
        
        if (cameraGranted && audioGranted) {
            // Permisos otorgados, continuar con el setup del Live
            android.util.Log.d("LiveLauncher", "✅ Permisos otorgados, continuando con setup...")
            startLiveSetupInternal()
        } else {
            // Permisos denegados
            android.util.Log.e("LiveLauncher", "❌ Permisos denegados por el usuario")
            errorMessage = "Se necesitan permisos de cámara y micrófono para iniciar un live"
            isLoading = false
        }
    }
    
    // Función interna que realmente inicia el Live (después de tener permisos)
    fun startLiveSetupInternal() {
        android.util.Log.d("LiveLauncher", "🚀 ===== INICIANDO SETUP DE LIVE CON ZEGOCLOUD =====")
        android.util.Log.d("LiveLauncher", "👤 Usuario: $currentUsername ($currentUserId)")
        android.util.Log.d("LiveLauncher", "📸 Foto perfil: $profileImageUrl")
        
        // Validar que tenemos los datos necesarios
        if (currentUserId.isEmpty()) {
            android.util.Log.e("LiveLauncher", "❌ currentUserId está vacío!")
            errorMessage = "Error: Usuario no identificado"
            return
        }
        
        if (currentUsername.isEmpty()) {
            android.util.Log.e("LiveLauncher", "❌ currentUsername está vacío!")
            errorMessage = "Error: Nombre de usuario no encontrado"
            return
        }
        
        isLoading = true
        errorMessage = null
        
        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
            try {
                // Crear sesión en Firebase
                android.util.Log.d("LiveLauncher", "📞 Creando sesión en Firebase...")
                android.util.Log.d("LiveLauncher", "   userId: $currentUserId")
                android.util.Log.d("LiveLauncher", "   username: $currentUsername")
                android.util.Log.d("LiveLauncher", "   profileImageUrl: $profileImageUrl")
                
                // 🔧 SOLUCIÓN: ZegoCloud NO necesita token de backend
                // Solo necesitamos crear un sessionId y channelName únicos
                val sessionId = firebaseManager.generateSessionId()
                val channelName = "live_${currentUserId}_${System.currentTimeMillis()}"
                
                android.util.Log.d("LiveLauncher", "✅ Sesión creada (sin token de backend)")
                android.util.Log.d("LiveLauncher", "   SessionId: $sessionId")
                android.util.Log.d("LiveLauncher", "   Canal: $channelName")
                android.util.Log.d("LiveLauncher", "   ZegoCloud usa APP_ID y APP_SIGN directamente")
                
                // Crear sesión en Firebase (ZegoCloud - sin token)
                firebaseManager.createLiveSessionZego(
                    sessionId = sessionId,
                    userId = currentUserId,
                    username = currentUsername,
                    channelName = channelName,
                    title = "Live de $currentUsername"
                )
                
                isLoading = false
                
                // Lanzar LiveActivity directamente
                android.util.Log.d("LiveLauncher", "🚀 Lanzando LiveActivity...")
                val intent = android.content.Intent(context, LiveActivity::class.java)
                intent.putExtra("userId", currentUserId)
                intent.putExtra("username", currentUsername)
                intent.putExtra("channelName", channelName)
                intent.putExtra("sessionId", sessionId)
                context.startActivity(intent)
                
                android.widget.Toast.makeText(context, "✅ Iniciando transmisión...", android.widget.Toast.LENGTH_SHORT).show()
                
                // Cerrar LiveLauncher
                onClose()
            } catch (e: Exception) {
                android.util.Log.e("LiveLauncher", "❌ Error en startLiveSetup: ${e.message}", e)
                android.util.Log.e("LiveLauncher", "   Stack trace:", e)
                errorMessage = "Error: ${e.message ?: "Desconocido"}"
                isLoading = false
            }
        }
    }
    
    // Función pública que verifica permisos antes de iniciar
    fun startLiveSetup() {
        android.util.Log.d("LiveLauncher", "🔐 Verificando permisos...")
        
        val cameraPermission = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CAMERA
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        
        val audioPermission = androidx.core.content.ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.RECORD_AUDIO
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        
        if (cameraPermission && audioPermission) {
            // Ya tenemos permisos, continuar directamente
            android.util.Log.d("LiveLauncher", "✅ Permisos ya otorgados")
            startLiveSetupInternal()
        } else {
            // Solicitar permisos
            android.util.Log.d("LiveLauncher", "📱 Solicitando permisos al usuario...")
            permissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.CAMERA,
                    android.Manifest.permission.RECORD_AUDIO
                )
            )
        }
    }
    
    // Mostrar Toast cuando hay error
    LaunchedEffect(errorMessage) {
        errorMessage?.let { msg ->
            android.widget.Toast.makeText(context, "❌ Error: $msg", android.widget.Toast.LENGTH_LONG).show()
        }
    }
    
    // Ya no necesitamos mostrar LiveRecordingScreen aquí
    // LiveActivity se lanza directamente desde startLiveSetupInternal()
    
    // UI según el estado
    when {
        isLoading -> {
            // Cargando
            LoadingScreen()
        }
        
        errorMessage != null -> {
            // Error
            ErrorScreen(
                message = errorMessage!!,
                onRetry = { 
                    android.widget.Toast.makeText(context, "🔄 Reintentando...", android.widget.Toast.LENGTH_SHORT).show()
                    startLiveSetup()
                },
                onClose = onClose
            )
        }
        
        else -> {
            // Estado inicial
            IdleScreen(
                onStart = { startLiveSetup() },
                onClose = onClose
            )
        }
    }
}

@Composable
private fun IdleScreen(
    onStart: () -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Botón de cerrar en la esquina superior derecha
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Cerrar",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
        
        // Contenido central
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icono grande de Live
            Image(
                painter = painterResource(id = R.drawable.ic_live),
                contentDescription = "Live",
                modifier = Modifier.size(120.dp)
            )
            
            Spacer(Modifier.height(32.dp))
            
            // Título
            Text(
                "Iniciar transmisión en vivo",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(Modifier.height(16.dp))
            
            // Descripción
            Text(
                "Comparte tu talento con el mundo.\nTus seguidores recibirán una notificación.",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                lineHeight = 24.sp
            )
            
            Spacer(Modifier.height(48.dp))
            
            // Botón principal de Iniciar Live
            Button(
                onClick = {
                    android.util.Log.d("LiveLauncher", "🚀 Usuario presionó Iniciar Live")
                    android.widget.Toast.makeText(context, "🎬 Preparando transmisión...", android.widget.Toast.LENGTH_SHORT).show()
                    onStart()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = PopArtColors.Pink
                ),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(64.dp),
                shape = RoundedCornerShape(32.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        "🔴",
                        fontSize = 24.sp
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        "Iniciar Live",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            // Información adicional
            Surface(
                color = Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    InfoRow("✅", "Transmisión en tiempo real")
                    Spacer(Modifier.height(12.dp))
                    InfoRow("👥", "Interactúa con tus seguidores")
                    Spacer(Modifier.height(12.dp))
                    InfoRow("💬", "Chat en vivo")
                }
            }
        }
    }
}

@Composable
private fun InfoRow(emoji: String, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            emoji,
            fontSize = 20.sp
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text,
            fontSize = 14.sp,
            color = Color.White.copy(alpha = 0.9f)
        )
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = PopArtColors.Yellow,
                modifier = Modifier.size(64.dp),
                strokeWidth = 6.dp
            )
            Spacer(Modifier.height(32.dp))
            Text(
                "Preparando Live...",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Conectando con Agora",
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun ErrorScreen(
    message: String,
    onRetry: () -> Unit,
    onClose: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        // Botón de cerrar en la esquina superior derecha
        IconButton(
            onClick = onClose,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                Icons.Default.Close,
                contentDescription = "Cerrar",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
        
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "❌",
                fontSize = 80.sp
            )
            Spacer(Modifier.height(24.dp))
            Text(
                "Error al iniciar Live",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Surface(
                color = Color.Red.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    message,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(16.dp)
                )
            }
            Spacer(Modifier.height(32.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = PopArtColors.Yellow
                ),
                modifier = Modifier
                    .width(200.dp)
                    .height(56.dp)
            ) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Reintentar",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = onClose) {
                Text(
                    "Cancelar",
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
    }
}
}
