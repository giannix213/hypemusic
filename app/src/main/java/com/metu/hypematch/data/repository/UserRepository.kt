package com.metu.hypematch.data.repository

import com.metu.hypematch.FirebaseManager
import com.metu.hypematch.UserProfile
import com.metu.hypematch.data.local.UserProfileDao
import com.metu.hypematch.data.local.UserProfileEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * 🚀 FASE 3: Repository Pattern con Cache-First
 * 
 * PATRÓN CRÍTICO:
 * 1. Emite datos de caché inmediatamente (< 50ms)
 * 2. Actualiza desde Firebase en background
 * 3. Room emite automáticamente los datos actualizados
 * 
 * Beneficios:
 * - Carga instantánea
 * - Funciona offline
 * - Datos siempre frescos
 * - Mejor UX
 */
class UserRepository(
    private val userProfileDao: UserProfileDao,
    private val firebaseManager: FirebaseManager
) {
    /**
     * Obtener perfil de usuario con patrón Cache-First
     */
    fun getUserProfile(userId: String): Flow<UserProfile?> = flow {
        android.util.Log.d("UserRepository", "🔍 Buscando perfil de $userId")
        
        // 1. ⚡ EMITIR CACHÉ INMEDIATAMENTE
        val cachedProfile = userProfileDao.getUserProfileSync(userId)
        if (cachedProfile != null) {
            android.util.Log.d("UserRepository", "⚡ Emitiendo perfil desde caché (${cachedProfile.username})")
            emit(cachedProfile.toUserProfile())
        } else {
            android.util.Log.d("UserRepository", "⚠️ No hay caché, esperando Firebase...")
        }
        
        // 2. 🔄 ACTUALIZAR DESDE FIREBASE EN BACKGROUND
        try {
            val networkProfile = withContext(Dispatchers.IO) {
                firebaseManager.getFullUserProfile(userId)
            }
            
            if (networkProfile != null) {
                // 3. 💾 GUARDAR EN CACHÉ
                userProfileDao.insertUserProfile(networkProfile.toEntity())
                android.util.Log.d("UserRepository", "✅ Perfil actualizado desde Firebase")
                
                // Emitir perfil actualizado
                emit(networkProfile)
            }
        } catch (e: Exception) {
            android.util.Log.e("UserRepository", "❌ Error actualizando perfil: ${e.message}")
            // Si falla, el usuario sigue viendo datos de caché
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Forzar actualización desde Firebase
     */
    suspend fun refreshUserProfile(userId: String) {
        withContext(Dispatchers.IO) {
            try {
                android.util.Log.d("UserRepository", "🔄 Refrescando perfil de $userId")
                val profile = firebaseManager.getFullUserProfile(userId)
                if (profile != null) {
                    userProfileDao.insertUserProfile(profile.toEntity())
                    android.util.Log.d("UserRepository", "✅ Perfil refrescado")
                }
            } catch (e: Exception) {
                android.util.Log.e("UserRepository", "❌ Error refrescando: ${e.message}")
            }
        }
    }
    
    /**
     * Obtener estadísticas de caché
     */
    suspend fun getCacheStats(): String {
        return withContext(Dispatchers.IO) {
            val count = userProfileDao.getProfileCount()
            "Perfiles en caché: $count"
        }
    }
}

// Extension functions para convertir entre entidades
private fun UserProfileEntity.toUserProfile(): UserProfile {
    return UserProfile(
        userId = userId,
        username = username,
        isArtist = isArtist,
        bio = bio,
        profileImageUrl = profileImageUrl,
        coverImageUrl = coverImageUrl,
        galleryPhotos = galleryPhotos,
        galleryVideos = galleryVideos,
        socialLinks = socialLinks,
        followers = followers,
        following = following,
        totalPlays = totalPlays,
        totalSongs = totalSongs,
        createdAt = createdAt
    )
}

private fun UserProfile.toEntity(): UserProfileEntity {
    return UserProfileEntity(
        userId = userId,
        username = username,
        isArtist = isArtist,
        bio = bio,
        profileImageUrl = profileImageUrl,
        coverImageUrl = coverImageUrl,
        galleryPhotos = galleryPhotos,
        galleryVideos = galleryVideos,
        socialLinks = socialLinks,
        followers = followers,
        following = following,
        totalPlays = totalPlays,
        totalSongs = totalSongs,
        createdAt = createdAt
    )
}
