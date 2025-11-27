package com.metu.hypematch

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.animation.core.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import coil.compose.rememberAsyncImagePainter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Función para generar la imagen de portada automáticamente
suspend fun generateCoverImage(
    context: android.content.Context,
    profileImageUrl: String?,
    username: String
): Bitmap = withContext(Dispatchers.IO) {
    val width = 800
    val height = 800
    
    // Crear bitmap base
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    
    val paint = Paint().apply {
        isAntiAlias = true
    }
    
    // Cargar la plantilla user_plantilla como fondo
    try {
        val templateBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.user_plantilla)
        val scaledTemplate = Bitmap.createScaledBitmap(templateBitmap, width, height, true)
        canvas.drawBitmap(scaledTemplate, 0f, 0f, paint)
        android.util.Log.d("CoverGenerator", "✅ Plantilla user_plantilla cargada como fondo")
    } catch (e: Exception) {
        android.util.Log.e("CoverGenerator", "❌ Error cargando plantilla: ${e.message}")
        // Fondo de respaldo si falla la plantilla
        paint.color = android.graphics.Color.parseColor("#FFD700")
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }
    
    // Cargar y dibujar la foto de perfil del usuario desde Firebase
    if (!profileImageUrl.isNullOrEmpty()) {
        try {
            // Descargar la imagen desde la URL de Firebase
            val url = java.net.URL(profileImageUrl)
            val connection = url.openConnection()
            connection.connect()
            val inputStream = connection.getInputStream()
            val profileBitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            
            if (profileBitmap != null) {
                // Crear versión circular de la foto de perfil (más grande para encuadrar con la plantilla)
                val profileSize = 450
                val scaledProfile = Bitmap.createScaledBitmap(profileBitmap, profileSize, profileSize, true)
                
                // Crear bitmap circular
                val circularBitmap = Bitmap.createBitmap(profileSize, profileSize, Bitmap.Config.ARGB_8888)
                val circularCanvas = Canvas(circularBitmap)
                
                // Dibujar círculo blanco de fondo
                paint.color = android.graphics.Color.WHITE
                circularCanvas.drawCircle(profileSize / 2f, profileSize / 2f, profileSize / 2f, paint)
                
                // Aplicar máscara circular a la foto
                paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
                circularCanvas.drawBitmap(scaledProfile, 0f, 0f, paint)
                paint.xfermode = null
                
                // Dibujar la foto circular en el centro del canvas
                val left = (width - profileSize) / 2f
                val top = (height - profileSize) / 2f
                canvas.drawBitmap(circularBitmap, left, top, null)
                
                android.util.Log.d("CoverGenerator", "✅ Foto de perfil cargada desde Firebase")
            }
        } catch (e: Exception) {
            android.util.Log.e("CoverGenerator", "❌ Error cargando foto de perfil: ${e.message}")
            // Si falla, dibujar inicial
            drawUserInitial(canvas, username, width, height, paint)
        }
    } else {
        // Si no hay foto, dibujar inicial del usuario
        drawUserInitial(canvas, username, width, height, paint)
    }
    
    // Dibujar el nombre del usuario en la parte superior (color negro)
    if (username.isNotEmpty()) {
        paint.color = android.graphics.Color.BLACK
        paint.textSize = 65f
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.setShadowLayer(0f, 0f, 0f, android.graphics.Color.TRANSPARENT) // Sin sombra
        
        val textY = 100f // Posición superior
        canvas.drawText(username, width / 2f, textY, paint)
    }
    
    bitmap
}

// Función auxiliar para dibujar la inicial del usuario
private fun drawUserInitial(canvas: Canvas, username: String, width: Int, height: Int, paint: Paint) {
    if (username.isNotEmpty()) {
        // Círculo blanco de fondo (más grande para encuadrar con la plantilla)
        paint.color = android.graphics.Color.WHITE
        canvas.drawCircle(width / 2f, height / 2f, 225f, paint)
        
        // Inicial del usuario
        paint.color = android.graphics.Color.parseColor("#FF1493")
        paint.textSize = 260f
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        
        val initial = username.first().uppercaseChar().toString()
        val textY = height / 2f + 90f
        canvas.drawText(initial, width / 2f, textY, paint)
    }
}

