package com.metu.hypematch

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy

/**
 * 🚀 FASE 2 & 3: Configuración optimizada de ImageLoader
 * 
 * Optimizaciones:
 * - Hardware Bitmaps para mejor rendimiento
 * - Caché de memoria (25% de RAM)
 * - Caché de disco (100MB)
 * - Decodificador nativo de Android
 */
object ImageLoaderConfig {
    fun createImageLoader(context: Context): ImageLoader {
        android.util.Log.d("ImageLoaderConfig", "🖼️ Creando ImageLoader optimizado")
        
        return ImageLoader.Builder(context)
            .crossfade(true)
            // ✅ Hardware Bitmaps (más rápidos y eficientes)
            .allowHardware(true)
            .bitmapConfig(Bitmap.Config.HARDWARE)
            // ✅ Caché de memoria (25% de RAM disponible)
            .memoryCache {
                MemoryCache.Builder(context)
                    .maxSizePercent(0.25)
                    .build()
            }
            // ✅ Caché de disco (100MB)
            .diskCache {
                DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizeBytes(100 * 1024 * 1024) // 100MB
                    .build()
            }
            // ✅ Políticas de caché agresivas
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            // ✅ Decodificador nativo de Android (más rápido)
            .components {
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
            .also {
                android.util.Log.d("ImageLoaderConfig", "✅ ImageLoader optimizado creado")
            }
    }
}
