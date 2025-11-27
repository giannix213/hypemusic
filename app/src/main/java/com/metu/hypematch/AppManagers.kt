package com.metu.hypematch

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

/**
 * 🚀 FASE 2: CompositionLocal para Managers Estables
 * 
 * Problema: Los managers se recreaban en cada recomposición
 * Solución: Crear managers una sola vez y compartirlos globalmente
 * 
 * Beneficios:
 * - Managers se crean solo una vez
 * - Sin recreaciones innecesarias
 * - Mejor rendimiento
 * - Código más limpio
 */

// CompositionLocal para managers globales
val LocalAuthManager = compositionLocalOf<AuthManager> { 
    error("AuthManager not provided. Wrap your content with ProvideAppManagers") 
}

val LocalFirebaseManager = compositionLocalOf<FirebaseManager> { 
    error("FirebaseManager not provided. Wrap your content with ProvideAppManagers") 
}

val LocalThemeManager = compositionLocalOf<ThemeManager> { 
    error("ThemeManager not provided. Wrap your content with ProvideAppManagers") 
}

val LocalSongLikesManager = compositionLocalOf<SongLikesManager> { 
    error("SongLikesManager not provided. Wrap your content with ProvideAppManagers") 
}

val LocalFavoritesManager = compositionLocalOf<FavoritesManager> { 
    error("FavoritesManager not provided. Wrap your content with ProvideAppManagers") 
}

/**
 * Provider que crea los managers una sola vez y los comparte globalmente
 */
@Composable
fun ProvideAppManagers(
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    
    android.util.Log.d("AppManagers", "🔧 Creando managers globales...")
    
    // Crear managers una sola vez usando remember
    val authManager = remember { 
        android.util.Log.d("AppManagers", "✅ AuthManager creado")
        AuthManager(context) 
    }
    
    val firebaseManager = remember { 
        android.util.Log.d("AppManagers", "✅ FirebaseManager creado")
        FirebaseManager() 
    }
    
    val themeManager = remember { 
        android.util.Log.d("AppManagers", "✅ ThemeManager creado")
        ThemeManager(context) 
    }
    
    val songLikesManager = remember { 
        android.util.Log.d("AppManagers", "✅ SongLikesManager creado")
        SongLikesManager() 
    }
    
    val favoritesManager = remember { 
        android.util.Log.d("AppManagers", "✅ FavoritesManager creado")
        FavoritesManager(context) 
    }
    
    // Proveer managers a través de CompositionLocal
    CompositionLocalProvider(
        LocalAuthManager provides authManager,
        LocalFirebaseManager provides firebaseManager,
        LocalThemeManager provides themeManager,
        LocalSongLikesManager provides songLikesManager,
        LocalFavoritesManager provides favoritesManager
    ) {
        content()
    }
}

/**
 * Extension functions para acceso fácil
 */
@Composable
fun rememberAuthManager(): AuthManager = LocalAuthManager.current

@Composable
fun rememberFirebaseManager(): FirebaseManager = LocalFirebaseManager.current

@Composable
fun rememberThemeManager(): ThemeManager = LocalThemeManager.current

@Composable
fun rememberSongLikesManager(): SongLikesManager = LocalSongLikesManager.current

@Composable
fun rememberFavoritesManager(): FavoritesManager = LocalFavoritesManager.current