// Función para generar portada con foto personalizada
suspend fun generateCoverImageWithCustomPhoto(
    context: android.content.Context,
    customPhoto: Bitmap,
    username: String
): Bitmap = withContext(Dispatchers.IO) {
    val width = 800
    val height = 800
    
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    
    val paint = Paint().apply {
        isAntiAlias = true
    }
    
    // Cargar la plantilla user_plantilla como fondo
    try {
        val templateBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.user_plantilla)
        val scaledTemplate = Bitmap.createScaledBitmap(templateBitmap, width, height, true)
        canvas.drawBitmap(scaledTemplate, 0f, 0f, paint)
    } catch (e: Exception) {
        paint.color = android.graphics.Color.parseColor("#FFD700")
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
    }
    
    // Crear versión circular de la foto personalizada
    val profileSize = 450
    val scaledProfile = Bitmap.createScaledBitmap(customPhoto, profileSize, profileSize, true)
    
    val circularBitmap = Bitmap.createBitmap(profileSize, profileSize, Bitmap.Config.ARGB_8888)
    val circularCanvas = Canvas(circularBitmap)
    
    paint.color = android.graphics.Color.WHITE
    circularCanvas.drawCircle(profileSize / 2f, profileSize / 2f, profileSize / 2f, paint)
    
    paint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
    circularCanvas.drawBitmap(scaledProfile, 0f, 0f, paint)
    paint.xfermode = null
    
    val left = (width - profileSize) / 2f
    val top = (height - profileSize) / 2f
    canvas.drawBitmap(circularBitmap, left, top, null)
    
    // Dibujar el nombre del usuario arriba
    if (username.isNotEmpty()) {
        paint.color = android.graphics.Color.BLACK
        paint.textSize = 65f
        paint.textAlign = Paint.Align.CENTER
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.setShadowLayer(0f, 0f, 0f, android.graphics.Color.TRANSPARENT)
        
        val textY = 100f
        canvas.drawText(username, width / 2f, textY, paint)
    }
    
    bitmap
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadMusicScreen(onBack: () -> Unit) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val focusManager = LocalFocusManager.current
    val firebaseManager = remember { FirebaseManager() }
    val authManager = remember { AuthManager(context) }
    val scope = rememberCoroutineScope()
    val userId = authManager.getUserId() ?: ""
    
    var title by remember { mutableStateOf("") }
    var artistName by remember { mutableStateOf("") }
    var genre by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var selectedAudioUri by remember { mutableStateOf<Uri?>(null) }
    var bio by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableStateOf(0) }
    var uploadMessage by remember { mutableStateOf("") }
    var generatedCoverBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    var customCoverUri by remember { mutableStateOf<Uri?>(null) }
    var customVideoUri by remember { mutableStateOf<Uri?>(null) }
    var showCoverOptionsDialog by remember { mutableStateOf(false) }
    
    // Cargar el nombre del usuario y foto de perfil automáticamente
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            try {
                val profile = firebaseManager.getUserProfile(userId)
                val username = profile?.username ?: ""
                val photoUrl = profile?.profileImageUrl
                
                if (username.isNotEmpty()) {
                    artistName = username
                    profileImageUrl = photoUrl
                    android.util.Log.d("UploadMusicScreen", "✅ Nombre del artista autocompletado: $username")
                }
            } catch (e: Exception) {
                android.util.Log.e("UploadMusicScreen", "❌ Error cargando perfil: ${e.message}")
            }
        }
    }
    
    // Generar portada automáticamente cuando cambie el perfil o el nombre
    LaunchedEffect(profileImageUrl, artistName) {
        if (artistName.isNotEmpty()) {
            try {
                val coverBitmap = generateCoverImage(context, profileImageUrl, artistName)
                generatedCoverBitmap = coverBitmap
                android.util.Log.d("UploadMusicScreen", "✅ Portada generada automáticamente")
            } catch (e: Exception) {
                android.util.Log.e("UploadMusicScreen", "❌ Error generando portada: ${e.message}")
            }
        }
    }

    // Selector de archivos de audio
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedAudioUri = uri
    }
    
    // Selector de imagen personalizada para portada
    val customImagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        customCoverUri = uri
        if (uri != null) {
            // Cargar la imagen personalizada directamente (sin plantilla)
            scope.launch {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val customBitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()
                    
                    if (customBitmap != null) {
                        // Crear imagen cuadrada 800x800 con crop centrado
                        val size = 800
                        val width = customBitmap.width
                        val height = customBitmap.height
                        
                        // Calcular el tamaño del crop (el lado más pequeño)
                        val cropSize = minOf(width, height)
                        
                        // Calcular las coordenadas para centrar el crop
                        val x = (width - cropSize) / 2
                        val y = (height - cropSize) / 2
                        
                        // Crear bitmap cuadrado centrado
                        val croppedBitmap = Bitmap.createBitmap(customBitmap, x, y, cropSize, cropSize)
                        
                        // Escalar a 800x800
                        val scaledBitmap = Bitmap.createScaledBitmap(croppedBitmap, size, size, true)
                        
                        generatedCoverBitmap = scaledBitmap
                        android.util.Log.d("UploadMusicScreen", "✅ Foto personalizada cargada (${width}x${height} -> ${size}x${size})")
                    }
                } catch (e: Exception) {
                    android.util.Log.e("UploadMusicScreen", "❌ Error con foto personalizada: ${e.message}")
                }
            }
        }
    }
    
    // Selector de video para portada
    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        customVideoUri = uri
        if (uri != null) {
            android.util.Log.d("UploadMusicScreen", "✅ Video seleccionado como portada")
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "SUBIR MÚSICA",
                            fontWeight = FontWeight.Black,
                            color = PopArtColors.Yellow
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                Icons.Default.ArrowBack,
                                contentDescription = "Volver",
                                tint = PopArtColors.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = PopArtColors.Black
                    )
                )
            },
            containerColor = PopArtColors.Black
        ) { padding ->
        androidx.compose.foundation.lazy.LazyColumn(
            modifier = Modifier
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                }
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .pointerInput(Unit) {
                    detectTapGestures(onTap = {
                        focusManager.clearFocus()
                    })
                },
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Imagen de portada generada automáticamente
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "PORTADA DE LA CANCIÓN",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PopArtColors.Yellow,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    // Portada
                    if (customVideoUri != null) {
                        // Mostrar preview de video con plantilla de fondo
                        Box(
                            modifier = Modifier
                                .size(250.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(4.dp, PopArtColors.Purple, RoundedCornerShape(16.dp))
                        ) {
                            // Plantilla de fondo para videos
                            Image(
                                painter = androidx.compose.ui.res.painterResource(id = R.drawable.user_plantilla_video),
                                contentDescription = "Fondo de video",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            
                            // Video encima de la plantilla
                            androidx.compose.ui.viewinterop.AndroidView(
                                factory = { ctx ->
                                    android.widget.VideoView(ctx).apply {
                                        setBackgroundColor(android.graphics.Color.TRANSPARENT)
                                        setVideoURI(customVideoUri)
                                        setOnPreparedListener { mp ->
                                            mp.isLooping = true
                                            mp.setVolume(0f, 0f) // Sin sonido
                                        }
                                        start()
                                    }
                                },
                                update = { videoView ->
                                    // Actualizar el video cuando cambie el URI
                                    videoView.stopPlayback()
                                    videoView.setVideoURI(customVideoUri)
                                    videoView.setOnPreparedListener { mp ->
                                        mp.isLooping = true
                                        mp.setVolume(0f, 0f)
                                    }
                                    videoView.start()
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                            
                            // Overlay con indicador de video
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(PopArtColors.Black.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_play),
                                    contentDescription = "Video",
                                    tint = PopArtColors.Yellow,
                                    modifier = Modifier.size(60.dp)
                                )
                            }
                        }
                    } else if (generatedCoverBitmap != null) {
                        Image(
                            bitmap = generatedCoverBitmap!!.asImageBitmap(),
                            contentDescription = "Portada generada",
                            modifier = Modifier
                                .size(250.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .border(
                                    4.dp,
                                    if (customCoverUri != null) PopArtColors.Purple else PopArtColors.Yellow,
                                    RoundedCornerShape(16.dp)
                                ),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        // Placeholder mientras se genera
                        Box(
                            modifier = Modifier
                                .size(250.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(PopArtColors.White.copy(alpha = 0.1f))
                                .border(4.dp, PopArtColors.Yellow, RoundedCornerShape(16.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = PopArtColors.Yellow,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }
                    
                    Spacer(Modifier.height(12.dp))
                    
                    // Icono de edición debajo de la portada
                    IconButton(
                        onClick = { showCoverOptionsDialog = true },
                        modifier = Modifier
                            .size(56.dp)
                            .background(PopArtColors.Yellow, CircleShape)
                    ) {
                        Icon(
                            painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_edit),
                            contentDescription = "Editar portada",
                            tint = PopArtColors.Black,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    
                    Spacer(Modifier.height(8.dp))
                    
                    Text(
                        if (customVideoUri != null) "Video como portada (se reproduce en bucle)"
                        else if (customCoverUri != null) "Foto personalizada (sin plantilla)"
                        else "Se genera automáticamente con tu foto de perfil",
                        fontSize = 12.sp,
                        color = PopArtColors.White.copy(alpha = 0.7f)
                    )
                }
                
                // Diálogo de opciones de portada
                if (showCoverOptionsDialog) {
                    AlertDialog(
                        onDismissRequest = { showCoverOptionsDialog = false },
                        title = {
                            Text(
                                "Personalizar Portada",
                                fontWeight = FontWeight.Black,
                                color = PopArtColors.Black
                            )
                        },
                        text = {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Opción: Cambiar foto
                                Button(
                                    onClick = {
                                        showCoverOptionsDialog = false
                                        customImagePickerLauncher.launch("image/*")
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = PopArtColors.Yellow)
                                ) {
                                    Icon(
                                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_camara),
                                        contentDescription = null,
                                        tint = PopArtColors.Black,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Cambiar Foto", color = PopArtColors.Black, fontWeight = FontWeight.Bold)
                                }
                                
                                // Opción: Agregar video
                                Button(
                                    onClick = {
                                        showCoverOptionsDialog = false
                                        videoPickerLauncher.launch("video/*")
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = ButtonDefaults.buttonColors(containerColor = PopArtColors.Pink)
                                ) {
                                    Icon(
                                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_play),
                                        contentDescription = null,
                                        tint = PopArtColors.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Agregar Video", color = PopArtColors.White, fontWeight = FontWeight.Bold)
                                }
                                
                                // Opción: Restaurar predeterminada
                                if (customCoverUri != null || customVideoUri != null) {
                                    Button(
                                        onClick = {
                                            showCoverOptionsDialog = false
                                            customCoverUri = null
                                            customVideoUri = null
                                            // Regenerar portada predeterminada
                                            scope.launch {
                                                try {
                                                    val coverBitmap = generateCoverImage(context, profileImageUrl, artistName)
                                                    generatedCoverBitmap = coverBitmap
                                                    android.util.Log.d("UploadMusicScreen", "✅ Portada predeterminada restaurada")
                                                } catch (e: Exception) {
                                                    android.util.Log.e("UploadMusicScreen", "❌ Error restaurando portada: ${e.message}")
                                                }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth(),
                                        colors = ButtonDefaults.buttonColors(containerColor = PopArtColors.White)
                                    ) {
                                        Icon(
                                            painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_back),
                                            contentDescription = null,
                                            tint = PopArtColors.Black,
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Spacer(Modifier.width(8.dp))
                                        Text("Restaurar Predeterminada", color = PopArtColors.Black, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { showCoverOptionsDialog = false }) {
                                Text("Cerrar", color = PopArtColors.Black, fontWeight = FontWeight.Bold)
                            }
                        },
                        containerColor = PopArtColors.White
                    )
                }
            }
            
            item {
            // Campo: Nombre del artista (Autocompletado y de solo lectura)
            OutlinedTextField(
                value = artistName,
                onValueChange = { }, // No editable
                label = { Text("Nombre del artista") },
                modifier = Modifier.fillMaxWidth(),
                enabled = false, // Campo de solo lectura
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = PopArtColors.Yellow,
                    disabledBorderColor = PopArtColors.Yellow,
                    disabledLabelColor = PopArtColors.Yellow
                ),
                supportingText = {
                    Text(
                        "Tu nombre de usuario se usa automáticamente",
                        color = PopArtColors.White.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                }
            )

            // Campo: Título de la canción
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Título de la canción") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = PopArtColors.White,
                    unfocusedTextColor = PopArtColors.White,
                    focusedBorderColor = PopArtColors.Yellow,
                    unfocusedBorderColor = PopArtColors.White,
                    focusedLabelColor = PopArtColors.Yellow,
                    unfocusedLabelColor = PopArtColors.White
                )
            )

            // Campo: Género musical (Dropdown)
            var showGenreMenu by remember { mutableStateOf(false) }
            var showCustomGenreDialog by remember { mutableStateOf(false) }
            
            val genreOptions = listOf(
                "Pop",
                "Rock",
                "Reggaetón",
                "Música Clásica",
                "Blues",
                "Jazz",
                "Música Independiente",
                "Otro (personalizado)"
            )
            
            Box {
                OutlinedTextField(
                    value = genre,
                    onValueChange = { },
                    label = { Text("Género musical") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showGenreMenu = true },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = PopArtColors.White,
                        disabledBorderColor = PopArtColors.White,
                        disabledLabelColor = PopArtColors.White
                    ),
                    trailingIcon = {
                        Icon(
                            painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_arrow_drop_down),
                            contentDescription = "Seleccionar género",
                            tint = PopArtColors.Yellow,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                )
                
                DropdownMenu(
                    expanded = showGenreMenu,
                    onDismissRequest = { showGenreMenu = false },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .background(PopArtColors.White)
                ) {
                    genreOptions.forEach { genreOption ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    genreOption,
                                    color = PopArtColors.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            onClick = {
                                if (genreOption == "Otro (personalizado)") {
                                    showGenreMenu = false
                                    showCustomGenreDialog = true
                                } else {
                                    genre = genreOption
                                    showGenreMenu = false
                                }
                            }
                        )
                    }
                }
            }
            
            // Diálogo para género personalizado
            if (showCustomGenreDialog) {
                var customGenre by remember { mutableStateOf("") }
                
                AlertDialog(
                    onDismissRequest = { showCustomGenreDialog = false },
                    title = {
                        Text(
                            "Género Personalizado",
                            fontWeight = FontWeight.Black,
                            color = PopArtColors.Black
                        )
                    },
                    text = {
                        OutlinedTextField(
                            value = customGenre,
                            onValueChange = { customGenre = it },
                            label = { Text("Escribe el género") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PopArtColors.Yellow,
                                focusedLabelColor = PopArtColors.Yellow
                            )
                        )
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                if (customGenre.isNotBlank()) {
                                    genre = customGenre
                                    showCustomGenreDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PopArtColors.Yellow),
                            enabled = customGenre.isNotBlank()
                        ) {
                            Text("Guardar", color = PopArtColors.Black, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCustomGenreDialog = false }) {
                            Text("Cancelar", color = PopArtColors.Black)
                        }
                    },
                    containerColor = PopArtColors.White
                )
            }

            // Campo: Ciudad (Dropdown)
            var showCityMenu by remember { mutableStateOf(false) }
            
            val cityOptions = listOf(
                "Lima",
                "Arequipa",
                "Cusco",
                "Trujillo",
                "Chiclayo",
                "Piura",
                "Iquitos",
                "Huancayo",
                "Tacna",
                "Puno",
                "Ayacucho",
                "Cajamarca",
                "Ica",
                "Juliaca",
                "Huánuco"
            )
            
            Box {
                OutlinedTextField(
                    value = location,
                    onValueChange = { },
                    label = { Text("Ciudad") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCityMenu = true },
                    enabled = false,
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = PopArtColors.White,
                        disabledBorderColor = PopArtColors.White,
                        disabledLabelColor = PopArtColors.White
                    ),
                    trailingIcon = {
                        Icon(
                            painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_arrow_drop_down),
                            contentDescription = "Seleccionar ciudad",
                            tint = PopArtColors.Yellow,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                )
                
                DropdownMenu(
                    expanded = showCityMenu,
                    onDismissRequest = { showCityMenu = false },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .background(PopArtColors.White)
                ) {
                    cityOptions.forEach { city ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    city,
                                    color = PopArtColors.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            },
                            onClick = {
                                location = city
                                showCityMenu = false
                            }
                        )
                    }
                }
            }

            // Botón: Seleccionar archivo de audio
            Button(
                onClick = { audioPickerLauncher.launch("audio/*") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedAudioUri != null) PopArtColors.Cyan else PopArtColors.White
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = PopArtColors.Black
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    if (selectedAudioUri != null) "✓ Audio seleccionado" else "Seleccionar archivo MP3",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = PopArtColors.Black
                )
            }

            // Campo: Biografía (opcional)
            OutlinedTextField(
                value = bio,
                onValueChange = { bio = it },
                label = { Text("Sobre ti (opcional)") },
                placeholder = { Text("Cuéntanos sobre tu música...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 4,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = PopArtColors.White,
                    unfocusedTextColor = PopArtColors.White,
                    focusedBorderColor = PopArtColors.Yellow,
                    unfocusedBorderColor = PopArtColors.White,
                    focusedLabelColor = PopArtColors.Yellow,
                    unfocusedLabelColor = PopArtColors.White
                )
            )

            // Barra de progreso
            if (isUploading) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    LinearProgressIndicator(
                        progress = uploadProgress / 100f,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp),
                        color = PopArtColors.Yellow,
                        trackColor = PopArtColors.White.copy(alpha = 0.3f)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Subiendo... $uploadProgress%",
                        color = PopArtColors.White,
                        fontSize = 14.sp
                    )
                }
            }

            // Mensaje de estado
            if (uploadMessage.isNotEmpty()) {
                Text(
                    uploadMessage,
                    color = if (uploadMessage.contains("Error")) PopArtColors.Pink else PopArtColors.Cyan,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(Modifier.height(24.dp))

            // Botón: Subir canción
            Button(
                onClick = {
                    if (selectedAudioUri != null && title.isNotBlank() && artistName.isNotBlank()) {
                        isUploading = true
                        uploadMessage = ""

                        scope.launch {
                            try {
                                // Subir portada (imagen o video)
                                val imageUrl: String
                                val videoUrl: String
                                
                                if (customVideoUri != null) {
                                    // Subir video como portada
                                    uploadMessage = "Subiendo video de portada..."
                                    videoUrl = firebaseManager.uploadVideoFile(customVideoUri!!) { progress ->
                                        uploadProgress = progress / 3 // 33% para el video
                                    }
                                    imageUrl = "" // No hay imagen si hay video
                                } else if (customCoverUri != null) {
                                    // Subir foto personalizada directamente
                                    uploadMessage = "Subiendo foto personalizada..."
                                    imageUrl = firebaseManager.uploadImageFile(customCoverUri!!) { progress ->
                                        uploadProgress = progress / 2 // 50% para la imagen
                                    }
                                    videoUrl = ""
                                } else if (generatedCoverBitmap != null) {
                                    // Subir imagen generada con plantilla
                                    uploadMessage = "Subiendo portada..."
                                    
                                    // Convertir bitmap a URI temporal
                                    val tempFile = java.io.File(context.cacheDir, "cover_${System.currentTimeMillis()}.jpg")
                                    val outputStream = java.io.FileOutputStream(tempFile)
                                    generatedCoverBitmap!!.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                                    outputStream.flush()
                                    outputStream.close()
                                    
                                    val tempUri = androidx.core.content.FileProvider.getUriForFile(
                                        context,
                                        "${context.packageName}.fileprovider",
                                        tempFile
                                    )
                                    
                                    imageUrl = firebaseManager.uploadImageFile(tempUri) { progress ->
                                        uploadProgress = progress / 2 // 50% para la imagen
                                    }
                                    videoUrl = ""
                                } else {
                                    imageUrl = ""
                                    videoUrl = ""
                                }
                                
                                // Subir archivo de audio
                                uploadMessage = "Subiendo audio..."
                                val audioUrl = firebaseManager.uploadAudioFile(selectedAudioUri!!) { progress ->
                                    uploadProgress = if (generatedCoverBitmap != null) 50 + (progress / 2) else progress
                                }

                                // Guardar metadata
                                uploadMessage = "Guardando información..."
                                val songData = UploadSongData(
                                    title = title,
                                    artistName = artistName,
                                    artistId = userId, // ID del usuario que sube la canción
                                    genre = genre.ifBlank { "Sin género" },
                                    location = location.ifBlank { "Sin ubicación" },
                                    audioUrl = audioUrl,
                                    imageUrl = imageUrl,
                                    videoUrl = videoUrl,
                                    bio = bio.ifBlank { "Artista independiente" }
                                )

                                firebaseManager.saveSongMetadata(songData)

                                uploadMessage = "✓ ¡Canción subida exitosamente!"
                                isUploading = false

                                // Limpiar formulario
                                title = ""
                                genre = ""
                                location = ""
                                selectedAudioUri = null
                                customCoverUri = null
                                customVideoUri = null
                                bio = ""
                                uploadProgress = 0
                                
                                // Recargar el nombre del artista y regenerar portada
                                val profile = firebaseManager.getUserProfile(userId)
                                val username = profile?.username ?: ""
                                val photoUrl = profile?.profileImageUrl
                                if (username.isNotEmpty()) {
                                    artistName = username
                                    profileImageUrl = photoUrl
                                    // Regenerar portada predeterminada
                                    val coverBitmap = generateCoverImage(context, profileImageUrl, artistName)
                                    generatedCoverBitmap = coverBitmap
                                }

                            } catch (e: Exception) {
                                uploadMessage = "Error: ${e.message}"
                                isUploading = false
                            }
                        }
                    } else {
                        uploadMessage = "Por favor completa todos los campos y selecciona un archivo"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PopArtColors.Yellow),
                shape = RoundedCornerShape(30.dp),
                enabled = !isUploading
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    tint = PopArtColors.Black
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "PUBLICAR CANCIÓN",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = PopArtColors.Black
                )
            }
            }
        }
        
        // Overlay de carga cuando se está subiendo
        if (isUploading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(PopArtColors.Black.copy(alpha = 0.95f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Logo HYPE
                    Text(
                        "HYPE",
                        fontSize = 64.sp,
                        fontWeight = FontWeight.Black,
                        color = PopArtColors.Yellow
                    )
                    
                    // Icono de carga animado
                    val infiniteTransition = rememberInfiniteTransition(label = "loading")
                    val rotation by infiniteTransition.animateFloat(
                        initialValue = 0f,
                        targetValue = 360f,
                        animationSpec = infiniteRepeatable(
                            animation = tween(1000, easing = LinearEasing),
                            repeatMode = RepeatMode.Restart
                        ),
                        label = "rotation"
                    )
                    
                    Icon(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.ic_loading),
                        contentDescription = "Cargando",
                        tint = PopArtColors.Yellow,
                        modifier = Modifier
                            .size(80.dp)
                            .rotate(rotation)
                    )
                    
                    // Mensaje de progreso
                    Text(
                        uploadMessage,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PopArtColors.White
                    )
                    
                    // Barra de progreso
                    LinearProgressIndicator(
                        progress = uploadProgress / 100f,
                        modifier = Modifier
                            .width(200.dp)
                            .height(8.dp),
                        color = PopArtColors.Yellow,
                        trackColor = PopArtColors.White.copy(alpha = 0.3f)
                    )
                    
                    Text(
                        "$uploadProgress%",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = PopArtColors.Yellow
                    )
                }
            }
        }
    } // Cierre del Box principal
} // Cierre de la función UploadMusicScreen
}