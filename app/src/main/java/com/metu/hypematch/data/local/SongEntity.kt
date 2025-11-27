package com.metu.hypematch.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 🚀 FASE 3: Entidad de Room para Songs
 * 
 * Almacena canciones en caché local para carga instantánea
 */
@Entity(tableName = "songs")
data class SongEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val name: String,
    val genre: String,
    val location: String,
    val imageUrl: String,
    val songUrl: String,
    val bio: String,
    val plays: Long,
    val lastUpdated: Long = System.currentTimeMillis()
)
