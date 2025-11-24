# 🎬 Ejemplo de Integración: Live Streaming

## 📱 Cómo Integrar los Componentes de Live

### 1️⃣ Agregar Rutas de Navegación

```kotlin
// En tu NavHost principal
NavHost(navController = navController, startDestination = "home") {
    
    // ... otras rutas ...
    
    // Ruta para iniciar Live (Streamer)
    composable(
        route = "live_recording/{sessionId}/{channelName}/{token}",
        arguments = listOf(
            navArgument("sessionId") { type = NavType.StringType },
            navArgument("channelName") { type = NavType.StringType },
            navArgument("token") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
        val channelName = backStackEntry.arguments?.getString("channelName") ?: ""
        val token = backStackEntry.arguments?.getString("token") ?: ""
        
        LiveRecordingScreen(
            sessionId = sessionId,
            channelName = channelName,
            agoraToken = token,
            onStreamStarted = {
                // Actualizar Firestore: Live está activo
                FirebaseManager.updateLiveStatus(sessionId, isActive = true)
            },
            onStreamEnded = {
                // Actualizar Firestore: Live terminó
                FirebaseManager.updateLiveStatus(sessionId, isActive = false)
                navController.popBackStack()
            }
        )
    }
    
    // Ruta para ver Live (Espectador)
    composable(
        route = "live_viewer/{sessionId}/{channelName}/{token}/{streamerName}",
        arguments = listOf(
            navArgument("sessionId") { type = NavType.StringType },
            navArgument("channelName") { type = NavType.StringType },
            navArgument("token") { type = NavType.StringType },
            navArgument("streamerName") { type = NavType.StringType }
        )
    ) { backStackEntry ->
        val sessionId = backStackEntry.arguments?.getString("sessionId") ?: ""
        val channelName = backStackEntry.arguments?.getString("channelName") ?: ""
        val token = backStackEntry.arguments?.getString("token") ?: ""
        val streamerName = backStackEntry.arguments?.getString("streamerName") ?: ""
        
        LiveStreamViewerScreen(
            sessionId = sessionId,
            channelName = channelName,
            agoraToken = token,
            streamerName = streamerName,
            onExit = {
                navController.popBackStack()
            }
        )
    }
}
```

---

### 2️⃣ Botón para Iniciar Live (Streamer)

```kotlin
// En tu ProfileScreen o LiveLauncherScreen
@Composable
fun StartLiveButton(
    navController: NavController,
    userId: String,
    userName: String
) {
    var isLoading by remember { mutableStateOf(false) }
    
    Button(
        onClick = {
            isLoading = true
            
            // Generar datos del Live
            val sessionId = UUID.randomUUID().toString()
            val channelName = "live_${userId}_${System.currentTimeMillis()}"
            
            // Llamar a Cloud Function para obtener token
            generateBroadcasterToken(
                channelName = channelName,
                uid = 0 // Agora asignará un UID automáticamente
            ) { token ->
                isLoading = false
                
                if (token != null) {
                    // Crear sesión en Firestore
                    createLiveSession(
                        sessionId = sessionId,
                        channelName = channelName,
                        streamerId = userId,
                        streamerName = userName
                    )
                    
                    // Navegar a LiveRecordingScreen
                    navController.navigate(
                        "live_recording/$sessionId/$channelName/$token"
                    )
                } else {
                    // Mostrar error
                    android.util.Log.e("StartLive", "Error obteniendo token")
                }
            }
        },
        enabled = !isLoading,
        colors = ButtonDefaults.buttonColors(
            containerColor = PopArtColors.Pink
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
    ) {
        if (isLoading) {
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(24.dp)
            )
        } else {
            Icon(
                painter = painterResource(R.drawable.ic_live),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text("Iniciar Live", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// Función para generar token de broadcaster
fun generateBroadcasterToken(
    channelName: String,
    uid: Int,
    onResult: (String?) -> Unit
) {
    val functions = Firebase.functions
    val data = hashMapOf(
        "channelName" to channelName,
        "uid" to uid,
        "role" to "publisher" // broadcaster
    )
    
    functions
        .getHttpsCallable("generateAgoraToken")
        .call(data)
        .addOnSuccessListener { result ->
            val token = (result.data as? Map<*, *>)?.get("token") as? String
            onResult(token)
        }
        .addOnFailureListener { e ->
            android.util.Log.e("AgoraToken", "Error: ${e.message}")
            onResult(null)
        }
}

// Función para crear sesión en Firestore
fun createLiveSession(
    sessionId: String,
    channelName: String,
    streamerId: String,
    streamerName: String
) {
    val db = Firebase.firestore
    val liveData = hashMapOf(
        "sessionId" to sessionId,
        "channelName" to channelName,
        "streamerId" to streamerId,
        "streamerName" to streamerName,
        "isActive" to true,
        "viewerCount" to 0,
        "startedAt" to FieldValue.serverTimestamp(),
        "endedAt" to null
    )
    
    db.collection("liveStreams")
        .document(sessionId)
        .set(liveData)
        .addOnSuccessListener {
            android.util.Log.d("Firestore", "✅ Sesión de Live creada")
        }
        .addOnFailureListener { e ->
            android.util.Log.e("Firestore", "❌ Error creando sesión: ${e.message}")
        }
}
```

---

### 3️⃣ Catálogo de Lives Activos (Espectadores)

