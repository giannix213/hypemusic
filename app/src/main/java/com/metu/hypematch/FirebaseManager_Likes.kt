package com.metu.hypematch

import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

// Extensión de FirebaseManager para manejar likes de canciones
class SongLikesManager {
    private val firestore = FirebaseFirestore.getInstance()
    
    // Dar like a una canción (siempre agrega, no quita)
    suspend fun likeSong(songId: String, userId: String): Boolean {
        return try {
            val likeRef = firestore.collection("songs")
                .document(songId)
                .collection("likes")
                .document(userId)
            
            val likeDoc = likeRef.get().await()
            
            if (!likeDoc.exists()) {
                // Solo dar like si no existe
                likeRef.set(hashMapOf("timestamp" to System.currentTimeMillis())).await()
                firestore.collection("songs").document(songId)
                    .update("likes", com.google.firebase.firestore.FieldValue.increment(1))
                    .await()
                android.util.Log.d("SongLikesManager", "✅ Like agregado a canción $songId por usuario $userId")
            } else {
                android.util.Log.d("SongLikesManager", "⚠️ Usuario $userId ya dio like a canción $songId")
            }
            true
        } catch (e: Exception) {
            android.util.Log.e("SongLikesManager", "❌ Error al dar like: ${e.message}")
            false
        }
    }
    
    // Dar/quitar like a una canción (toggle)
    suspend fun toggleSongLike(songId: String, userId: String): Boolean {
        return try {
            val likeRef = firestore.collection("songs")
                .document(songId)
                .collection("likes")
                .document(userId)
            
            val likeDoc = likeRef.get().await()
            
            if (likeDoc.exists()) {
                // Quitar like
                likeRef.delete().await()
                firestore.collection("songs").document(songId)
                    .update("likes", com.google.firebase.firestore.FieldValue.increment(-1))
                    .await()
                false
            } else {
                // Dar like
                likeRef.set(hashMapOf("timestamp" to System.currentTimeMillis())).await()
                firestore.collection("songs").document(songId)
                    .update("likes", com.google.firebase.firestore.FieldValue.increment(1))
                    .await()
                true
            }
        } catch (e: Exception) {
            android.util.Log.e("SongLikesManager", "Error al dar like: ${e.message}")
            false
        }
    }
    
    // Verificar si el usuario dio like a una canción
    suspend fun hasUserLikedSong(songId: String, userId: String): Boolean {
        return try {
            firestore.collection("songs")
                .document(songId)
                .collection("likes")
                .document(userId)
                .get()
                .await()
                .exists()
        } catch (e: Exception) {
            false
        }
    }
    
    // Obtener todas las canciones que le gustaron al usuario
    suspend fun getUserLikedSongs(userId: String): List<String> {
        return try {
            val allSongs = firestore.collection("songs").get().await()
            val likedSongs = mutableListOf<String>()
            
            for (songDoc in allSongs.documents) {
                val hasLiked = hasUserLikedSong(songDoc.id, userId)
                if (hasLiked) {
                    likedSongs.add(songDoc.id)
                }
            }
            
            likedSongs
        } catch (e: Exception) {
            android.util.Log.e("SongLikesManager", "Error obteniendo canciones gustadas: ${e.message}")
            emptyList()
        }
    }
    
    // Obtener canciones que le gustaron al usuario (con detalles completos)
    suspend fun getUserLikedSongsDetails(userId: String, firebaseManager: FirebaseManager): List<ArtistCard> {
        return try {
            val likedSongIds = getUserLikedSongs(userId)
            val allSongs = firebaseManager.getAllSongs()
            
            allSongs.filter { it.id in likedSongIds }
        } catch (e: Exception) {
            android.util.Log.e("SongLikesManager", "Error obteniendo detalles de canciones gustadas: ${e.message}")
            emptyList()
        }
    }
}
