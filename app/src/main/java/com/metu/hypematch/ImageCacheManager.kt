package com.metu.hypematch

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL

/**
 * Gestor de caché de imágenes para cargar fotos de perfil/portada instantáneamente
 * Guarda copias locales de las imágenes para eliminar el retraso de descarga
 */
class ImageCacheManager(private val context: Context) {
    
    private val cacheDir = File(context.cacheDir, "profile_images")
    
    init {
        // Crear directorio de caché si no existe
        if (!cacheDir.exists()) {
            cacheDir.mkdirs()
        }
    }
    
    /**
     * Obtiene la ruta local de una imagen cacheada
     * @param imageUrl URL de la imagen en Firebase
     * @param imageType Tipo de imagen ("profile" o "cover")
     * @return File local si existe, null si no está cacheada
     */
    fun getCachedImageFile(imageUrl: String, imageType: String): File? {
        if (imageUrl.isEmpty()) return null
        
        val fileName = "${imageType}_${imageUrl.hashCode()}.jpg"
        val file = File(cacheDir, fileName)
        
        return if (file.exists()) {
            Log.d("ImageCache", "✅ Imagen encontrada en caché: $imageType")
            file
        } else {
            Log.d("ImageCache", "❌ Imagen no encontrada en caché: $imageType")
            null
        }
    }
    
    /**
     * Descarga y guarda una imagen en caché local
     * @param imageUrl URL de la imagen en Firebase
     * @param imageType Tipo de imagen ("profile" o "cover")
     */
    suspend fun cacheImage(imageUrl: String, imageType: String) = withContext(Dispatchers.IO) {
        if (imageUrl.isEmpty()) return@withContext
        
        try {
            val fileName = "${imageType}_${imageUrl.hashCode()}.jpg"
            val file = File(cacheDir, fileName)
            
            // Si ya existe, no descargar de nuevo
            if (file.exists()) {
                Log.d("ImageCache", "⏭️ Imagen ya existe en caché: $imageType")
                return@withContext
            }
            
            Log.d("ImageCache", "📥 Descargando imagen para caché: $imageType")
            
            // Descargar imagen
            val url = URL(imageUrl)
            val connection = url.openConnection()
            connection.connectTimeout = 10000
            connection.readTimeout = 10000
            
            val inputStream = connection.getInputStream()
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            
            // Guardar en archivo local
            FileOutputStream(file).use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            
            Log.d("ImageCache", "✅ Imagen guardada en caché: $imageType (${file.length() / 1024}KB)")
            
        } catch (e: Exception) {
            Log.e("ImageCache", "❌ Error cacheando imagen $imageType: ${e.message}")
        }
    }
    
    /**
     * Limpia el caché de imágenes antiguas (más de 7 días)
     */
    fun cleanOldCache() {
        try {
            val sevenDaysAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
            
            cacheDir.listFiles()?.forEach { file ->
                if (file.lastModified() < sevenDaysAgo) {
                    file.delete()
                    Log.d("ImageCache", "🗑️ Imagen antigua eliminada: ${file.name}")
                }
            }
        } catch (e: Exception) {
            Log.e("ImageCache", "Error limpiando caché: ${e.message}")
        }
    }
    
    /**
     * Obtiene el tamaño total del caché en MB
     */
    fun getCacheSize(): Double {
        var size = 0L
        cacheDir.listFiles()?.forEach { file ->
            size += file.length()
        }
        return size / (1024.0 * 1024.0)
    }
}
