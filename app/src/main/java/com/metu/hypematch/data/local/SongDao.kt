package com.metu.hypematch.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 🚀 FASE 3: DAO para Songs
 * 
 * Operaciones de base de datos para canciones
 */
@Dao
interface SongDao {
    @Query("SELECT * FROM songs ORDER BY lastUpdated DESC LIMIT :limit")
    fun getSongs(limit: Int = 10): Flow<List<SongEntity>>
    
    @Query("SELECT * FROM songs WHERE userId = :userId ORDER BY lastUpdated DESC")
    fun getUserSongs(userId: String): Flow<List<SongEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSongs(songs: List<SongEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSong(song: SongEntity)
    
    @Query("DELETE FROM songs WHERE id = :songId")
    suspend fun deleteSong(songId: String)
    
    @Query("DELETE FROM songs")
    suspend fun deleteAllSongs()
    
    @Query("SELECT COUNT(*) FROM songs")
    suspend fun getSongCount(): Int
}
