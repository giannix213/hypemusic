package com.metu.hypematch

import androidx.compose.ui.graphics.Color

// Modelo para subir canciones a Firebase
data class UploadSongData(
    val title: String = "",
    val artistName: String = "",
    val artistId: String = "", // ID del usuario que sube la canción
    val genre: String = "",
    val location: String = "",
    val audioUrl: String = "",
    val imageUrl: String = "", // URL de la foto de perfil del artista
    val videoUrl: String = "", // URL del video de portada (opcional)
    val bio: String = "",
    val uploadDate: Long = System.currentTimeMillis(),
    val plays: Long = 0
)

// Modelo para el perfil completo del usuario
data class UserProfile(
    val userId: String = "",
    val username: String = "",
    val isArtist: Boolean = false,
    val bio: String = "",
    val profileImageUrl: String = "",
    val coverImageUrl: String = "",
    val galleryPhotos: List<String> = emptyList(), // URLs de fotos adicionales
    val galleryVideos: List<String> = emptyList(), // URLs de videos
    val socialLinks: Map<String, String> = emptyMap(), // Ej: "Instagram" to "@usuario"
    val followers: Int = 0,
    val following: Int = 0,
    val totalPlays: Long = 0,
    val totalSongs: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

// Modelo principal para la tarjeta de un artista.
// MainActivity lo usa como el parámetro `artist: ArtistCard`.
data class ArtistCard(
    val id: String,
    val userId: String = "", // ID del usuario/artista
    val name: String,
    val emoji: String,
    val genre: String,
    val location: String,
    val bio: String,
    val photos: List<String>, // URLs de imágenes o emojis
    val imageUrl: String = "", // URL de la imagen principal del artista
    val socialLinks: Map<String, String>, // Ej: "Instagram" to "@usuario"
    val stats: ArtistStats,
    val songUrl: String // IMPORTANTE: URL para que ExoPlayer pueda reproducir la canción
)

// Modelo para las estadísticas del artista.
// Usado dentro de ArtistCard y en la InfoPage.
data class ArtistStats(
    val followers: String, // Usamos String para poder poner "1.2M", "25K", etc.
    val songs: Int,
    val plays: String
)

// Objeto para definir la paleta de colores de la app.
// MainActivity lo usa extensivamente: PopArtColors.Yellow, PopArtColors.Black, etc.
object PopArtColors {
    val Yellow = Color(0xFFFFEB3B)
    val Pink = Color(0xFFE91E63)
    val Cyan = Color(0xFF00BCD4)
    val Purple = Color(0xFF9C27B0)
    val Orange = Color(0xFFFF9800)
    val Black = Color(0xFF000000)
    val White = Color(0xFFFFFFFF)
    val MulticolorGradient = androidx.compose.ui.graphics.Brush.verticalGradient(
        colors = listOf(Pink, Cyan, Purple)
    )
}

// Modelos para Lives y Concursos
data class LiveStream(
    val id: String,
    val name: String,
    val artistName: String,
    val location: String,
    val emoji: String,
    val viewers: Int,
    val isLive: Boolean,
    val startTime: Long,
    val thumbnailUrl: String = ""
)

data class Concert(
    val name: String,
    val location: String,
    val date: String,
    val emoji: String,
    val time: String
)

data class Contest(
    val name: String,
    val prize: String,
    val deadline: String,
    val emoji: String,
    val color: Color,
    val type: ContestType = ContestType.FAST, // Rápido o Alto Impacto
    val category: String = "", // Categoría del concurso
    val allowMultipleEntries: Boolean = true // Permite participaciones ilimitadas
)

enum class ContestType {
    FAST,        // Concursos Rápidos (semanal/mensual)
    HIGH_IMPACT  // Concursos de Alto Impacto (cada 3-6 meses)
}

// Modelo para entradas de concurso (videos subidos por usuarios)
data class ContestEntry(
    val id: String = "",
    val userId: String = "",
    val username: String = "",
    val profilePictureUrl: String = "",  // URL de la foto de perfil del usuario
    val videoUrl: String = "",
    val thumbnailUrl: String = "",
    val title: String = "",
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val likes: Int = 0,
    val views: Int = 0,
    val comments: Int = 0,
    val contestId: String = "default"
)

// Modelo para historias (nuevo sistema de highlights)
data class Story(
    val id: String = "",
    val userId: String = "",
    val username: String = "",
    val imageUrl: String = "",
    val videoUrl: String = "",
    val timestamp: Long = 0L,
    val isHighlighted: Boolean = false
)

// Modelo para estados/stories de artistas (sistema antiguo)
data class ArtistStory(
    val id: String = "",
    val userId: String = "", // ID del usuario que creó la historia
    val artistId: String = "",
    val artistName: String = "",
    val artistImageUrl: String = "",
    val mediaUrl: String = "", // URL de imagen o video
    val mediaType: String = "image", // "image" o "video"
    val caption: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 86400000, // 24 horas
    val views: Int = 0,
    val isViewed: Boolean = false,
    val isHighlighted: Boolean = false
) {
    // Verificar si el estado ha expirado
    fun isExpired(): Boolean {
        return System.currentTimeMillis() > expiresAt
    }
    
    // Obtener tiempo restante en horas
    fun getTimeRemaining(): String {
        val remaining = expiresAt - System.currentTimeMillis()
        val hours = remaining / 3600000
        return if (hours > 0) "${hours}h" else "Expira pronto"
    }
}

// Modelo para comentarios (usado en canciones y videos)
data class VideoComment(
    val id: String = "",
    val username: String = "",
    val text: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    var likes: Int = 0,
    var isLiked: Boolean = false,
    val replies: List<VideoComment> = emptyList(),
    val isVoiceNote: Boolean = false
) {
    // Función para formatear el timestamp
    fun getFormattedTime(): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        
        return when {
            diff < 60_000 -> "Ahora" // Menos de 1 minuto
            diff < 3_600_000 -> "Hace ${diff / 60_000}m" // Menos de 1 hora
            diff < 86_400_000 -> "Hace ${diff / 3_600_000}h" // Menos de 1 día
            diff < 604_800_000 -> "Hace ${diff / 86_400_000}d" // Menos de 1 semana
            else -> "Hace ${diff / 604_800_000}sem" // Semanas
        }
    }
}
