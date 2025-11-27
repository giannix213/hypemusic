package com.metu.hypematch.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters

/**
 * 🚀 FASE 3: Entidad de Room para UserProfile
 * 
 * Almacena perfiles de usuario en caché local para carga instantánea
 */
@Entity(tableName = "user_profiles")
@TypeConverters(Converters::class)
data class UserProfileEntity(
    @PrimaryKey
    val userId: String,
    val username: String,
    val isArtist: Boolean,
    val bio: String,
    val profileImageUrl: String,
    val coverImageUrl: String,
    val galleryPhotos: List<String>,
    val galleryVideos: List<String>,
    val socialLinks: Map<String, String>,
    val followers: Int,
    val following: Int,
    val totalPlays: Long,
    val totalSongs: Int,
    val createdAt: Long,
    val lastUpdated: Long = System.currentTimeMillis()
)