```kotlin
@Composable
fun ActiveLivesScreen(navController: NavController) {
    val db = Firebase.firestore
    var activeLives by remember { mutableStateOf<List<LiveSession>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Escuchar Lives activos en tiempo real
    LaunchedEffect(Unit) {
        db.collection("liveStreams")
            .whereEqualTo("isActive", true)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("ActiveLives", "Error: ${error.message}")
                    isLoading = false
                    return@addSnapshotListener
                }
                
                activeLives = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(LiveSession::class.java)
                } ?: emptyList()
                
                isLoading = false
            }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PopArtColors.Background)
    ) {
        // Header
        TopAppBar(
            title = { Text("🔴 Lives Activos") },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = PopArtColors.Purple
            )
        )
        
        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PopArtColors.Pink)
            }
        } else if (activeLives.isEmpty()) {
            // Sin Lives activos
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "📺",
                    fontSize = 80.sp
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "No hay Lives activos",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    "Vuelve más tarde o inicia tu propio Live",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        } else {
            // Lista de Lives
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(activeLives) { live ->
                    LiveCard(
                        live = live,
                        onClick = {
                            // Generar token de viewer
                            generateViewerToken(
                                channelName = live.channelName,
                                uid = 0
                            ) { token ->
                                if (token != null) {
                                    navController.navigate(
                                        "live_viewer/${live.sessionId}/${live.channelName}/$token/${live.streamerName}"
                                    )
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LiveCard(
    live: LiveSession,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar del streamer
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(PopArtColors.Pink, shape = MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    live.streamerName.first().toString(),
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(Modifier.width(16.dp))
            
            // Información
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Indicador LIVE
                Surface(
                    color = Color.Red,
                    shape = MaterialTheme.shapes.small
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(Color.White, shape = MaterialTheme.shapes.small)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "LIVE",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
                
                Spacer(Modifier.height(8.dp))
                
                Text(
                    live.streamerName,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                
                Spacer(Modifier.height(4.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("👁️", fontSize = 12.sp)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${live.viewerCount} viendo",
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }
            }
            
            // Icono de play
            Icon(
                painter = painterResource(R.drawable.ic_play),
                contentDescription = "Ver Live",
                tint = PopArtColors.Pink,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

// Función para generar token de viewer
fun generateViewerToken(
    channelName: String,
    uid: Int,
    onResult: (String?) -> Unit
) {
    val functions = Firebase.functions
    val data = hashMapOf(
        "channelName" to channelName,
        "uid" to uid,
        "role" to "subscriber" // viewer/audience
    )
    
    functions
        .getHttpsCallable("generateAgoraToken")
        .call(data)
        .addOnSuccessListener { result ->
            val token = (result.data as? Map<*, *>)?.get("token") as? String
            onResult(token)
        }
        .addOnFailureListener { e ->
            android.util.Log.e("AgoraToken", "Error: ${e.message}")
            onResult(null)
        }
}
```

---

### 4️⃣ Modelo de Datos

```kotlin
// LiveSession.kt
data class LiveSession(
    val sessionId: String = "",
    val channelName: String = "",
    val streamerId: String = "",
    val streamerName: String = "",
    val isActive: Boolean = false,
    val viewerCount: Int = 0,
    val startedAt: com.google.firebase.Timestamp? = null,
    val endedAt: com.google.firebase.Timestamp? = null
)
```

---

### 5️⃣ Cloud Function para Generar Tokens

```javascript
// functions/index.js
const functions = require('firebase-functions');
const { RtcTokenBuilder, RtcRole } = require('agora-access-token');

const APP_ID = '72117baf2c874766b556e6f83ac9c58d';
const APP_CERTIFICATE = 'f907826ae8ff4c00b7057d15b6f2e628'; // SECRETO

exports.generateAgoraToken = functions.https.onCall((data, context) => {
    const { channelName, uid, role } = data;
    
    // Validar parámetros
    if (!channelName || uid === undefined || !role) {
        throw new functions.https.HttpsError(
            'invalid-argument',
            'Faltan parámetros requeridos'
        );
    }
    
    // Determinar rol
    const agoraRole = role === 'publisher' 
        ? RtcRole.PUBLISHER 
        : RtcRole.SUBSCRIBER;
    
    // Token válido por 24 horas
    const expirationTimeInSeconds = 86400;
    const currentTimestamp = Math.floor(Date.now() / 1000);
    const privilegeExpiredTs = currentTimestamp + expirationTimeInSeconds;
    
    // Generar token
    const token = RtcTokenBuilder.buildTokenWithUid(
        APP_ID,
        APP_CERTIFICATE,
        channelName,
        uid,
        agoraRole,
        privilegeExpiredTs
    );
    
    console.log('✅ Token generado:', {
        channelName,
        uid,
        role: agoraRole
    });
    
    return { token };
});
```

---

## 🚀 Flujo Completo

### Para el Streamer:
1. Usuario presiona "Iniciar Live"
2. App genera `sessionId` y `channelName`
3. App llama a Cloud Function para obtener token de broadcaster
4. App crea documento en Firestore (`liveStreams/{sessionId}`)
5. App navega a `LiveRecordingScreen`
6. Usuario transmite en vivo
7. Al finalizar, actualiza Firestore (`isActive = false`)

### Para el Espectador:
1. Usuario ve lista de Lives activos (desde Firestore)
2. Usuario selecciona un Live
3. App llama a Cloud Function para obtener token de viewer
4. App navega a `LiveStreamViewerScreen`
5. Usuario ve la transmisión en tiempo real
6. Al salir, regresa a la lista

---

## ✅ Checklist de Implementación

- [ ] Agregar rutas de navegación en NavHost
- [ ] Crear botón "Iniciar Live" en perfil
- [ ] Implementar `generateBroadcasterToken()`
- [ ] Implementar `generateViewerToken()`
- [ ] Crear pantalla de Lives activos
- [ ] Configurar Cloud Function en Firebase
- [ ] Probar flujo completo en dispositivos reales
- [ ] Agregar manejo de errores
- [ ] Implementar sistema de comentarios (opcional)
- [ ] Agregar notificaciones push (opcional)

¡Todo listo para integrar! 🎉
