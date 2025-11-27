package com.metu.hypematch

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import kotlinx.coroutines.launch

/**
 * AsyncImage optimizada con caché persistente y carga local
 * Elimina el retraso al cargar imágenes de perfil/portada
 */
@Composable
fun CachedAsyncImage(
    imageUrl: String,
    imageType: String, // "profile" o "cover"
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Crop
) {
    val context = LocalContext.current
    val imageCacheManager = remember { ImageCacheManager(context) }
    val scope = rememberCoroutineScope()
    
    // Intentar obtener archivo local primero
    val cachedFile = remember(imageUrl) {
        imageCacheManager.getCachedImageFile(imageUrl, imageType)
    }
    
    // Si no hay archivo local, cachear en background
    LaunchedEffect(imageUrl) {
        if (imageUrl.isNotEmpty() && cachedFile == null) {
            scope.launch {
                imageCacheManager.cacheImage(imageUrl, imageType)
            }
        }
    }
    
    // Usar archivo local si existe, sino usar URL con caché habilitado
    val imageModel = cachedFile ?: imageUrl
    
    AsyncImage(
        model = ImageRequest.Builder(context)
            .data(imageModel)
            .diskCachePolicy(CachePolicy.ENABLED) // Caché en disco habilitado
            .memoryCachePolicy(CachePolicy.ENABLED) // Caché en memoria habilitado
            .crossfade(false) // Sin crossfade para carga instantánea
            .build(),
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale
    )
}
