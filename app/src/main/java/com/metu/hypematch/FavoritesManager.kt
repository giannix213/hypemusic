package com.metu.hypematch

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class FavoriteSong(
    val id: String,
    val artistName: String,
    val genre: String,
    val location: String,
    val songUrl: String,
    val reactionType: String, // "❤️" o "🔥"
    val timestamp: Long = System.currentTimeMillis()
)

class FavoritesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("favorites", Context.MODE_PRIVATE)
    private val gson = Gson()
    
    fun addFavorite(artist: ArtistCard, reactionType: String) {
        val favorites = getFavorites().toMutableList()
        
        // Verificar si ya existe
        if (favorites.any { it.id == artist.id }) {
            return
        }
        
        val emoji = if (reactionType == "heart") "❤️" else "🔥"
        
        val favorite = FavoriteSong(
            id = artist.id,
            artistName = artist.name,
            genre = artist.genre,
            location = artist.location,
            songUrl = artist.songUrl,
            reactionType = emoji
        )
        
        favorites.add(favorite)
        saveFavorites(favorites)
    }
    
    fun addRejected(artistId: String) {
        val rejected = getRejected().toMutableSet()
        rejected.add(artistId)
        saveRejected(rejected)
    }
    
    fun getRejected(): Set<String> {
        val json = prefs.getString("rejected_list", null) ?: return emptySet()
        val type = object : TypeToken<Set<String>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptySet()
        }
    }
    
    fun isAlreadySeen(artistId: String): Boolean {
        // Verificar si está en favoritos o rechazados
        return getFavorites().any { it.id == artistId } || getRejected().contains(artistId)
    }
    
    private fun saveRejected(rejected: Set<String>) {
        val json = gson.toJson(rejected)
        prefs.edit().putString("rejected_list", json).apply()
    }
    
    fun getFavorites(): List<FavoriteSong> {
        val json = prefs.getString("favorites_list", null) ?: return emptyList()
        val type = object : TypeToken<List<FavoriteSong>>() {}.type
        return try {
            gson.fromJson(json, type)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    fun removeFavorite(id: String) {
        val favorites = getFavorites().toMutableList()
        favorites.removeAll { it.id == id }
        saveFavorites(favorites)
    }
    
    private fun saveFavorites(favorites: List<FavoriteSong>) {
        val json = gson.toJson(favorites)
        prefs.edit().putString("favorites_list", json).apply()
    }
}
