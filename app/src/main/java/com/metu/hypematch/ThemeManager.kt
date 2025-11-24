package com.metu.hypematch

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension para DataStore
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "theme_preferences")

class ThemeManager(private val context: Context) {
    private val DARK_MODE_KEY = booleanPreferencesKey("dark_mode")
    
    // Flow para observar cambios en el tema
    val isDarkModeFlow: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[DARK_MODE_KEY] ?: false // Por defecto modo oscuro (false = tema actual negro)
        }
    
    // Guardar preferencia de tema
    suspend fun setDarkMode(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[DARK_MODE_KEY] = isDark
        }
    }
    
    // Alternar tema
    suspend fun toggleTheme() {
        context.dataStore.edit { preferences ->
            val current = preferences[DARK_MODE_KEY] ?: false
            preferences[DARK_MODE_KEY] = !current
        }
    }
}

// Colores para tema claro
object LightThemeColors {
    val Background = androidx.compose.ui.graphics.Color(0xFFF5F5F5)
    val Surface = androidx.compose.ui.graphics.Color.White
    val Primary = PopArtColors.Yellow
    val Secondary = PopArtColors.Pink
    val Text = androidx.compose.ui.graphics.Color(0xFF1A1A1A)
    val TextSecondary = androidx.compose.ui.graphics.Color(0xFF666666)
    val Border = androidx.compose.ui.graphics.Color(0xFFE0E0E0)
}

// Colores para tema oscuro (los actuales)
object DarkThemeColors {
    val Background = PopArtColors.Black
    val Surface = androidx.compose.ui.graphics.Color(0xFF1A1A1A)
    val Primary = PopArtColors.Yellow
    val Secondary = PopArtColors.Pink
    val Text = PopArtColors.White
    val TextSecondary = androidx.compose.ui.graphics.Color(0xFFB0B0B0)
    val Border = androidx.compose.ui.graphics.Color(0xFF333333)
}

// Clase para acceder a los colores según el tema
data class AppColors(
    val background: androidx.compose.ui.graphics.Color,
    val surface: androidx.compose.ui.graphics.Color,
    val primary: androidx.compose.ui.graphics.Color,
    val secondary: androidx.compose.ui.graphics.Color,
    val text: androidx.compose.ui.graphics.Color,
    val textSecondary: androidx.compose.ui.graphics.Color,
    val border: androidx.compose.ui.graphics.Color
)

@Composable
fun getAppColors(isDarkMode: Boolean): AppColors {
    return if (isDarkMode) {
        AppColors(
            background = DarkThemeColors.Background,
            surface = DarkThemeColors.Surface,
            primary = DarkThemeColors.Primary,
            secondary = DarkThemeColors.Secondary,
            text = DarkThemeColors.Text,
            textSecondary = DarkThemeColors.TextSecondary,
            border = DarkThemeColors.Border
        )
    } else {
        AppColors(
            background = LightThemeColors.Background,
            surface = LightThemeColors.Surface,
            primary = LightThemeColors.Primary,
            secondary = LightThemeColors.Secondary,
            text = LightThemeColors.Text,
            textSecondary = LightThemeColors.TextSecondary,
            border = LightThemeColors.Border
        )
    }
}
