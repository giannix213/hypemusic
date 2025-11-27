package com.metu.hypematch

import android.net.Uri
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirebaseManager {
    private val firestore = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    // Subir archivo de audio a Firebase Storage
    suspend fun uploadAudioFile(uri: Uri, onProgress: (Int) -> Unit): String {
        val fileName = "songs/${UUID.randomUUID()}.mp3"
        val storageRef = storage.reference.child(fileName)

        return try {
            val uploadTask = storageRef.putFile(uri)

            // Monitorear progreso
            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                onProgress(progress)
            }

            uploadTask.await()

            // Obtener URL de descarga
            storageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            throw Exception("Error al subir archivo: ${e.message}")
        }
    }
    
    // Subir imagen a Firebase Storage
    suspend fun uploadImageFile(uri: Uri, onProgress: (Int) -> Unit): String {
        val fileName = "images/${UUID.randomUUID()}.jpg"
        val storageRef = storage.reference.child(fileName)

        return try {
            val uploadTask = storageRef.putFile(uri)

            // Monitorear progreso
            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                onProgress(progress)
            }

            uploadTask.await()

            // Obtener URL de descarga
            storageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            throw Exception("Error al subir imagen: ${e.message}")
        }
    }
    
    // Subir video a Firebase Storage
    suspend fun uploadVideoFile(uri: Uri, onProgress: (Int) -> Unit): String {
        val fileName = "videos/${UUID.randomUUID()}.mp4"
        val storageRef = storage.reference.child(fileName)

        return try {
            val uploadTask = storageRef.putFile(uri)

            // Monitorear progreso
            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                onProgress(progress)
            }

            uploadTask.await()

            // Obtener URL de descarga
            storageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            throw Exception("Error al subir video: ${e.message}")
        }
    }

    // Guardar metadata de la canción en Firestore
    suspend fun saveSongMetadata(songData: UploadSongData): String {
        return try {
            // Guardar la canción
            val docRef = firestore.collection("songs").add(songData).await()
            
            // Incrementar contador de canciones del usuario
            if (songData.artistId.isNotEmpty()) {
                firestore.collection("users").document(songData.artistId)
                    .update("totalSongs", com.google.firebase.firestore.FieldValue.increment(1))
                    .await()
                
                android.util.Log.d("FirebaseManager", "✅ Contador de canciones incrementado para usuario: ${songData.artistId}")
            }
            
            docRef.id
        } catch (e: Exception) {
            throw Exception("Error al guardar canción: ${e.message}")
        }
    }
    
    // Eliminar una canción por su URL de media (video o imagen)
    suspend fun deleteSongByMediaUrl(userId: String, mediaUrl: String) {
        try {
            // Buscar la canción que contiene esta URL
            val snapshot = firestore.collection("songs")
                .whereEqualTo("artistId", userId)
                .get()
                .await()
            
            for (doc in snapshot.documents) {
                val data = doc.data ?: continue
                val videoUrl = data["videoUrl"] as? String ?: ""
                val imageUrl = data["imageUrl"] as? String ?: ""
                
                // Si la URL coincide con el video o imagen, eliminar la canción
                if (videoUrl == mediaUrl || imageUrl == mediaUrl) {
                    firestore.collection("songs").document(doc.id).delete().await()
                    
                    // Decrementar contador de canciones del usuario
                    firestore.collection("users").document(userId)
                        .update("totalSongs", com.google.firebase.firestore.FieldValue.increment(-1))
                        .await()
                    
                    android.util.Log.d("FirebaseManager", "✅ Canción eliminada: ${doc.id}")
                    break
                }
            }
        } catch (e: Exception) {
            throw Exception("Error al eliminar canción: ${e.message}")
        }
    }

    // Verificar si un archivo existe en Firebase Storage
    private suspend fun fileExists(url: String): Boolean {
        return try {
            if (url.isEmpty()) return false
            
            // Extraer la referencia del storage desde la URL
            val storageRef = storage.getReferenceFromUrl(url)
            
            // Intentar obtener metadata del archivo
            storageRef.metadata.await()
            true
        } catch (e: Exception) {
            android.util.Log.w("FirebaseManager", "Archivo no existe o no es accesible: $url")
            false
        }
    }
    
    // Limpiar canciones con archivos rotos
    suspend fun cleanupBrokenSongs() {
        try {
            val snapshot = firestore.collection("songs").get().await()
            val brokenSongs = mutableListOf<String>()
            
            for (doc in snapshot.documents) {
                val audioUrl = doc.data?.get("audioUrl") as? String ?: ""
                if (audioUrl.isNotEmpty() && !fileExists(audioUrl)) {
                    brokenSongs.add(doc.id)
                    android.util.Log.d("FirebaseManager", "Canción con archivo roto encontrada: ${doc.id}")
                }
            }
            
            // Eliminar canciones con archivos rotos
            brokenSongs.forEach { songId ->
                firestore.collection("songs").document(songId).delete().await()
                android.util.Log.d("FirebaseManager", "Canción eliminada: $songId")
            }
            
            if (brokenSongs.isNotEmpty()) {
                android.util.Log.d("FirebaseManager", "Limpieza completada: ${brokenSongs.size} canciones eliminadas")
            }
        } catch (e: Exception) {
            android.util.Log.e("FirebaseManager", "Error en limpieza: ${e.message}")
        }
    }

    // 🚀 OPTIMIZACIÓN 2: PAGINACIÓN - Obtener canciones con límite
    suspend fun getAllSongs(limit: Long = 10, lastSongId: String? = null): List<ArtistCard> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        return@withContext try {
            android.util.Log.d("FirebaseManager", "📥 Obteniendo canciones (límite: $limit, cursor: $lastSongId)...")
            
            // Intentar ordenar por uploadDate, si falla obtener sin ordenar
            var query = firestore.collection("songs")
                .orderBy("uploadDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(limit)
            
            // Si hay cursor, continuar desde ahí (paginación)
            if (lastSongId != null) {
                try {
                    val lastDocument = firestore.collection("songs").document(lastSongId).get().await()
                    if (lastDocument.exists()) {
                        query = firestore.collection("songs")
                            .orderBy("uploadDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
                            .startAfter(lastDocument)
                            .limit(limit)
                    }
                } catch (e: Exception) {
                    android.util.Log.w("FirebaseManager", "⚠️ Error con cursor, ignorando: ${e.message}")
                }
            }
            
            val snapshot = try {
                query.get().await()
            } catch (e: Exception) {
                android.util.Log.w("FirebaseManager", "⚠️ No se pudo ordenar por uploadDate, obteniendo sin ordenar: ${e.message}")
                firestore.collection("songs")
                    .limit(limit)
                    .get()
                    .await()
            }

            android.util.Log.d("FirebaseManager", "📊 Documentos encontrados: ${snapshot.documents.size}")

            val validSongs = mutableListOf<ArtistCard>()
            val brokenSongs = mutableListOf<String>()

            for (doc in snapshot.documents) {
                try {
                    val data = doc.data
                    
                    // Si el documento está vacío o no tiene datos, eliminarlo
                    if (data == null || data.isEmpty()) {
                        android.util.Log.w("FirebaseManager", "⚠️ Documento vacío encontrado: ${doc.id}, eliminando...")
                        try {
                            firestore.collection("songs").document(doc.id).delete().await()
                            android.util.Log.d("FirebaseManager", "✅ Documento vacío eliminado: ${doc.id}")
                        } catch (e: Exception) {
                            android.util.Log.e("FirebaseManager", "❌ Error eliminando documento vacío: ${e.message}")
                        }
                        continue
                    }
                    
                    val audioUrl = data["audioUrl"] as? String ?: ""
                    
                    // Saltar si no hay URL de audio
                    if (audioUrl.isEmpty()) {
                        android.util.Log.d("FirebaseManager", "⚠️ Canción sin audioUrl: ${doc.id}, eliminando...")
                        try {
                            firestore.collection("songs").document(doc.id).delete().await()
                            android.util.Log.d("FirebaseManager", "✅ Canción sin audio eliminada: ${doc.id}")
                        } catch (e: Exception) {
                            android.util.Log.e("FirebaseManager", "❌ Error eliminando canción sin audio: ${e.message}")
                        }
                        continue
                    }
                    
                    val imageUrl = data["imageUrl"] as? String ?: ""
                    val artistId = data["artistId"] as? String ?: ""
                    val artistName = data["artistName"] as? String ?: "Artista Desconocido"
                    val genre = data["genre"] as? String ?: "Sin género"
                    val location = data["location"] as? String ?: "Sin ubicación"
                    val bio = data["bio"] as? String ?: "Artista independiente"
                    val plays = data["plays"] as? Long ?: 0L
                    
                    // Intentar obtener información del perfil del artista (opcional)
                    var artistProfile: UserProfile? = null
                    if (artistId.isNotEmpty()) {
                        try {
                            artistProfile = getFullUserProfile(artistId)
                        } catch (e: Exception) {
                            android.util.Log.w("FirebaseManager", "No se pudo obtener perfil de $artistId: ${e.message}")
                        }
                    }
                    
                    // Crear la tarjeta con los datos disponibles
                    validSongs.add(
                        ArtistCard(
                            id = doc.id,
                            userId = artistId,
                            name = artistName,
                            genre = genre,
                            location = location,
                            emoji = "🎵",
                            imageUrl = artistProfile?.profileImageUrl?.takeIf { it.isNotEmpty() } ?: imageUrl,
                            songUrl = audioUrl,
                            bio = artistProfile?.bio?.takeIf { it.isNotEmpty() } ?: bio,
                            photos = artistProfile?.galleryPhotos?.takeIf { it.isNotEmpty() } 
                                ?: if (imageUrl.isNotEmpty()) listOf(imageUrl) else listOf("🎵", "🎤", "🎸"),
                            socialLinks = artistProfile?.socialLinks ?: emptyMap(),
                            stats = artistProfile?.let {
                                ArtistStats(
                                    followers = formatNumber(it.followers),
                                    songs = it.totalSongs,
                                    plays = formatNumber(it.totalPlays.toInt())
                                )
                            } ?: ArtistStats(
                                followers = "0",
                                songs = 1,
                                plays = plays.toString()
                            )
                        )
                    )
                } catch (e: Exception) {
                    android.util.Log.e("FirebaseManager", "Error procesando canción ${doc.id}: ${e.message}")
                    // Continuar con la siguiente canción
                }
            }
            
            // Eliminar canciones con archivos rotos de la base de datos
            if (brokenSongs.isNotEmpty()) {
                android.util.Log.d("FirebaseManager", "Eliminando ${brokenSongs.size} canciones con archivos rotos")
                brokenSongs.forEach { songId ->
                    try {
                        firestore.collection("songs").document(songId).delete().await()
                    } catch (e: Exception) {
                        android.util.Log.e("FirebaseManager", "Error eliminando canción $songId: ${e.message}")
                    }
                }
            }
            
            validSongs
        } catch (e: Exception) {
            android.util.Log.e("FirebaseManager", "Error obteniendo canciones: ${e.message}")
            emptyList()
        }
    }

    // Incrementar reproducciones
    suspend fun incrementPlays(songId: String) {
        try {
            firestore.collection("songs").document(songId)
                .update("plays", com.google.firebase.firestore.FieldValue.increment(1))
                .await()
        } catch (e: Exception) {
            // Ignorar errores
        }
    }
    
    // Obtener canciones subidas por un usuario específico
    suspend fun getUserSongs(userId: String): List<ArtistCard> {
        return try {
            // Intentar ordenar por uploadDate, si falla obtener sin ordenar
            val snapshot = try {
                firestore.collection("songs")
                    .whereEqualTo("artistId", userId)
                    .orderBy("uploadDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()
            } catch (e: Exception) {
                android.util.Log.w("FirebaseManager", "⚠️ No se pudo ordenar por uploadDate, obteniendo sin ordenar: ${e.message}")
                firestore.collection("songs")
                    .whereEqualTo("artistId", userId)
                    .get()
                    .await()
            }

            snapshot.documents.mapNotNull { doc ->
                try {
                    val data = doc.data ?: return@mapNotNull null
                    val audioUrl = data["audioUrl"] as? String ?: ""
                    
                    if (audioUrl.isEmpty()) return@mapNotNull null
                    
                    val imageUrl = data["imageUrl"] as? String ?: ""
                    val artistId = data["artistId"] as? String ?: ""
                    val artistName = data["artistName"] as? String ?: "Artista Desconocido"
                    val genre = data["genre"] as? String ?: "Sin género"
                    val location = data["location"] as? String ?: "Sin ubicación"
                    val bio = data["bio"] as? String ?: "Artista independiente"
                    val plays = data["plays"] as? Long ?: 0L
                    
                    // Intentar obtener información del perfil del artista (opcional)
                    var artistProfile: UserProfile? = null
                    if (artistId.isNotEmpty()) {
                        try {
                            artistProfile = getFullUserProfile(artistId)
                        } catch (e: Exception) {
                            android.util.Log.w("FirebaseManager", "No se pudo obtener perfil de $artistId: ${e.message}")
                        }
                    }
                    
                    ArtistCard(
                        id = doc.id,
                        userId = artistId,
                        name = artistName,
                        genre = genre,
                        location = location,
                        emoji = "🎵",
                        imageUrl = artistProfile?.profileImageUrl?.takeIf { it.isNotEmpty() } ?: imageUrl,
                        songUrl = audioUrl,
                        bio = artistProfile?.bio?.takeIf { it.isNotEmpty() } ?: bio,
                        photos = artistProfile?.galleryPhotos?.takeIf { it.isNotEmpty() } 
                            ?: if (imageUrl.isNotEmpty()) listOf(imageUrl) else listOf("🎵", "🎤", "🎸"),
                        socialLinks = artistProfile?.socialLinks ?: emptyMap(),
                        stats = artistProfile?.let {
                            ArtistStats(
                                followers = formatNumber(it.followers),
                                songs = it.totalSongs,
                                plays = formatNumber(it.totalPlays.toInt())
                            )
                        } ?: ArtistStats(
                            followers = "0",
                            songs = 1,
                            plays = plays.toString()
                        )
                    )
                } catch (e: Exception) {
                    android.util.Log.e("FirebaseManager", "Error parseando canción: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("FirebaseManager", "Error obteniendo canciones del usuario: ${e.message}")
            emptyList()
        }
    }
    
    // 🚀 OPTIMIZACIÓN 3: DISPATCHERS.IO - Obtener videos y fotos de las canciones del usuario
    suspend fun getUserSongMedia(userId: String): List<String> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        return@withContext try {
            val snapshot = firestore.collection("songs")
                .whereEqualTo("artistId", userId)
                .get()
                .await()

            val mediaUrls = mutableListOf<String>()
            
            snapshot.documents.forEach { doc ->
                try {
                    val data = doc.data ?: return@forEach
                    
                    // Agregar video si existe
                    val videoUrl = data["videoUrl"] as? String
                    if (!videoUrl.isNullOrEmpty()) {
                        mediaUrls.add(videoUrl)
                    }
                    
                    // Agregar imagen si existe
                    val imageUrl = data["imageUrl"] as? String
                    if (!imageUrl.isNullOrEmpty()) {
                        mediaUrls.add(imageUrl)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("FirebaseManager", "Error obteniendo media: ${e.message}")
                }
            }
            
            android.util.Log.d("FirebaseManager", "✅ Media obtenida: ${mediaUrls.size} archivos")
            mediaUrls
        } catch (e: Exception) {
            android.util.Log.e("FirebaseManager", "Error obteniendo media del usuario: ${e.message}")
            emptyList()
        }
    }
    
    // Marcar canción como rechazada por el usuario
    suspend fun markSongAsRejected(userId: String, songId: String) {
        try {
            firestore.collection("users")
                .document(userId)
                .collection("rejectedSongs")
                .document(songId)
                .set(hashMapOf("timestamp" to System.currentTimeMillis()))
                .await()
            
            android.util.Log.d("FirebaseManager", "✅ Canción $songId marcada como rechazada para usuario $userId")
        } catch (e: Exception) {
            android.util.Log.e("FirebaseManager", "Error marcando canción como rechazada: ${e.message}")
        }
    }
    
    // 🚀 OPTIMIZACIÓN 2: PAGINACIÓN - Obtener canciones para el feed "Descubre" con límite
    suspend fun getDiscoverSongs(
        userId: String, 
        songLikesManager: SongLikesManager,
        limit: Long = 10,
        lastSongId: String? = null
    ): List<ArtistCard> = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
        return@withContext try {
            android.util.Log.d("FirebaseManager", "🔍 getDiscoverSongs - userId: $userId, límite: $limit")
            
            // Obtener canciones con paginación
            val allSongs = getAllSongs(limit = limit * 3, lastSongId = lastSongId) // Cargar más para compensar filtros
            android.util.Log.d("FirebaseManager", "📊 Canciones obtenidas: ${allSongs.size}")
            
            // Obtener canciones que le gustaron
            val likedSongIds = songLikesManager.getUserLikedSongs(userId)
            android.util.Log.d("FirebaseManager", "❤️ Canciones con like: ${likedSongIds.size}")
            
            // Obtener canciones rechazadas
            val rejectedSnapshot = firestore.collection("users")
                .document(userId)
                .collection("rejectedSongs")
                .get()
                .await()
            val rejectedSongIds = rejectedSnapshot.documents.map { it.id }.toSet()
            android.util.Log.d("FirebaseManager", "🚫 Canciones rechazadas: ${rejectedSongIds.size}")
            
            // Filtrar: excluir canciones propias, con like, y rechazadas
            val filtered = allSongs.filter { song ->
                val isOwn = song.userId == userId
                val isLiked = song.id in likedSongIds
                val isRejected = song.id in rejectedSongIds
                
                if (isOwn) android.util.Log.d("FirebaseManager", "⏭️ Saltando canción propia: ${song.name}")
                if (isLiked) android.util.Log.d("FirebaseManager", "⏭️ Saltando canción con like: ${song.name}")
                if (isRejected) android.util.Log.d("FirebaseManager", "⏭️ Saltando canción rechazada: ${song.name}")
                
                !isOwn && !isLiked && !isRejected
            }
            
            android.util.Log.d("FirebaseManager", "✅ Canciones filtradas para mostrar: ${filtered.size}")
            filtered
        } catch (e: Exception) {
            android.util.Log.e("FirebaseManager", "❌ Error obteniendo canciones para Descubre: ${e.message}", e)
            emptyList()
        }
    }

    // Guardar perfil de usuario
    suspend fun saveUserProfile(userId: String, username: String, isArtist: Boolean) {
        try {
            val userProfile = hashMapOf(
                "username" to username,
                "isArtist" to isArtist,
                "createdAt" to System.currentTimeMillis(),
                "updatedAt" to System.currentTimeMillis()
            )
            firestore.collection("users").document(userId)
                .set(userProfile)
                .await()
        } catch (e: Exception) {
            throw Exception("Error al guardar perfil: ${e.message}")
        }
    }

    // Obtener perfil de usuario
    suspend fun getUserProfile(userId: String): UserProfile? {
        return try {
            val doc = firestore.collection("users").document(userId).get().await()
            if (doc.exists()) {
                UserProfile(
                    userId = doc.id,
                    username = doc.getString("username") ?: "",
                    isArtist = doc.getBoolean("isArtist") ?: false,
                    bio = doc.getString("bio") ?: "",
                    profileImageUrl = doc.getString("profileImageUrl") ?: "",
                    coverImageUrl = doc.getString("coverImageUrl") ?: "",
                    galleryPhotos = (doc.get("galleryPhotos") as? List<String>) ?: emptyList(),
                    galleryVideos = (doc.get("galleryVideos") as? List<String>) ?: emptyList(),
                    socialLinks = (doc.get("socialLinks") as? Map<String, String>) ?: emptyMap(),
                    followers = (doc.getLong("followers") ?: 0).toInt(),
                    following = (doc.getLong("following") ?: 0).toInt(),
                    totalPlays = doc.getLong("totalPlays") ?: 0L,
                    totalSongs = (doc.getLong("totalSongs") ?: 0).toInt(),
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
                )
            } else {
                null
            }
        } catch (e: Exception) {
            android.util.Log.e("FirebaseManager", "Error obteniendo perfil: ${e.message}")
            null
        }
    }

    // Actualizar nombre de usuario
    suspend fun updateUsername(userId: String, username: String) {
        try {
            firestore.collection("users").document(userId)
                .update(
                    "username", username,
                    "updatedAt", System.currentTimeMillis()
                )
                .await()
        } catch (e: Exception) {
            throw Exception("Error al actualizar nombre: ${e.message}")
        }
    }

    // Actualizar rol de usuario
    suspend fun updateUserRole(userId: String, isArtist: Boolean) {
        try {
            firestore.collection("users").document(userId)
                .update(
                    "isArtist", isArtist,
                    "updatedAt", System.currentTimeMillis()
                )
                .await()
        } catch (e: Exception) {
            throw Exception("Error al actualizar rol: ${e.message}")
        }
    }

    // Subir imagen de perfil o portada
    suspend fun uploadProfileImage(uri: Uri, userId: String, type: String, onProgress: (Int) -> Unit): String {
        val fileName = if (type == "profile") {
            "profile_images/$userId.jpg"
        } else {
            "cover_images/$userId.jpg"
        }
        val storageRef = storage.reference.child(fileName)

        return try {
            val uploadTask = storageRef.putFile(uri)

            // Monitorear progreso
            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                onProgress(progress)
            }

            uploadTask.await()

            // Obtener URL de descarga
            storageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            throw Exception("Error al subir imagen: ${e.message}")
        }
    }

    // Actualizar URL de imagen de perfil
    suspend fun updateProfileImage(userId: String, imageUrl: String) {
        try {
            firestore.collection("users").document(userId)
                .update(
                    "profileImageUrl", imageUrl,
                    "updatedAt", System.currentTimeMillis()
                )
                .await()
        } catch (e: Exception) {
            throw Exception("Error al actualizar imagen de perfil: ${e.message}")
        }
    }

    // Actualizar URL de imagen de portada
    suspend fun updateCoverImage(userId: String, imageUrl: String) {
        try {
            firestore.collection("users").document(userId)
                .update(
                    "coverImageUrl", imageUrl,
                    "updatedAt", System.currentTimeMillis()
                )
                .await()
        } catch (e: Exception) {
            throw Exception("Error al actualizar imagen de portada: ${e.message}")
        }
    }

    // ============ FUNCIONES DE COMENTARIOS ============
    
    // Agregar comentario a una canción
    suspend fun addComment(songId: String, userId: String, username: String, text: String, isVoiceNote: Boolean = false): String {
        return try {
            val commentData = hashMapOf(
                "userId" to userId,
                "username" to username,
                "text" to text,
                "timestamp" to System.currentTimeMillis(),
                "likes" to 0,
                "isVoiceNote" to isVoiceNote,
                "songId" to songId
            )
            
            val docRef = firestore.collection("songs")
                .document(songId)
                .collection("comments")
                .add(commentData)
                .await()
            
            docRef.id
        } catch (e: Exception) {
            throw Exception("Error al agregar comentario: ${e.message}")
        }
    }
    
    // Agregar respuesta a un comentario
    suspend fun addReply(songId: String, commentId: String, userId: String, username: String, text: String, isVoiceNote: Boolean = false): String {
        return try {
            val replyData = hashMapOf(
                "userId" to userId,
                "username" to username,
                "text" to text,
                "timestamp" to System.currentTimeMillis(),
                "likes" to 0,
                "isVoiceNote" to isVoiceNote
            )
            
            val docRef = firestore.collection("songs")
                .document(songId)
                .collection("comments")
                .document(commentId)
                .collection("replies")
                .add(replyData)
                .await()
            
            docRef.id
        } catch (e: Exception) {
            throw Exception("Error al agregar respuesta: ${e.message}")
        }
    }
    
    // Obtener comentarios de una canción con listener en tiempo real
    fun getCommentsRealtime(songId: String, onCommentsUpdate: (List<VideoComment>) -> Unit) {
        firestore.collection("songs")
            .document(songId)
            .collection("comments")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onCommentsUpdate(emptyList())
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    val comments = snapshot.documents.mapNotNull { doc ->
                        try {
                            VideoComment(
                                id = doc.id,
                                username = doc.getString("username") ?: "",
                                text = doc.getString("text") ?: "",
                                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                                likes = (doc.getLong("likes") ?: 0).toInt(),
                                isLiked = false, // Se actualizará con los datos del usuario
                                replies = emptyList(), // Se cargarán por separado
                                isVoiceNote = doc.getBoolean("isVoiceNote") ?: false
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    onCommentsUpdate(comments)
                }
            }
    }
    
    // Obtener respuestas de un comentario
    suspend fun getReplies(songId: String, commentId: String): List<VideoComment> {
        return try {
            val snapshot = firestore.collection("songs")
                .document(songId)
                .collection("comments")
                .document(commentId)
                .collection("replies")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.ASCENDING)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                try {
                    VideoComment(
                        id = doc.id,
                        username = doc.getString("username") ?: "",
                        text = doc.getString("text") ?: "",
                        timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                        likes = (doc.getLong("likes") ?: 0).toInt(),
                        isLiked = false,
                        replies = emptyList(),
                        isVoiceNote = doc.getBoolean("isVoiceNote") ?: false
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    // Dar/quitar like a un comentario
    suspend fun toggleCommentLike(songId: String, commentId: String, userId: String, isReply: Boolean = false, parentCommentId: String? = null) {
        try {
            val likeRef = if (isReply && parentCommentId != null) {
                firestore.collection("songs")
                    .document(songId)
                    .collection("comments")
                    .document(parentCommentId)
                    .collection("replies")
                    .document(commentId)
                    .collection("likes")
                    .document(userId)
            } else {
                firestore.collection("songs")
                    .document(songId)
                    .collection("comments")
                    .document(commentId)
                    .collection("likes")
                    .document(userId)
            }
            
            val likeDoc = likeRef.get().await()
            
            val commentRef = if (isReply && parentCommentId != null) {
                firestore.collection("songs")
                    .document(songId)
                    .collection("comments")
                    .document(parentCommentId)
                    .collection("replies")
                    .document(commentId)
            } else {
                firestore.collection("songs")
                    .document(songId)
                    .collection("comments")
                    .document(commentId)
            }
            
            if (likeDoc.exists()) {
                // Quitar like
                likeRef.delete().await()
                commentRef.update("likes", com.google.firebase.firestore.FieldValue.increment(-1)).await()
            } else {
                // Dar like
                likeRef.set(hashMapOf("timestamp" to System.currentTimeMillis())).await()
                commentRef.update("likes", com.google.firebase.firestore.FieldValue.increment(1)).await()
            }
        } catch (e: Exception) {
            throw Exception("Error al dar like: ${e.message}")
        }
    }
    
    // Verificar si el usuario dio like a un comentario
    suspend fun hasUserLikedComment(songId: String, commentId: String, userId: String, isReply: Boolean = false, parentCommentId: String? = null): Boolean {
        return try {
            val likeRef = if (isReply && parentCommentId != null) {
                firestore.collection("songs")
                    .document(songId)
                    .collection("comments")
                    .document(parentCommentId)
                    .collection("replies")
                    .document(commentId)
                    .collection("likes")
                    .document(userId)
            } else {
                firestore.collection("songs")
                    .document(songId)
                    .collection("comments")
                    .document(commentId)
                    .collection("likes")
                    .document(userId)
            }
            
            likeRef.get().await().exists()
        } catch (e: Exception) {
            false
        }
    }

    // ============ FUNCIONES DE PERFIL EXTENDIDO ============
    
    // Actualizar perfil completo del usuario
    suspend fun updateUserProfile(
        userId: String,
        bio: String? = null,
        galleryPhotos: List<String>? = null,
        galleryVideos: List<String>? = null,
        socialLinks: Map<String, String>? = null
    ) {
        try {
            val updates = mutableMapOf<String, Any>(
                "updatedAt" to System.currentTimeMillis()
            )
            
            bio?.let { updates["bio"] = it }
            galleryPhotos?.let { updates["galleryPhotos"] = it }
            galleryVideos?.let { updates["galleryVideos"] = it }
            socialLinks?.let { updates["socialLinks"] = it }
            
            firestore.collection("users").document(userId)
                .update(updates)
                .await()
        } catch (e: Exception) {
            throw Exception("Error al actualizar perfil: ${e.message}")
        }
    }
    
    // Obtener perfil completo del usuario
    suspend fun getFullUserProfile(userId: String): UserProfile? {
        return try {
            val doc = firestore.collection("users").document(userId).get().await()
            if (doc.exists()) {
                UserProfile(
                    userId = userId,
                    username = doc.getString("username") ?: "",
                    isArtist = doc.getBoolean("isArtist") ?: false,
                    bio = doc.getString("bio") ?: "",
                    profileImageUrl = doc.getString("profileImageUrl") ?: "",
                    coverImageUrl = doc.getString("coverImageUrl") ?: "",
                    galleryPhotos = (doc.get("galleryPhotos") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                    galleryVideos = (doc.get("galleryVideos") as? List<*>)?.mapNotNull { it as? String } ?: emptyList(),
                    socialLinks = (doc.get("socialLinks") as? Map<*, *>)?.mapNotNull { (k, v) -> 
                        if (k is String && v is String) k to v else null 
                    }?.toMap() ?: emptyMap(),
                    followers = (doc.getLong("followers") ?: 0).toInt(),
                    following = (doc.getLong("following") ?: 0).toInt(),
                    totalPlays = doc.getLong("totalPlays") ?: 0,
                    totalSongs = (doc.getLong("totalSongs") ?: 0).toInt(),
                    createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis(),
                    updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
    
    // Seguir/dejar de seguir a un usuario
    suspend fun toggleFollow(currentUserId: String, targetUserId: String): Boolean {
        return try {
            val followRef = firestore.collection("users")
                .document(currentUserId)
                .collection("following")
                .document(targetUserId)
            
            val followDoc = followRef.get().await()
            
            if (followDoc.exists()) {
                // Dejar de seguir
                followRef.delete().await()
                
                // Actualizar contador de following
                firestore.collection("users").document(currentUserId)
                    .update("following", com.google.firebase.firestore.FieldValue.increment(-1))
                    .await()
                
                // Actualizar contador de followers del target
                firestore.collection("users").document(targetUserId)
                    .update("followers", com.google.firebase.firestore.FieldValue.increment(-1))
                    .await()
                
                // Eliminar de la colección de followers del target
                firestore.collection("users")
                    .document(targetUserId)
                    .collection("followers")
                    .document(currentUserId)
                    .delete()
                    .await()
                
                false // Ya no sigue
            } else {
                // Seguir
                followRef.set(hashMapOf("timestamp" to System.currentTimeMillis())).await()
                
                // Actualizar contador de following
                firestore.collection("users").document(currentUserId)
                    .update("following", com.google.firebase.firestore.FieldValue.increment(1))
                    .await()
                
                // Actualizar contador de followers del target
                firestore.collection("users").document(targetUserId)
                    .update("followers", com.google.firebase.firestore.FieldValue.increment(1))
                    .await()
                
                // Agregar a la colección de followers del target
                firestore.collection("users")
                    .document(targetUserId)
                    .collection("followers")
                    .document(currentUserId)
                    .set(hashMapOf("timestamp" to System.currentTimeMillis()))
                    .await()
                
                true // Ahora sigue
            }
        } catch (e: Exception) {
            throw Exception("Error al seguir/dejar de seguir: ${e.message}")
        }
    }
    
    // Verificar si un usuario sigue a otro
    suspend fun isFollowing(currentUserId: String, targetUserId: String): Boolean {
        return try {
            firestore.collection("users")
                .document(currentUserId)
                .collection("following")
                .document(targetUserId)
                .get()
                .await()
                .exists()
        } catch (e: Exception) {
            false
        }
    }
    
    // Obtener estadísticas del artista
    suspend fun getArtistStats(userId: String): ArtistStats {
        return try {
            val userDoc = firestore.collection("users").document(userId).get().await()
            val followers = (userDoc.getLong("followers") ?: 0).toInt()
            val totalSongs = (userDoc.getLong("totalSongs") ?: 0).toInt()
            val totalPlays = userDoc.getLong("totalPlays") ?: 0
            
            ArtistStats(
                followers = formatNumber(followers),
                songs = totalSongs,
                plays = formatNumber(totalPlays.toInt())
            )
        } catch (e: Exception) {
            ArtistStats("0", 0, "0")
        }
    }
    
    // Formatear números (1000 -> 1K, 1000000 -> 1M)
    private fun formatNumber(num: Int): String {
        return when {
            num >= 1_000_000 -> String.format("%.1fM", num / 1_000_000.0)
            num >= 1_000 -> String.format("%.1fK", num / 1_000.0)
            else -> num.toString()
        }
    }
    
    // Subir foto/video a la galería
    suspend fun uploadGalleryMedia(uri: android.net.Uri, userId: String, isVideo: Boolean, onProgress: (Int) -> Unit): String {
        val extension = if (isVideo) "mp4" else "jpg"
        val folder = if (isVideo) "gallery_videos" else "gallery_photos"
        val fileName = "$folder/$userId/${java.util.UUID.randomUUID()}.$extension"
        val storageRef = storage.reference.child(fileName)

        return try {
            val uploadTask = storageRef.putFile(uri)

            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                onProgress(progress)
            }

            uploadTask.await()
            storageRef.downloadUrl.await().toString()
        } catch (e: Exception) {
            throw Exception("Error al subir archivo: ${e.message}")
        }
    }

    // ============ FUNCIONES DE CONCURSOS Y VIDEOS ============
    // (Las funciones principales de videos están al final del archivo con logs mejorados)
    
    // Incrementar vistas de video de concurso
    suspend fun incrementVideoViews(entryId: String) {
        try {
            firestore.collection("contest_entries").document(entryId)
                .update("views", com.google.firebase.firestore.FieldValue.increment(1))
                .await()
        } catch (e: Exception) {
            // Ignorar errores
        }
    }
    
    // Dar/quitar like a video de concurso
    suspend fun toggleVideoLike(entryId: String, userId: String): Boolean {
        return try {
            val likeRef = firestore.collection("contest_entries")
                .document(entryId)
                .collection("likes")
                .document(userId)
            
            val likeDoc = likeRef.get().await()
            
            if (likeDoc.exists()) {
                // Quitar like
                likeRef.delete().await()
                firestore.collection("contest_entries").document(entryId)
                    .update("likes", com.google.firebase.firestore.FieldValue.increment(-1))
                    .await()
                false
            } else {
                // Dar like
                likeRef.set(hashMapOf("timestamp" to System.currentTimeMillis())).await()
                firestore.collection("contest_entries").document(entryId)
                    .update("likes", com.google.firebase.firestore.FieldValue.increment(1))
                    .await()
                true
            }
        } catch (e: Exception) {
            false
        }
    }
    
    // Verificar si el usuario dio like a un video
    suspend fun hasUserLikedVideo(entryId: String, userId: String): Boolean {
        return try {
            firestore.collection("contest_entries")
                .document(entryId)
                .collection("likes")
                .document(userId)
                .get()
                .await()
                .exists()
        } catch (e: Exception) {
            false
        }
    }

    // ============ FUNCIONES DE ESTADOS/STORIES ============
    
    // Subir un estado
    suspend fun uploadStory(
        artistId: String,
        artistName: String,
        artistImageUrl: String,
        mediaUri: android.net.Uri,
        mediaType: String,
        caption: String,
        onProgress: (Int) -> Unit
    ): String {
        return try {
            android.util.Log.d("UPLOAD_STORY", "🚀 ===== INICIANDO SUBIDA DE HISTORIA =====")
            android.util.Log.d("UPLOAD_STORY", "👤 artistId: $artistId")
            android.util.Log.d("UPLOAD_STORY", "📝 artistName: $artistName")
            android.util.Log.d("UPLOAD_STORY", "🖼️ artistImageUrl: $artistImageUrl")
            android.util.Log.d("UPLOAD_STORY", "📎 mediaUri: $mediaUri")
            android.util.Log.d("UPLOAD_STORY", "🎬 mediaType: $mediaType")
            android.util.Log.d("UPLOAD_STORY", "💬 caption: $caption")
            
            // Subir el archivo
            android.util.Log.d("UPLOAD_STORY", "📤 Paso 1: Subiendo archivo a Storage...")
            val mediaUrl = if (mediaType == "video") {
                uploadContestVideo(mediaUri, artistId, onProgress)
            } else {
                uploadImageFile(mediaUri, onProgress)
            }
            android.util.Log.d("UPLOAD_STORY", "✅ Archivo subido exitosamente")
            android.util.Log.d("UPLOAD_STORY", "🔗 URL del archivo: $mediaUrl")
            
            // Crear el documento del estado
            val timestamp = System.currentTimeMillis()
            val expiresAt = timestamp + 86400000 // 24 horas
            
            android.util.Log.d("UPLOAD_STORY", "📅 Paso 2: Creando documento...")
            android.util.Log.d("UPLOAD_STORY", "⏰ timestamp: $timestamp")
            android.util.Log.d("UPLOAD_STORY", "⏰ expiresAt: $expiresAt")
            
            val story = hashMapOf(
                "artistId" to artistId,
                "artistName" to artistName,
                "artistImageUrl" to artistImageUrl,
                "mediaUrl" to mediaUrl,
                "mediaType" to mediaType,
                "caption" to caption,
                "timestamp" to timestamp,
                "expiresAt" to expiresAt,
                "views" to 0,
                "isHighlighted" to false
            )
            
            android.util.Log.d("UPLOAD_STORY", "💾 Paso 3: Guardando en Firestore...")
            android.util.Log.d("UPLOAD_STORY", "📊 Documento completo: $story")
            
            val docRef = firestore.collection("stories").add(story).await()
            
            android.util.Log.d("UPLOAD_STORY", "✅ ===== HISTORIA GUARDADA EXITOSAMENTE =====")
            android.util.Log.d("UPLOAD_STORY", "🆔 ID del documento: ${docRef.id}")
            android.util.Log.d("UPLOAD_STORY", "📍 Ruta: stories/${docRef.id}")
            android.util.Log.d("UPLOAD_STORY", "⏰ Expira: ${java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault()).format(java.util.Date(expiresAt))}")
            
            docRef.id
        } catch (e: Exception) {
            android.util.Log.e("UPLOAD_STORY", "❌ ===== ERROR EN SUBIDA =====")
            android.util.Log.e("UPLOAD_STORY", "❌ Mensaje: ${e.message}")
            android.util.Log.e("UPLOAD_STORY", "❌ Tipo: ${e.javaClass.simpleName}")
            android.util.Log.e("UPLOAD_STORY", "❌ Stack trace:", e)
            throw Exception("Error al subir historia: ${e.message}")
        }
    }
    
    // Obtener historias antiguas de un usuario específico (DEPRECATED - usar getUserStories nuevo)
    suspend fun getUserStoriesOld(userId: String): List<Story> {
        return try {
            val currentTime = System.currentTimeMillis()
            
            android.util.Log.d("FirebaseManager", "🔍 Buscando historias para userId: $userId")
            android.util.Log.d("FirebaseManager", "⏰ Tiempo actual: $currentTime")
            
            // Query simplificado sin orderBy múltiple para evitar problemas de índices
            val snapshot = firestore.collection("stories")
                .whereEqualTo("artistId", userId)
                .get()
                .await()
            
            android.util.Log.d("FirebaseManager", "📦 Documentos encontrados: ${snapshot.documents.size}")
            
            val stories = snapshot.documents.mapNotNull { doc ->
                try {
                    val expiresAt = doc.getLong("expiresAt") ?: 0L
                    android.util.Log.d("FirebaseManager", "  📄 Doc ${doc.id}: expiresAt=$expiresAt, expired=${expiresAt <= currentTime}")
                    
                    // Filtrar manualmente las historias expiradas
                    if (expiresAt > currentTime) {
                        Story(
                            id = doc.id,
                            userId = doc.getString("artistId") ?: "",
                            username = doc.getString("artistName") ?: "",
                            imageUrl = doc.getString("mediaUrl") ?: "",
                            videoUrl = if (doc.getString("mediaType") == "video") doc.getString("mediaUrl") ?: "" else "",
                            timestamp = doc.getLong("timestamp") ?: 0L,
                            isHighlighted = false
                        )
                    } else {
                        null
                    }
                } catch (e: Exception) {
                    android.util.Log.e("FirebaseManager", "❌ Error parseando historia: ${e.message}")
                    null
                }
            }.sortedByDescending { it.timestamp } // Ordenar por timestamp en memoria
            
            android.util.Log.d("FirebaseManager", "✅ Historias válidas: ${stories.size}")
            stories
        } catch (e: Exception) {
            android.util.Log.e("FirebaseManager", "❌ Error obteniendo historias del usuario: ${e.message}", e)
            emptyList()
        }
    }
    
    // Obtener estados de artistas que sigue el usuario
    suspend fun getStoriesFromFollowing(userId: String): List<ArtistStory> {
        return try {
            // Obtener lista de artistas que sigue
            val followingSnapshot = firestore.collection("users")
                .document(userId)
                .collection("following")
                .get()
                .await()
            
            val followingIds = followingSnapshot.documents.map { it.id }
            
            if (followingIds.isEmpty()) {
                return emptyList()
            }
            
            // Obtener estados de esos artistas (no expirados)
            val now = System.currentTimeMillis()
            val storiesSnapshot = firestore.collection("stories")
                .whereIn("artistId", followingIds.take(10)) // Firestore limita a 10 en whereIn
                .whereGreaterThan("expiresAt", now)
                .orderBy("expiresAt")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            // Verificar qué estados ya vio el usuario
            val viewedStories = getViewedStories(userId)
            
            storiesSnapshot.documents.mapNotNull { doc ->
                try {
                    ArtistStory(
                        id = doc.id,
                        artistId = doc.getString("artistId") ?: "",
                        artistName = doc.getString("artistName") ?: "",
                        artistImageUrl = doc.getString("artistImageUrl") ?: "",
                        mediaUrl = doc.getString("mediaUrl") ?: "",
                        mediaType = doc.getString("mediaType") ?: "image",
                        caption = doc.getString("caption") ?: "",
                        timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                        expiresAt = doc.getLong("expiresAt") ?: System.currentTimeMillis(),
                        views = (doc.getLong("views") ?: 0).toInt(),
                        isViewed = viewedStories.contains(doc.id)
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("FirebaseManager", "Error obteniendo estados: ${e.message}")
            emptyList()
        }
    }
    
    // Obtener estados de artistas a los que el usuario dio like
    suspend fun getStoriesFromLikedArtists(userId: String, songLikesManager: SongLikesManager): List<ArtistStory> {
        return try {
            // Obtener canciones que le gustaron al usuario
            val likedSongIds = songLikesManager.getUserLikedSongs(userId)
            
            if (likedSongIds.isEmpty()) {
                return emptyList()
            }
            
            // Obtener los IDs de los artistas de esas canciones
            val artistIds = mutableSetOf<String>()
            for (songId in likedSongIds) {
                try {
                    val songDoc = firestore.collection("songs").document(songId).get().await()
                    val artistId = songDoc.getString("artistId")
                    if (!artistId.isNullOrEmpty()) {
                        artistIds.add(artistId)
                    }
                } catch (e: Exception) {
                    android.util.Log.e("FirebaseManager", "Error obteniendo artista de canción $songId: ${e.message}")
                }
            }
            
            if (artistIds.isEmpty()) {
                return emptyList()
            }
            
            // Obtener estados de esos artistas (no expirados)
            val now = System.currentTimeMillis()
            val allStories = mutableListOf<ArtistStory>()
            
            // Firestore limita whereIn a 10 elementos, así que dividimos en grupos
            artistIds.chunked(10).forEach { artistIdsBatch ->
                try {
                    val storiesSnapshot = firestore.collection("stories")
                        .whereIn("artistId", artistIdsBatch)
                        .whereGreaterThan("expiresAt", now)
                        .orderBy("expiresAt")
                        .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                        .get()
                        .await()
                    
                    // Verificar qué estados ya vio el usuario
                    val viewedStories = getViewedStories(userId)
                    
                    val stories = storiesSnapshot.documents.mapNotNull { doc ->
                        try {
                            ArtistStory(
                                id = doc.id,
                                artistId = doc.getString("artistId") ?: "",
                                artistName = doc.getString("artistName") ?: "",
                                artistImageUrl = doc.getString("artistImageUrl") ?: "",
                                mediaUrl = doc.getString("mediaUrl") ?: "",
                                mediaType = doc.getString("mediaType") ?: "image",
                                caption = doc.getString("caption") ?: "",
                                timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis(),
                                expiresAt = doc.getLong("expiresAt") ?: System.currentTimeMillis(),
                                views = (doc.getLong("views") ?: 0).toInt(),
                                isViewed = viewedStories.contains(doc.id)
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    
                    allStories.addAll(stories)
                } catch (e: Exception) {
                    android.util.Log.e("FirebaseManager", "Error obteniendo estados del batch: ${e.message}")
                }
            }
            
            // Ordenar por timestamp (más recientes primero) y poner no vistos primero
            allStories.sortedWith(compareBy<ArtistStory> { it.isViewed }.thenByDescending { it.timestamp })
        } catch (e: Exception) {
            android.util.Log.e("FirebaseManager", "Error obteniendo estados de artistas con like: ${e.message}")
            emptyList()
        }
    }
    
    // Marcar estado como visto
    suspend fun markStoryAsViewed(storyId: String, userId: String) {
        try {
            // Agregar a la colección de vistos del usuario
            firestore.collection("users")
                .document(userId)
                .collection("viewedStories")
                .document(storyId)
                .set(hashMapOf("timestamp" to System.currentTimeMillis()))
                .await()
            
            // Incrementar contador de vistas del estado
            firestore.collection("stories")
                .document(storyId)
                .update("views", com.google.firebase.firestore.FieldValue.increment(1))
                .await()
        } catch (e: Exception) {
            android.util.Log.e("FirebaseManager", "Error marcando estado como visto: ${e.message}")
        }
    }
    
    // Obtener estados que el usuario ya vio
    private suspend fun getViewedStories(userId: String): Set<String> {
        return try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("viewedStories")
                .get()
                .await()
            
            snapshot.documents.map { it.id }.toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }
    
    // ============================================
    // FUNCIONES PARA VIDEOS DE CONCURSOS
    // ============================================
    
    // Subir video a Firebase Storage
    suspend fun uploadContestVideo(uri: Uri, userId: String, onProgress: (Int) -> Unit): String {
        val fileName = "contest_videos/${userId}/${UUID.randomUUID()}.mp4"
        val storageRef = storage.reference.child(fileName)

        return try {
            android.util.Log.d("FirebaseManager", "📤 Iniciando subida de video: $fileName")
            
            val uploadTask = storageRef.putFile(uri)

            // Monitorear progreso
            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                onProgress(progress)
                android.util.Log.d("FirebaseManager", "📊 Progreso de subida: $progress%")
            }

            uploadTask.await()
            android.util.Log.d("FirebaseManager", "✅ Video subido exitosamente")

            // Obtener URL de descarga
            val downloadUrl = storageRef.downloadUrl.await().toString()
            android.util.Log.d("FirebaseManager", "🔗 URL de descarga: $downloadUrl")
            downloadUrl
        } catch (e: Exception) {
            android.util.Log.e("FirebaseManager", "❌ Error al subir video: ${e.message}", e)
            throw Exception("Error al subir video: ${e.message}")
        }
    }
    
    // Crear entrada de concurso
    suspend fun createContestEntry(
        userId: String,
        username: String,
        videoUrl: String,
        title: String,
        contestId: String,
        description: String = "",
        profilePictureUrl: String = ""
    ): String {
        return try {
            android.util.Log.d("FirebaseManager", "📝 Creando entrada de concurso...")
            android.util.Log.d("FirebaseManager", "  👤 Usuario: $username ($userId)")
            android.util.Log.d("FirebaseManager", "  🎬 Video: $videoUrl")
            android.util.Log.d("FirebaseManager", "  📝 Título: $title")
            android.util.Log.d("FirebaseManager", "  🏆 Concurso: $contestId")
            
            val entryData = hashMapOf(
                "userId" to userId,
                "username" to username,
                "profilePictureUrl" to profilePictureUrl,
                "videoUrl" to videoUrl,
                "title" to title,
                "description" to description,
                "contestId" to contestId,
                "likes" to 0,
                "views" to 0,
                "timestamp" to System.currentTimeMillis()
            )
            
            val docRef = firestore.collection("contest_entries")
                .add(entryData)
                .await()
            
            android.util.Log.d("FirebaseManager", "✅ Entrada creada con ID: ${docRef.id}")
            android.util.Log.d("FirebaseManager", "✅ El video ahora aparecerá en el carrusel de Live")
            docRef.id
        } catch (e: Exception) {
            android.util.Log.e("FirebaseManager", "❌ Error al crear entrada: ${e.message}", e)
            throw Exception("Error al crear entrada de concurso: ${e.message}")
        }
    }
    
    // Obtener todas las entradas de concursos (sin duplicados y sin usuarios eliminados)
    suspend fun getAllContestEntries(): List<ContestEntry> {
        return try {
            android.util.Log.d("FirebaseManager", "🔍 Obteniendo videos de concursos desde Firestore...")
            
            val snapshot = firestore.collection("contest_entries")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            android.util.Log.d("FirebaseManager", "📦 Documentos encontrados: ${snapshot.documents.size}")
            
            // Parsear todos los documentos
            val allEntries = snapshot.documents.mapNotNull { doc ->
                try {
                    val videoUrl = doc.getString("videoUrl") ?: ""
                    val userId = doc.getString("userId") ?: ""
                    
                    // Validar que tenga URL de video
                    if (videoUrl.isEmpty()) {
                        android.util.Log.w("FirebaseManager", "⚠️ Entrada sin videoUrl: ${doc.id}")
                        return@mapNotNull null
                    }
                    
                    ContestEntry(
                        id = doc.id,
                        userId = userId,
                        username = doc.getString("username") ?: "Usuario",
                        profilePictureUrl = doc.getString("profilePictureUrl") ?: "",
                        videoUrl = videoUrl,
                        title = doc.getString("title") ?: "Sin título",
                        description = doc.getString("description") ?: "",
                        contestId = doc.getString("contestId") ?: "",
                        likes = (doc.getLong("likes") ?: 0).toInt(),
                        views = (doc.getLong("views") ?: 0).toInt(),
                        timestamp = doc.getLong("timestamp") ?: 0L
                    )
                } catch (e: Exception) {
                    android.util.Log.e("FirebaseManager", "❌ Error parseando entrada ${doc.id}: ${e.message}")
                    null
                }
            }
            
            android.util.Log.d("FirebaseManager", "📊 Videos parseados: ${allEntries.size}")
            
            // 🔥 FILTRAR VIDEOS DE USUARIOS QUE YA NO EXISTEN
            val validEntries = allEntries.filter { entry ->
                if (entry.userId.isEmpty()) {
                    android.util.Log.w("FirebaseManager", "⚠️ Video sin userId: ${entry.title}")
                    return@filter true // Mantener videos sin userId por compatibilidad
                }
                
                // Verificar si el usuario existe
                val userExists = try {
                    val userDoc = firestore.collection("users").document(entry.userId).get().await()
                    userDoc.exists()
                } catch (e: Exception) {
                    android.util.Log.e("FirebaseManager", "❌ Error verificando usuario ${entry.userId}: ${e.message}")
                    true // En caso de error, mantener el video
                }
                
                if (!userExists) {
                    android.util.Log.w("FirebaseManager", "🗑️ Usuario eliminado, removiendo video: ${entry.username} - ${entry.title}")
                }
                
                userExists
            }
            
            android.util.Log.d("FirebaseManager", "✅ Videos de usuarios válidos: ${validEntries.size} (eliminados: ${allEntries.size - validEntries.size})")
            
            // Eliminar duplicados por videoUrl (mantener el más reciente)
            val uniqueEntries = validEntries
                .groupBy { it.videoUrl }
                .map { (videoUrl, entries) ->
                    if (entries.size > 1) {
                        android.util.Log.d("FirebaseManager", "🔄 Duplicados encontrados para video: ${entries.first().title} (${entries.size} copias)")
                    }
                    // Mantener solo el más reciente (ya están ordenados por timestamp DESC)
                    entries.first()
                }
            
            android.util.Log.d("FirebaseManager", "✅ Videos únicos: ${uniqueEntries.size}")
            
            // Opcional: Mezclar para variedad (comentar si quieres mantener orden cronológico)
            val finalEntries = uniqueEntries.shuffled()
            
            // Log de resumen
            android.util.Log.d("FirebaseManager", "📋 Resumen de videos:")
            finalEntries.take(5).forEach { entry ->
                android.util.Log.d("FirebaseManager", "  - ${entry.username}: ${entry.title} (${entry.contestId})")
            }
            if (finalEntries.size > 5) {
                android.util.Log.d("FirebaseManager", "  ... y ${finalEntries.size - 5} videos más")
            }
            
            finalEntries
        } catch (e: Exception) {
            android.util.Log.e("FirebaseManager", "❌ Error obteniendo entradas: ${e.message}", e)
            emptyList()
        }
    }
    
    // Limpiar videos duplicados de Firestore (mantener solo el más reciente por videoUrl)
    suspend fun cleanupDuplicateVideos(): Int {
        return try {
            android.util.Log.d("FirebaseManager", "🧹 Iniciando limpieza de videos duplicados...")
            
            val snapshot = firestore.collection("contest_entries")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            val allEntries = snapshot.documents.mapNotNull { doc ->
                try {
                    Triple(
                        doc.id,
                        doc.getString("videoUrl") ?: "",
                        doc.getLong("timestamp") ?: 0L
                    )
                } catch (e: Exception) {
                    null
                }
            }
            
            // Agrupar por videoUrl
            val grouped = allEntries.groupBy { it.second }
            
            var deletedCount = 0
            
            // Para cada grupo de duplicados
            grouped.forEach { (videoUrl, entries) ->
                if (entries.size > 1) {
                    android.util.Log.d("FirebaseManager", "🔍 Encontrados ${entries.size} duplicados para: $videoUrl")
                    
                    // Mantener el más reciente (primero en la lista), eliminar el resto
                    val toDelete = entries.drop(1)
                    
                    toDelete.forEach { (docId, _, _) ->
                        try {
                            firestore.collection("contest_entries")
                                .document(docId)
                                .delete()
                                .await()
                            deletedCount++
                            android.util.Log.d("FirebaseManager", "🗑️ Eliminado duplicado: $docId")
                        } catch (e: Exception) {
                            android.util.Log.e("FirebaseManager", "❌ Error eliminando $docId: ${e.message}")
                        }
                    }
                }
            }
            
            android.util.Log.d("FirebaseManager", "✅ Limpieza completada: $deletedCount videos duplicados eliminados")
            deletedCount
        } catch (e: Exception) {
            android.util.Log.e("FirebaseManager", "❌ Error en limpieza de duplicados: ${e.message}", e)
            0
        }
    }
    
    // Limpiar videos de usuarios que ya no existen (huérfanos)
    suspend fun cleanupOrphanedVideos(): Int {
        return try {
            android.util.Log.d("FirebaseManager", "🧹 Iniciando limpieza de videos huérfanos...")
            
            val snapshot = firestore.collection("contest_entries").get().await()
            var deletedCount = 0
            
            snapshot.documents.forEach { doc ->
                val userId = doc.getString("userId") ?: ""
                val username = doc.getString("username") ?: "Desconocido"
                
                if (userId.isEmpty()) {
                    android.util.Log.w("FirebaseManager", "⚠️ Video sin userId: ${doc.id}")
                    return@forEach
                }
                
                // Verificar si el usuario existe
                val userExists = try {
                    val userDoc = firestore.collection("users").document(userId).get().await()
                    userDoc.exists()
                } catch (e: Exception) {
                    android.util.Log.e("FirebaseManager", "❌ Error verificando usuario $userId: ${e.message}")
                    true // En caso de error, no eliminar
                }
                
                if (!userExists) {
                    try {
                        firestore.collection("contest_entries").document(doc.id).delete().await()
                        deletedCount++
                        android.util.Log.d("FirebaseManager", "🗑️ Video huérfano eliminado: $username (userId: $userId)")
                    } catch (e: Exception) {
                        android.util.Log.e("FirebaseManager", "❌ Error eliminando video ${doc.id}: ${e.message}")
                    }
                }
            }
            
            android.util.Log.d("FirebaseManager", "✅ Limpieza completada: $deletedCount videos huérfanos eliminados")
            deletedCount
        } catch (e: Exception) {
            android.util.Log.e("FirebaseManager", "❌ Error en limpieza de videos huérfanos: ${e.message}", e)
            0
        }
    }
    
    // ============================================
    // ELIMINAR CUENTA DE USUARIO
    // ============================================
    
    // Eliminar todos los datos del usuario de Firebase
    // Esta función ahora llama a deleteAllUserData que es más completa
    suspend fun deleteUserAccount(userId: String) {
        deleteAllUserData(userId)
    }

    // Generar imagen de portada automática usando plantilla
    suspend fun generateDefaultCoverImage(
        context: android.content.Context,
        userId: String,
        username: String,
        profileImageUrl: String?
    ): android.net.Uri {
        return kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            try {
                // Cargar la plantilla desde drawable
                val templateBitmap = android.graphics.BitmapFactory.decodeResource(
                    context.resources,
                    R.drawable.user_plantilla
                ).copy(android.graphics.Bitmap.Config.ARGB_8888, true)
                
                val canvas = android.graphics.Canvas(templateBitmap)
                val paint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
                
                // Cargar foto de perfil si existe
                if (!profileImageUrl.isNullOrEmpty()) {
                    try {
                        val profileBitmap = coil.ImageLoader(context)
                            .execute(
                                coil.request.ImageRequest.Builder(context)
                                    .data(profileImageUrl)
                                    .build()
                            )
                            .drawable?.let { drawable ->
                                val bitmap = android.graphics.Bitmap.createBitmap(
                                    drawable.intrinsicWidth,
                                    drawable.intrinsicHeight,
                                    android.graphics.Bitmap.Config.ARGB_8888
                                )
                                val tempCanvas = android.graphics.Canvas(bitmap)
                                drawable.setBounds(0, 0, tempCanvas.width, tempCanvas.height)
                                drawable.draw(tempCanvas)
                                bitmap
                            }
                        
                        if (profileBitmap != null) {
                            // Redimensionar y dibujar foto de perfil en círculo
                            val profileSize = (templateBitmap.width * 0.3f).toInt()
                            val scaledProfile = android.graphics.Bitmap.createScaledBitmap(
                                profileBitmap,
                                profileSize,
                                profileSize,
                                true
                            )
                            
                            // Crear máscara circular
                            val circleBitmap = android.graphics.Bitmap.createBitmap(
                                profileSize,
                                profileSize,
                                android.graphics.Bitmap.Config.ARGB_8888
                            )
                            val circleCanvas = android.graphics.Canvas(circleBitmap)
                            val circlePaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG)
                            circlePaint.color = android.graphics.Color.WHITE
                            circleCanvas.drawCircle(
                                profileSize / 2f,
                                profileSize / 2f,
                                profileSize / 2f,
                                circlePaint
                            )
                            circlePaint.xfermode = android.graphics.PorterDuffXfermode(android.graphics.PorterDuff.Mode.SRC_IN)
                            circleCanvas.drawBitmap(scaledProfile, 0f, 0f, circlePaint)
                            
                            // Dibujar en el centro de la plantilla
                            val x = (templateBitmap.width - profileSize) / 2f
                            val y = (templateBitmap.height - profileSize) / 2f
                            canvas.drawBitmap(circleBitmap, x, y, paint)
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("FirebaseManager", "Error cargando foto de perfil: ${e.message}")
                    }
                }
                
                // Dibujar nombre del usuario en la parte superior
                val textPaint = android.graphics.Paint(android.graphics.Paint.ANTI_ALIAS_FLAG).apply {
                    color = android.graphics.Color.WHITE
                    textSize = templateBitmap.width * 0.08f
                    typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
                    textAlign = android.graphics.Paint.Align.CENTER
                    setShadowLayer(8f, 0f, 0f, android.graphics.Color.BLACK)
                }
                
                canvas.drawText(
                    username,
                    templateBitmap.width / 2f,
                    templateBitmap.height * 0.15f,
                    textPaint
                )
                
                // Guardar bitmap en archivo temporal
                val file = java.io.File(context.cacheDir, "cover_${userId}_${System.currentTimeMillis()}.jpg")
                val outputStream = java.io.FileOutputStream(file)
                templateBitmap.compress(android.graphics.Bitmap.CompressFormat.JPEG, 90, outputStream)
                outputStream.flush()
                outputStream.close()
                
                android.util.Log.d("FirebaseManager", "✅ Imagen de portada generada: ${file.absolutePath}")
                
                android.net.Uri.fromFile(file)
            } catch (e: Exception) {
                android.util.Log.e("FirebaseManager", "❌ Error generando imagen: ${e.message}", e)
                throw Exception("Error generando imagen de portada: ${e.message}")
            }
        }
    }

    // ============ FUNCIONES DE HISTORIAS ============
    
    // Crear una nueva historia 
    suspend fun createStory(
        userId: String,
        username: String,
        imageUrl: String = "",
        videoUrl: String = "",
        isHighlighted: Boolean = false
    ): String {
        return try {
            val storyData = hashMapOf(
                "userId" to userId,
                "username" to username,
                "imageUrl" to imageUrl,
                "videoUrl" to videoUrl,
                "timestamp" to System.currentTimeMillis(),
                "isHighlighted" to isHighlighted,
                "expiresAt" to (System.currentTimeMillis() + 86400000) // 24 horas
            )
            
            val docRef = firestore.collection("stories").add(storyData).await()
            android.util.Log.d("FirebaseManager", "✅ Historia creada: ${docRef.id}")
            docRef.id
        } catch (e: Exception) {
            android.util.Log.e("FirebaseManager", "❌ Error creando historia: ${e.message}")
            throw Exception("Error al crear historia: ${e.message}")
        }
    }
    
    // Obtener historias activas de un usuario (últimas 24 horas)
    suspend fun getUserStories(userId: String): List<ArtistStory> {
        return try {
            val now = System.currentTimeMillis()
            android.util.Log.d("HISTORIAS_FIREBASE", "🔍 Buscando historias para: $userId")
            
            // Query simplificado - solo filtra por userId (no requiere índice compuesto)
            val snapshot = firestore.collection("stories")
                .whereEqualTo("artistId", userId)
                .get()
                .await()
            
            android.util.Log.d("HISTORIAS_FIREBASE", "📦 Documentos encontrados: ${snapshot.documents.size}")
            
            // Filtrar manualmente las historias expiradas
            val stories = snapshot.documents.mapNotNull { doc ->
                try {
                    val expiresAt = doc.getLong("expiresAt") ?: 0L
                    
                    // Saltar si expiró
                    if (expiresAt <= now) {
                        android.util.Log.d("HISTORIAS_FIREBASE", "⏭️ Historia expirada: ${doc.id}")
                        return@mapNotNull null
                    }
                    
                    ArtistStory(
                        id = doc.id,
                        artistId = doc.getString("artistId") ?: "",
                        artistName = doc.getString("artistName") ?: "",
                        artistImageUrl = doc.getString("artistImageUrl") ?: "",
                        mediaUrl = doc.getString("mediaUrl") ?: "",
                        mediaType = doc.getString("mediaType") ?: "image",
                        caption = doc.getString("caption") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0L,
                        expiresAt = expiresAt,
                        views = (doc.getLong("views") ?: 0).toInt(),
                        isViewed = false,
                        isHighlighted = doc.getBoolean("isHighlighted") ?: false
                    )
                } catch (e: Exception) {
                    android.util.Log.e("HISTORIAS_FIREBASE", "Error: ${e.message}")
                    null
                }
            }.sortedByDescending { it.timestamp }
            
            android.util.Log.d("HISTORIAS_FIREBASE", "✅ ${stories.size} historias válidas")
            stories
        } catch (e: Exception) {
            android.util.Log.e("HISTORIAS_FIREBASE", "❌ Error obteniendo historias: ${e.message}", e)
            emptyList()
        }
    }
    
    // Obtener historias destacadas de un usuario (permanentes)
    suspend fun getUserHighlightedStories(userId: String): List<Story> {
        return try {
            val snapshot = firestore.collection("stories")
                .whereEqualTo("userId", userId)
                .whereEqualTo("isHighlighted", true)
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            snapshot.documents.mapNotNull { doc ->
                try {
                    Story(
                        id = doc.id,
                        userId = doc.getString("userId") ?: "",
                        username = doc.getString("username") ?: "",
                        imageUrl = doc.getString("imageUrl") ?: "",
                        videoUrl = doc.getString("videoUrl") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0L,
                        isHighlighted = true
                    )
                } catch (e: Exception) {
                    null
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("FirebaseManager", "Error obteniendo historias destacadas: ${e.message}")
            emptyList()
        }
    }
    
    // Marcar/desmarcar historia como destacada
    suspend fun toggleStoryHighlight(storyId: String, isHighlighted: Boolean) {
        try {
            firestore.collection("stories").document(storyId)
                .update(
                    "isHighlighted", isHighlighted,
                    "updatedAt", System.currentTimeMillis()
                )
                .await()
            
            android.util.Log.d("FirebaseManager", "✅ Historia ${if (isHighlighted) "destacada" else "desmarcada"}: $storyId")
        } catch (e: Exception) {
            android.util.Log.e("FirebaseManager", "Error actualizando historia: ${e.message}")
            throw Exception("Error al actualizar historia: ${e.message}")
        }
    }
    
    // Eliminar una historia
    suspend fun deleteStory(storyId: String, userId: String) {
        try {
            android.util.Log.d("FirebaseManager", "🗑️ Eliminando historia: $storyId")
            
            // Eliminar el documento de Firestore
            firestore.collection("stories").document(storyId).delete().await()
            
            // Decrementar contador de historias del usuario
            try {
                firestore.collection("users").document(userId)
                    .update("totalStories", com.google.firebase.firestore.FieldValue.increment(-1))
                    .await()
                android.util.Log.d("FirebaseManager", "✅ Contador decrementado")
            } catch (e: Exception) {
                android.util.Log.w("FirebaseManager", "⚠️ No se pudo decrementar contador: ${e.message}")
            }
            
            android.util.Log.d("FirebaseManager", "✅ Historia eliminada: $storyId")
        } catch (e: Exception) {
            android.util.Log.e("FirebaseManager", "❌ Error eliminando historia: ${e.message}")
            throw Exception("Error al eliminar historia: ${e.message}")
        }
    }
    
    // Verificar si un artista tiene historias activas
    suspend fun artistHasActiveStory(userId: String): Boolean {
        return try {
            val twentyFourHoursAgo = System.currentTimeMillis() - 86400000
            
            val snapshot = firestore.collection("stories")
                .whereEqualTo("userId", userId)
                .whereGreaterThan("timestamp", twentyFourHoursAgo)
                .limit(1)
                .get()
                .await()
            
            !snapshot.isEmpty
        } catch (e: Exception) {
            false
        }
    }
    
    // Limpiar historias expiradas (llamar periódicamente)
    suspend fun cleanupExpiredStories() {
        try {
            val now = System.currentTimeMillis()
            
            val snapshot = firestore.collection("stories")
                .whereEqualTo("isHighlighted", false) // Solo eliminar las no destacadas
                .whereLessThan("expiresAt", now)
                .get()
                .await()
            
            var deletedCount = 0
            snapshot.documents.forEach { doc ->
                try {
                    doc.reference.delete().await()
                    deletedCount++
                } catch (e: Exception) {
                    android.util.Log.e("FirebaseManager", "Error eliminando historia ${doc.id}: ${e.message}")
                }
            }
            
            if (deletedCount > 0) {
                android.util.Log.d("FirebaseManager", "✅ Limpieza completada: $deletedCount historias eliminadas")
            }
        } catch (e: Exception) {
            android.util.Log.e("FirebaseManager", "Error en limpieza de historias: ${e.message}")
        }
    }
    
    // Subir media para historia
    suspend fun uploadStoryMedia(uri: android.net.Uri, userId: String, isVideo: Boolean, onProgress: (Int) -> Unit): String {
        val extension = if (isVideo) "mp4" else "jpg"
        val fileName = "stories/$userId/${java.util.UUID.randomUUID()}.$extension"
        val storageRef = storage.reference.child(fileName)

        return try {
            val uploadTask = storageRef.putFile(uri)

            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                onProgress(progress)
            }

            uploadTask.await()
            val downloadUrl = storageRef.downloadUrl.await().toString()
            android.util.Log.d("FirebaseManager", "✅ Media de historia subido: $downloadUrl")
            downloadUrl
        } catch (e: Exception) {
            android.util.Log.e("FirebaseManager", "❌ Error subiendo media: ${e.message}")
            throw Exception("Error al subir archivo: ${e.message}")
        }
    }

    // ============ FUNCIÓN PARA GUARDAR METADATA DE HISTORIAS ============
    
    /**
     * Guarda la metadata de una nueva historia en la colección 'stories'.
     * @param story La data de la historia (URL, tipo, ID de usuario).
     * @return El ID del documento de la historia.
     */
    suspend fun uploadStoryMetadata(story: ArtistStory): String {
        return try {
            android.util.Log.d("UPLOAD_STORY_METADATA", "🚀 ===== GUARDANDO METADATA DE HISTORIA =====")
            android.util.Log.d("UPLOAD_STORY_METADATA", "� art=istId: ${story.artistId}")
            android.util.Log.d("UPLOAD_STORY_METADATA", "�  artistName: ${story.artistName}")
            android.util.Log.d("UPLOAD_STORY_METADATA", "�  mediaUrl: ${story.mediaUrl}")
            android.util.Log.d("UPLOAD_STORY_METADATA", "🎬 mediaType: ${story.mediaType}")
            
            // Crear el documento con todos los campos necesarios
            val storyData = hashMapOf(
                "artistId" to story.artistId,
                "artistName" to story.artistName,
                "artistImageUrl" to story.artistImageUrl,
                "mediaUrl" to story.mediaUrl,
                "mediaType" to story.mediaType,
                "caption" to story.caption,
                "timestamp" to story.timestamp,
                "expiresAt" to story.expiresAt,
                "views" to story.views,
                "isHighlighted" to story.isHighlighted
            )
            
            android.util.Log.d("UPLOAD_STORY_METADATA", "💾 Guardando en Firestore...")
            android.util.Log.d("UPLOAD_STORY_METADATA", "� Docudmento: $storyData")
            
            // Guardar en la colección 'stories'
            val docRef = firestore.collection("stories").add(storyData).await()
            
            // Opcional: Incrementar contador de historias en el perfil del usuario
            try {
                firestore.collection("users").document(story.artistId)
                    .update("totalStories", com.google.firebase.firestore.FieldValue.increment(1))
                    .await()
                android.util.Log.d("UPLOAD_STORY_METADATA", "✅ Contador de historias incrementado")
            } catch (e: Exception) {
                android.util.Log.w("UPLOAD_STORY_METADATA", "⚠️ No se pudo incrementar contador: ${e.message}")
            }
            
            android.util.Log.d("UPLOAD_STORY_METADATA", "✅ ===== METADATA GUARDADA EXITOSAMENTE =====")
            android.util.Log.d("UPLOAD_STORY_METADATA", "🆔 ID del documento: ${docRef.id}")
            android.util.Log.d("UPLOAD_STORY_METADATA", "📍 Ruta: stories/${docRef.id}")
            
            docRef.id
        } catch (e: Exception) {
            android.util.Log.e("UPLOAD_STORY_METADATA", "❌ ===== ERROR GUARDANDO METADATA =====")
            android.util.Log.e("UPLOAD_STORY_METADATA", "❌ Mensaje: ${e.message}")
            android.util.Log.e("UPLOAD_STORY_METADATA", "❌ Tipo: ${e.javaClass.simpleName}")
            android.util.Log.e("UPLOAD_STORY_METADATA", "❌ Stack trace:", e)
            throw Exception("Error al guardar metadata de historia: ${e.message}")
        }
    }
    
    // ============ FUNCIONES DE INTERACCIÓN CON VIDEOS ============
    
    // Dar/quitar like a un video de concurso
    suspend fun toggleLikeContestVideo(videoId: String, userId: String): Boolean {
        return try {
            val videoRef = firestore.collection("contest_entries").document(videoId)
            val likesRef = videoRef.collection("likes").document(userId)
            
            // Verificar si ya dio like
            val likeDoc = likesRef.get().await()
            
            if (likeDoc.exists()) {
                // Ya dio like, quitarlo
                likesRef.delete().await()
                
                // Decrementar contador
                videoRef.update("likes", com.google.firebase.firestore.FieldValue.increment(-1)).await()
                
                android.util.Log.d("FirebaseManager", "💔 Like removido del video $videoId")
                false // Retorna false = like removido
            } else {
                // No ha dado like, agregarlo
                likesRef.set(mapOf(
                    "userId" to userId,
                    "timestamp" to System.currentTimeMillis()
                )).await()
                
                // Incrementar contador
                videoRef.update("likes", com.google.firebase.firestore.FieldValue.increment(1)).await()
                
                android.util.Log.d("FirebaseManager", "❤️ Like agregado al video $videoId")
                true // Retorna true = like agregado
            }
        } catch (e: Exception) {
            android.util.Log.e("FirebaseManager", "❌ Error al dar like: ${e.message}")
            false
        }
    }
    
    // Agregar comentario a un video
    suspend fun addCommentToVideo(videoId: String, userId: String, username: String, comment: String): String {
        return try {
            val commentData = hashMapOf(
                "userId" to userId,
                "username" to username,
                "text" to comment,  // Usar "text" para consistencia con el modelo existente
                "timestamp" to System.currentTimeMillis()
            )
            
            val commentRef = firestore.collection("contest_entries")
                .document(videoId)
                .collection("comments")
                .add(commentData)
                .await()
            
            android.util.Log.d("FirebaseManager", "💬 Comentario agregado al video $videoId")
            commentRef.id
        } catch (e: Exception) {
            android.util.Log.e("FirebaseManager", "❌ Error al agregar comentario: ${e.message}")
            throw e
        }
    }
    
    // Obtener comentarios de un video
    suspend fun getVideoComments(videoId: String): List<VideoComment> {
        return try {
            android.util.Log.d("FirebaseManager", "📖 Obteniendo comentarios del video: $videoId")
            
            val snapshot = firestore.collection("contest_entries")
                .document(videoId)
                .collection("comments")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            android.util.Log.d("FirebaseManager", "📊 Total de comentarios encontrados: ${snapshot.documents.size}")
            
            snapshot.documents.mapNotNull { doc ->
                try {
                    val userId = doc.getString("userId") ?: ""
                    val username = doc.getString("username") ?: "Usuario"
                    val text = doc.getString("text") ?: ""
                    val timestamp = doc.getLong("timestamp") ?: 0L
                    
                    android.util.Log.d("FirebaseManager", "💬 Comentario ${doc.id}:")
                    android.util.Log.d("FirebaseManager", "  UserId: '$userId'")
                    android.util.Log.d("FirebaseManager", "  Username: '$username'")
                    android.util.Log.d("FirebaseManager", "  Text: '$text'")
                    android.util.Log.d("FirebaseManager", "  Timestamp: $timestamp")
                    
                    VideoComment(
                        id = doc.id,
                        username = username,
                        text = text,
                        timestamp = timestamp
                    )
                } catch (e: Exception) {
                    android.util.Log.e("FirebaseManager", "❌ Error parseando comentario ${doc.id}: ${e.message}")
                    null
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("FirebaseManager", "❌ Error obteniendo comentarios: ${e.message}")
            android.util.Log.e("FirebaseManager", "Stack trace:", e)
            emptyList()
        }
    }
    
    /**
     * Finaliza una sesión de Live activa.
     * Actualiza el documento en Firestore marcándolo como inactivo.
     */
    suspend fun endLiveSession(sessionId: String) {
        try {
            android.util.Log.d("FirebaseManager", "🛑 Finalizando sesión de Live: $sessionId")
            
            val endTime = System.currentTimeMillis()
            
            firestore.collection("live_sessions")
                .document(sessionId)
                .update(
                    mapOf(
                        "isActive" to false,
                        "endTime" to endTime,
                        "updatedAt" to com.google.firebase.Timestamp.now()
                    )
                )
                .await()
            
            android.util.Log.d("FirebaseManager", "✅ Sesión de Live finalizada correctamente")
        } catch (e: Exception) {
            android.util.Log.e("FirebaseManager", "❌ Error al finalizar sesión de Live: ${e.message}", e)
        }
    }
    
    /**
     * Obtiene todas las sesiones de Live activas.
     * Útil para mostrar la lista de Lives disponibles para ver.
     */
    suspend fun getActiveLiveSessions(): List<LiveSession> {
        return try {
            android.util.Log.d("FirebaseManager", "📡 Obteniendo sesiones de Live activas...")
            
            val snapshot = firestore.collection("live_sessions")
                .whereEqualTo("isActive", true)
                .orderBy("startTime", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .await()
            
            val sessions = snapshot.documents.mapNotNull { doc ->
                try {
                    val sessionId = doc.getString("sessionId") ?: doc.id
                    val username = doc.getString("username") ?: "Usuario"
                    val title = doc.getString("title") ?: "Live"
                    
                    android.util.Log.d("FirebaseManager", "  📡 Live encontrado: $username - $title")
                    
                    LiveSession(
                        sessionId = sessionId,
                        userId = doc.getString("userId") ?: "",
                        username = username,
                        profileImageUrl = doc.getString("profileImageUrl") ?: "",
                        title = title,
                        agoraChannelName = doc.getString("agoraChannelName") ?: "",
                        agoraToken = "", // Los viewers necesitarán su propio token
                        startTime = doc.getLong("startTime") ?: 0L,
                        isActive = doc.getBoolean("isActive") ?: false,
                        viewerCount = doc.getLong("viewerCount")?.toInt() ?: 0
                    )
                } catch (e: Exception) {
                    android.util.Log.e("FirebaseManager", "❌ Error parseando sesión: ${e.message}")
                    null
                }
            }
            
            android.util.Log.d("FirebaseManager", "✅ ${sessions.size} sesiones activas encontradas")
            sessions
        } catch (e: Exception) {
            android.util.Log.e("FirebaseManager", "❌ Error obteniendo sesiones activas: ${e.message}", e)
            emptyList()
        }
    }
    
    /**
     * Incrementa el contador de espectadores de una sesión de Live.
     */
    suspend fun incrementLiveViewers(sessionId: String) {
        try {
            firestore.collection("live_sessions")
                .document(sessionId)
                .update("viewerCount", com.google.firebase.firestore.FieldValue.increment(1))
                .await()
            
            android.util.Log.d("FirebaseManager", "👁️ Viewer agregado a sesión: $sessionId")
        } catch (e: Exception) {
            android.util.Log.e("FirebaseManager", "❌ Error incrementando viewers: ${e.message}")
        }
    }
    
    /**
     * Decrementa el contador de espectadores de una sesión de Live.
     */
    suspend fun decrementLiveViewers(sessionId: String) {
        try {
            firestore.collection("live_sessions")
                .document(sessionId)
                .update("viewerCount", com.google.firebase.firestore.FieldValue.increment(-1))
                .await()
            
            android.util.Log.d("FirebaseManager", "👁️ Viewer removido de sesión: $sessionId")
        } catch (e: Exception) {
            android.util.Log.e("FirebaseManager", "❌ Error decrementando viewers: ${e.message}")
        }
    }
    
    /**
     * Inicia una nueva sesión de Live.
     * Genera token de Agora y crea documento en Firestore.
     */
    suspend fun startNewLiveSession(
        userId: String,
        username: String,
        profileImageUrl: String,
        title: String
    ): LiveSession? {
        return try {
            android.util.Log.d("FirebaseManager", "🎬 ===== INICIANDO NUEVA SESIÓN DE LIVE =====")
            android.util.Log.d("FirebaseManager", "👤 Usuario: $username ($userId)")
            android.util.Log.d("FirebaseManager", "📝 Título: $title")
            
            // 1. Generar nombre de canal único
            val channelName = "live_${userId}_${System.currentTimeMillis()}"
            android.util.Log.d("FirebaseManager", "📺 Canal generado: $channelName")
            
            // 2. Llamar a Cloud Function para obtener token de Agora
            android.util.Log.d("FirebaseManager", "🔑 Solicitando token de Agora...")
            val functions = com.google.firebase.functions.FirebaseFunctions.getInstance()
            val data = hashMapOf(
                "channelName" to channelName,
                "role" to "publisher",
                "uid" to 0
            )
            
            val result = functions
                .getHttpsCallable("generateAgoraToken")
                .call(data)
                .await()
            
            val resultData = result.data as? Map<*, *>
            val agoraToken = resultData?.get("token") as? String
            
            if (agoraToken.isNullOrEmpty()) {
                android.util.Log.e("FirebaseManager", "❌ No se recibió token de Agora")
                return null
            }
            
            android.util.Log.d("FirebaseManager", "✅ Token de Agora recibido: ${agoraToken.take(20)}...")
            
            // 3. Crear documento en Firestore
            android.util.Log.d("FirebaseManager", "💾 Creando documento en Firestore...")
            
            // Primero crear el documento para obtener el ID
            val docRef = firestore.collection("live_sessions").document()
            val sessionId = docRef.id
            
            val sessionData = hashMapOf(
                "sessionId" to sessionId,  // ← IMPORTANTE: Guardar el sessionId en el documento
                "userId" to userId,
                "username" to username,
                "profileImageUrl" to profileImageUrl,
                "title" to title,
                "agoraChannelName" to channelName,
                "agoraToken" to agoraToken,
                "startTime" to System.currentTimeMillis(),
                "isActive" to true,
                "viewerCount" to 0
            )
            
            docRef.set(sessionData).await()
            
            android.util.Log.d("FirebaseManager", "✅ Sesión creada en Firestore: $sessionId")
            
            // 4. Retornar objeto LiveSession
            LiveSession(
                sessionId = sessionId,
                userId = userId,
                username = username,
                profileImageUrl = profileImageUrl,
                title = title,
                agoraChannelName = channelName,
                agoraToken = agoraToken,
                startTime = System.currentTimeMillis(),
                isActive = true,
                viewerCount = 0
            )
        } catch (e: Exception) {
            android.util.Log.e("FirebaseManager", "❌ ===== ERROR INICIANDO LIVE =====")
            android.util.Log.e("FirebaseManager", "❌ Mensaje: ${e.message}")
            android.util.Log.e("FirebaseManager", "❌ Stack trace:", e)
            null
        }
    }
    
    /**
     * Crear una sesión de Live (versión simplificada para compatibilidad - LEGACY AGORA)
     * Esta función se usa cuando ya se tiene el sessionId y token generados
     */
    suspend fun createLiveSession(
        sessionId: String,
        userId: String,
        username: String,
        channelName: String,
        title: String
    ) {
        try {
            android.util.Log.d("FirebaseManager", "📝 Creando sesión de Live (Agora): $sessionId")
            
            val sessionData = hashMapOf(
                "sessionId" to sessionId,
                "userId" to userId,
                "username" to username,
                "profileImageUrl" to "",
                "title" to title,
                "agoraChannelName" to channelName,
                "agoraToken" to "", // El token ya se usó para conectar
                "startTime" to System.currentTimeMillis(),
                "isActive" to true,
                "viewerCount" to 0,
                "provider" to "agora"
            )
            
            firestore.collection("live_sessions")
                .document(sessionId)
                .set(sessionData)
                .await()
            
            android.util.Log.d("FirebaseManager", "✅ Sesión de Live creada: $sessionId")
        } catch (e: Exception) {
            android.util.Log.e("FirebaseManager", "❌ Error creando sesión: ${e.message}")
            throw e
        }
    }
    
    /**
     * Generar un ID único para sesión de Live
     */
    fun generateSessionId(): String {
        return firestore.collection("live_sessions").document().id
    }
    
    /**
     * Crear sesión de Live para ZegoCloud (sin token de backend)
     * ZegoCloud usa APP_ID y APP_SIGN directamente, no necesita token del servidor
     */
    suspend fun createLiveSessionZego(
        sessionId: String,
        userId: String,
        username: String,
        channelName: String,
        title: String
    ) {
        try {
            android.util.Log.d("FirebaseManager", "📝 [ZEGO] Creando sesión de live en Firestore...")
            android.util.Log.d("FirebaseManager", "   SessionId: $sessionId")
            android.util.Log.d("FirebaseManager", "   UserId: $userId")
            android.util.Log.d("FirebaseManager", "   Username: $username")
            android.util.Log.d("FirebaseManager", "   ChannelName: $channelName")
            
            val liveData = hashMapOf(
                "sessionId" to sessionId,
                "userId" to userId,
                "username" to username,
                "channelName" to channelName,
                "title" to title,
                "isActive" to true,
                "viewerCount" to 0,
                "startTime" to System.currentTimeMillis(),
                "provider" to "zegocloud",  // Identificar que usa ZegoCloud
                "endTime" to null,
                "createdAt" to com.google.firebase.Timestamp.now()
            )
            
            firestore.collection("live_sessions")
                .document(sessionId)
                .set(liveData)
                .await()
            
            android.util.Log.d("FirebaseManager", "✅ [ZEGO] Sesión de live creada exitosamente")
        } catch (e: Exception) {
            android.util.Log.e("FirebaseManager", "❌ [ZEGO] Error creando sesión: ${e.message}")
            throw e
        }
    }
    
    /**
     * Observa la colección 'live_sessions' en tiempo real.
     * Filtra solo las sesiones marcadas como activas (isActive = true).
     * 
     * @param onUpdate Función de callback que se llama cada vez que la lista de Lives cambia.
     * @return El objeto ListenerRegistration para detener la escucha.
     */
    fun observeLiveSessions(onUpdate: (List<LiveSession>) -> Unit): com.google.firebase.firestore.ListenerRegistration {
        android.util.Log.d("FirebaseManager", "👀 Iniciando observación de Lives activos...")
        android.util.Log.d("FirebaseManager", "📍 Colección: live_sessions")
        android.util.Log.d("FirebaseManager", "🔍 Filtro: isActive = true")
        
        return firestore.collection("live_sessions")
            .whereEqualTo("isActive", true)
            // NOTA: orderBy removido temporalmente para evitar error de índice
            // .orderBy("startTime", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    android.util.Log.e("FirebaseManager", "❌ ===== ERROR ESCUCHANDO LIVES =====")
                    android.util.Log.e("FirebaseManager", "❌ Mensaje: ${error.message}")
                    android.util.Log.e("FirebaseManager", "❌ Código: ${error.code}")
                    android.util.Log.e("FirebaseManager", "❌ Stack trace:", error)
                    android.util.Log.e("FirebaseManager", "❌ =====================================")
                    onUpdate(emptyList())
                    return@addSnapshotListener
                }
                
                if (snapshot != null) {
                    android.util.Log.d("FirebaseManager", "📦 Snapshot recibido")
                    android.util.Log.d("FirebaseManager", "📊 Total de documentos: ${snapshot.documents.size}")
                    android.util.Log.d("FirebaseManager", "📊 Documentos vacíos: ${snapshot.isEmpty}")
                    
                    // Log de cada documento RAW
                    snapshot.documents.forEachIndexed { index, doc ->
                        android.util.Log.d("FirebaseManager", "📄 Documento ${index + 1}:")
                        android.util.Log.d("FirebaseManager", "   ID: ${doc.id}")
                        android.util.Log.d("FirebaseManager", "   Existe: ${doc.exists()}")
                        android.util.Log.d("FirebaseManager", "   isActive: ${doc.getBoolean("isActive")}")
                        android.util.Log.d("FirebaseManager", "   username: ${doc.getString("username")}")
                        android.util.Log.d("FirebaseManager", "   sessionId: ${doc.getString("sessionId")}")
                    }
                    
                    val liveSessions = snapshot.documents.mapNotNull { document ->
                        try {
                            val session = document.toObject(LiveSession::class.java)
                            if (session != null) {
                                android.util.Log.d("FirebaseManager", "✅ Live parseado: ${session.username}")
                            }
                            session
                        } catch (e: Exception) {
                            android.util.Log.e("FirebaseManager", "❌ Error parseando Live: ${e.message}", e)
                            null
                        }
                    }
                    
                    android.util.Log.d("FirebaseManager", "🔴 ===== RESULTADO FINAL =====")
                    android.util.Log.d("FirebaseManager", "🔴 Lives detectados y actualizados: ${liveSessions.size}")
                    liveSessions.forEach { session ->
                        android.util.Log.d("FirebaseManager", "  📡 ${session.username} - ${session.title} (${session.viewerCount} viewers)")
                    }
                    android.util.Log.d("FirebaseManager", "🔴 ============================")
                    
                    onUpdate(liveSessions)
                } else {
                    android.util.Log.w("FirebaseManager", "⚠️ Snapshot es null")
                    onUpdate(emptyList())
                }
            }
    }
    
    // ============================================
    // FUNCIÓN PARA OBTENER CANCIONES DE ARTISTAS SEGUIDOS
    // ============================================
    
    /**
     * Obtiene las canciones de los artistas que el usuario sigue
     */
    suspend fun getSongsFromFollowing(userId: String): List<ArtistCard> {
        return try {
            // Obtener lista de usuarios que sigue usando la subcolección existente
            val followingSnapshot = firestore.collection("users")
                .document(userId)
                .collection("following")
                .get()
                .await()
            
            val following: List<String> = followingSnapshot.documents.map { it.id }
            
            if (following.isEmpty()) {
                android.util.Log.d("FirebaseManager", "📭 Usuario no sigue a nadie")
                return emptyList()
            }
            
            android.util.Log.d("FirebaseManager", "🎵 Buscando canciones de ${following.size} artistas seguidos")
            
            // Obtener canciones de esos artistas
            val songs: MutableList<ArtistCard> = mutableListOf()
            
            // Firestore tiene límite de 10 elementos en whereIn, así que dividimos en chunks
            following.chunked(10).forEach { chunk: List<String> ->
                val snapshot: com.google.firebase.firestore.QuerySnapshot = firestore.collection("songs")
                    .whereIn("artistId", chunk)
                    .orderBy("uploadDate", com.google.firebase.firestore.Query.Direction.DESCENDING)
                    .get()
                    .await()
                
                snapshot.documents.forEach { doc ->
                    try {
                        val song = ArtistCard(
                            id = doc.id,
                            userId = doc.getString("artistId") ?: "",
                            name = doc.getString("artistName") ?: "Artista Desconocido",
                            emoji = "🎵",
                            genre = doc.getString("genre") ?: "Desconocido",
                            location = doc.getString("location") ?: "Desconocido",
                            bio = doc.getString("bio") ?: "",
                            photos = emptyList(),
                            imageUrl = doc.getString("imageUrl") ?: "",
                            socialLinks = emptyMap(),
                            stats = ArtistStats(
                                followers = "0",
                                songs = 0,
                                plays = (doc.getLong("plays") ?: 0).toString()
                            ),
                            songUrl = doc.getString("audioUrl") ?: ""
                        )
                        songs.add(song)
                    } catch (e: Exception) {
                        android.util.Log.e("FirebaseManager", "Error parseando canción: ${e.message}")
                    }
                }
            }
            
            android.util.Log.d("FirebaseManager", "✅ Encontradas ${songs.size} canciones de artistas seguidos")
            songs
        } catch (e: Exception) {
            android.util.Log.e("FirebaseManager", "❌ Error obteniendo canciones de following: ${e.message}")
            emptyList()
        }
    }
    
    // ============ VERIFICACIÓN DE PERFIL COMPLETO ============
    
    /**
     * Verifica si un usuario tiene un perfil completo en Firestore.
     * Los usuarios que solo se autentican con Google sin crear perfil NO pueden participar en concursos.
     * 
     * @param userId ID del usuario a verificar
     * @return true si el usuario tiene perfil completo (documento existe y tiene username), false en caso contrario
     */
    suspend fun hasCompleteProfile(userId: String): Boolean {
        return try {
            if (userId.isEmpty()) {
                android.util.Log.w("FirebaseManager", "⚠️ userId vacío, perfil incompleto")
                return false
            }
            
            val doc = firestore.collection("users")
                .document(userId)
                .get()
                .await()
            
            // Verificar que el documento existe
            if (!doc.exists()) {
                android.util.Log.w("FirebaseManager", "⚠️ Usuario $userId no tiene documento en Firestore")
                return false
            }
            
            // Verificar que tiene username configurado
            val username = doc.getString("username")
            val hasUsername = !username.isNullOrEmpty()
            
            if (!hasUsername) {
                android.util.Log.w("FirebaseManager", "⚠️ Usuario $userId no tiene username configurado")
                return false
            }
            
            android.util.Log.d("FirebaseManager", "✅ Usuario $userId tiene perfil completo (username: $username)")
            true
            
        } catch (e: Exception) {
            android.util.Log.e("FirebaseManager", "❌ Error verificando perfil de $userId: ${e.message}")
            false
        }
    }
    
    // ============ ELIMINACIÓN COMPLETA DE DATOS DEL USUARIO ============
    
    /**
     * Elimina TODOS los datos del usuario de Firebase (Firestore y Storage).
     * Esto incluye:
     * - Documento del usuario en /users/{userId}
     * - Subcolecciones: rejectedSongs, viewedStories, following, followers
     * - Canciones subidas por el usuario
     * - Historias del usuario
     * - Videos de concursos del usuario
     * - Comentarios del usuario
     * - Likes del usuario
     * - Archivos en Storage (fotos de perfil, portada, galería, etc.)
     * 
     * @param userId ID del usuario a eliminar
     */
    suspend fun deleteAllUserData(userId: String) {
        try {
            android.util.Log.d("FirebaseManager", "🗑️ ===== INICIANDO ELIMINACIÓN COMPLETA DE DATOS =====")
            android.util.Log.d("FirebaseManager", "👤 Usuario: $userId")
            
            // 1. Eliminar subcolección: rejectedSongs
            android.util.Log.d("FirebaseManager", "🗑️ Eliminando canciones rechazadas...")
            val rejectedSongs = firestore.collection("users")
                .document(userId)
                .collection("rejectedSongs")
                .get()
                .await()
            rejectedSongs.documents.forEach { it.reference.delete().await() }
            android.util.Log.d("FirebaseManager", "✅ ${rejectedSongs.size()} canciones rechazadas eliminadas")
            
            // 2. Eliminar subcolección: viewedStories
            android.util.Log.d("FirebaseManager", "🗑️ Eliminando historias vistas...")
            val viewedStories = firestore.collection("users")
                .document(userId)
                .collection("viewedStories")
                .get()
                .await()
            viewedStories.documents.forEach { it.reference.delete().await() }
            android.util.Log.d("FirebaseManager", "✅ ${viewedStories.size()} historias vistas eliminadas")
            
            // 3. Eliminar subcolección: following
            android.util.Log.d("FirebaseManager", "🗑️ Eliminando lista de seguidos...")
            val following = firestore.collection("users")
                .document(userId)
                .collection("following")
                .get()
                .await()
            following.documents.forEach { 
                val targetUserId = it.id
                // Eliminar el documento de following
                it.reference.delete().await()
                // Decrementar contador de followers del usuario seguido
                try {
                    firestore.collection("users").document(targetUserId)
                        .update("followers", com.google.firebase.firestore.FieldValue.increment(-1))
                        .await()
                    // Eliminar de la colección de followers del target
                    firestore.collection("users")
                        .document(targetUserId)
                        .collection("followers")
                        .document(userId)
                        .delete()
                        .await()
                } catch (e: Exception) {
                    android.util.Log.w("FirebaseManager", "No se pudo actualizar followers de $targetUserId")
                }
            }
            android.util.Log.d("FirebaseManager", "✅ ${following.size()} seguidos eliminados")
            
            // 4. Eliminar subcolección: followers
            android.util.Log.d("FirebaseManager", "🗑️ Eliminando lista de seguidores...")
            val followers = firestore.collection("users")
                .document(userId)
                .collection("followers")
                .get()
                .await()
            followers.documents.forEach { 
                val followerUserId = it.id
                // Eliminar el documento de followers
                it.reference.delete().await()
                // Decrementar contador de following del seguidor
                try {
                    firestore.collection("users").document(followerUserId)
                        .update("following", com.google.firebase.firestore.FieldValue.increment(-1))
                        .await()
                    // Eliminar de la colección de following del follower
                    firestore.collection("users")
                        .document(followerUserId)
                        .collection("following")
                        .document(userId)
                        .delete()
                        .await()
                } catch (e: Exception) {
                    android.util.Log.w("FirebaseManager", "No se pudo actualizar following de $followerUserId")
                }
            }
            android.util.Log.d("FirebaseManager", "✅ ${followers.size()} seguidores eliminados")
            
            // 5. Eliminar canciones del usuario
            android.util.Log.d("FirebaseManager", "🗑️ Eliminando canciones...")
            val songs = firestore.collection("songs")
                .whereEqualTo("artistId", userId)
                .get()
                .await()
            songs.documents.forEach { it.reference.delete().await() }
            android.util.Log.d("FirebaseManager", "✅ ${songs.size()} canciones eliminadas")
            
            // 6. Eliminar historias del usuario
            android.util.Log.d("FirebaseManager", "🗑️ Eliminando historias...")
            val stories = firestore.collection("stories")
                .whereEqualTo("artistId", userId)
                .get()
                .await()
            stories.documents.forEach { it.reference.delete().await() }
            android.util.Log.d("FirebaseManager", "✅ ${stories.size()} historias eliminadas")
            
            // 7. Eliminar videos de concursos del usuario
            android.util.Log.d("FirebaseManager", "🗑️ Eliminando videos de concursos...")
            val contestEntries = firestore.collection("contest_entries")
                .whereEqualTo("userId", userId)
                .get()
                .await()
            contestEntries.documents.forEach { it.reference.delete().await() }
            android.util.Log.d("FirebaseManager", "✅ ${contestEntries.size()} videos de concursos eliminados")
            
            // 8. Eliminar comentarios del usuario en todas las canciones
            android.util.Log.d("FirebaseManager", "🗑️ Eliminando comentarios...")
            val allSongs = firestore.collection("songs").get().await()
            var commentsDeleted = 0
            allSongs.documents.forEach { songDoc ->
                val comments = songDoc.reference.collection("comments")
                    .whereEqualTo("userId", userId)
                    .get()
                    .await()
                comments.documents.forEach { 
                    it.reference.delete().await()
                    commentsDeleted++
                }
            }
            android.util.Log.d("FirebaseManager", "✅ $commentsDeleted comentarios eliminados")
            
            // 9. Eliminar likes del usuario en canciones
            android.util.Log.d("FirebaseManager", "🗑️ Eliminando likes de canciones...")
            var likesDeleted = 0
            allSongs.documents.forEach { songDoc ->
                val like = songDoc.reference.collection("likes")
                    .document(userId)
                    .get()
                    .await()
                if (like.exists()) {
                    like.reference.delete().await()
                    // Decrementar contador de likes
                    songDoc.reference.update("likes", com.google.firebase.firestore.FieldValue.increment(-1)).await()
                    likesDeleted++
                }
            }
            android.util.Log.d("FirebaseManager", "✅ $likesDeleted likes de canciones eliminados")
            
            // 10. Eliminar likes del usuario en videos de concursos
            android.util.Log.d("FirebaseManager", "🗑️ Eliminando likes de videos...")
            val allContestEntries = firestore.collection("contest_entries").get().await()
            var videoLikesDeleted = 0
            allContestEntries.documents.forEach { entryDoc ->
                val like = entryDoc.reference.collection("likes")
                    .document(userId)
                    .get()
                    .await()
                if (like.exists()) {
                    like.reference.delete().await()
                    // Decrementar contador de likes
                    entryDoc.reference.update("likes", com.google.firebase.firestore.FieldValue.increment(-1)).await()
                    videoLikesDeleted++
                }
            }
            android.util.Log.d("FirebaseManager", "✅ $videoLikesDeleted likes de videos eliminados")
            
            // 11. Eliminar documento principal del usuario
            android.util.Log.d("FirebaseManager", "🗑️ Eliminando documento principal del usuario...")
            firestore.collection("users").document(userId).delete().await()
            android.util.Log.d("FirebaseManager", "✅ Documento principal eliminado")
            
            // 12. Eliminar archivos de Storage (opcional, puede tardar)
            android.util.Log.d("FirebaseManager", "🗑️ Eliminando archivos de Storage...")
            try {
                // Eliminar foto de perfil
                storage.reference.child("profile_images/$userId.jpg").delete().await()
            } catch (e: Exception) {
                android.util.Log.w("FirebaseManager", "No se pudo eliminar foto de perfil")
            }
            try {
                // Eliminar foto de portada
                storage.reference.child("cover_images/$userId.jpg").delete().await()
            } catch (e: Exception) {
                android.util.Log.w("FirebaseManager", "No se pudo eliminar foto de portada")
            }
            try {
                // Eliminar carpeta de galería
                val galleryPhotos = storage.reference.child("gallery_photos/$userId").listAll().await()
                galleryPhotos.items.forEach { it.delete().await() }
            } catch (e: Exception) {
                android.util.Log.w("FirebaseManager", "No se pudo eliminar galería de fotos")
            }
            try {
                // Eliminar carpeta de videos de galería
                val galleryVideos = storage.reference.child("gallery_videos/$userId").listAll().await()
                galleryVideos.items.forEach { it.delete().await() }
            } catch (e: Exception) {
                android.util.Log.w("FirebaseManager", "No se pudo eliminar galería de videos")
            }
            
            android.util.Log.d("FirebaseManager", "✅ ===== ELIMINACIÓN COMPLETA FINALIZADA =====")
            
        } catch (e: Exception) {
            android.util.Log.e("FirebaseManager", "❌ Error eliminando datos del usuario: ${e.message}", e)
            throw Exception("Error al eliminar datos del usuario: ${e.message}")
        }
    }
    
}
