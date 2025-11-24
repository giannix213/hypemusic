package com.metu.hypematch

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

// Componente del Header con logo y menú hamburguesa
@Composable
fun HypeHeader(
    onMenuClick: () -> Unit,
    isDarkMode: Boolean,
    colors: AppColors,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(colors.background)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icono de menú hamburguesa
        IconButton(
            onClick = onMenuClick,
            modifier = Modifier
                .size(48.dp)
                .background(colors.surface, RoundedCornerShape(12.dp))
                .border(2.dp, colors.border, RoundedCornerShape(12.dp))
        ) {
            Icon(
                Icons.Default.Menu,
                contentDescription = "Menú",
                tint = colors.text,
                modifier = Modifier.size(28.dp)
            )
        }
        
        Spacer(Modifier.width(12.dp))
        
        // Logo y nombre
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedMusicLogo(
                modifier = Modifier
                    .size(50.dp)
                    .padding(end = 12.dp)
            )
            Text(
                "HYPE",
                fontSize = 28.sp,
                fontWeight = FontWeight.Black,
                color = colors.primary
            )
        }
    }
}

// Navigation Drawer moderno
@Composable
fun ModernNavigationDrawer(
    drawerState: DrawerState,
    isDarkMode: Boolean,
    onThemeToggle: () -> Unit,
    onLogout: () -> Unit,
    content: @Composable () -> Unit
) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val firebaseManager = remember { FirebaseManager() }
    val scope = rememberCoroutineScope()
    
    val userId = authManager.getUserId() ?: ""
    var username by remember { mutableStateOf("Usuario") }
    var isArtist by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }
    
    // Cargar perfil del usuario
    LaunchedEffect(userId) {
        if (userId.isNotEmpty()) {
            val profile = firebaseManager.getUserProfile(userId)
            username = profile?.username ?: "Usuario"
            isArtist = profile?.isArtist ?: false
        }
    }
    
    val colors = getAppColors(isDarkMode)
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(300.dp),
                drawerContainerColor = colors.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    // Header del drawer con perfil
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Avatar
                        Box(
                            modifier = Modifier
                                .size(80.dp)
                                .clip(CircleShape)
                                .background(colors.primary)
                                .border(3.dp, colors.border, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                username.firstOrNull()?.uppercase() ?: "U",
                                fontSize = 36.sp,
                                fontWeight = FontWeight.Black,
                                color = PopArtColors.Black
                            )
                        }
                        
                        Spacer(Modifier.height(12.dp))
                        
                        Text(
                            username,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = colors.text
                        )
                        
                        Text(
                            if (isArtist) "🎤 Artista" else "🎧 Espectador",
                            fontSize = 14.sp,
                            color = colors.textSecondary
                        )
                    }
                    
                    Divider(
                        color = colors.border,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    
                    // Opciones del menú
                    DrawerMenuItem(
                        icon = Icons.Default.Person,
                        title = "Mi Perfil",
                        colors = colors,
                        onClick = {
                            scope.launch { drawerState.close() }
                        }
                    )
                    
                    DrawerMenuItem(
                        icon = Icons.Default.Favorite,
                        title = "Mis Favoritos",
                        colors = colors,
                        onClick = {
                            scope.launch { drawerState.close() }
                        }
                    )
                    
                    DrawerMenuItem(
                        icon = Icons.Default.Settings,
                        title = "Configuración",
                        colors = colors,
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                showSettings = true
                            }
                        }
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Divider(
                        color = colors.border,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    // Toggle de tema
                    ThemeToggleItem(
                        isDarkMode = isDarkMode,
                        onToggle = onThemeToggle,
                        colors = colors
                    )
                    
                    Spacer(Modifier.height(16.dp))
                    
                    Divider(
                        color = colors.border,
                        thickness = 1.dp,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    
                    // Botón de cambio de rol
                    RoleToggleItem(
                        isArtist = isArtist,
                        onToggle = {
                            scope.launch {
                                try {
                                    val newRole = !isArtist
                                    firebaseManager.updateUserRole(userId, newRole)
                                    isArtist = newRole
                                    android.util.Log.d("Drawer", "✅ Rol cambiado a: ${if (newRole) "Artista" else "Espectador"}")
                                } catch (e: Exception) {
                                    android.util.Log.e("Drawer", "❌ Error cambiando rol: ${e.message}")
                                }
                            }
                        },
                        colors = colors
                    )
                    
                    Spacer(Modifier.weight(1f))
                    
                    // Botón de cerrar sesión
                    Button(
                        onClick = {
                            scope.launch {
                                drawerState.close()
                                authManager.signOut()
                                onLogout()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.secondary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.ExitToApp,
                            contentDescription = "Cerrar sesión",
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Cerrar Sesión",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    // Versión de la app
                    Text(
                        "Hype v1.0",
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }
            }
        },
        content = {
            if (showSettings) {
                SettingsScreen(
                    onBack = { showSettings = false },
                    onLogout = onLogout,
                    isDarkMode = isDarkMode,
                    colors = colors
                )
            } else {
                content()
            }
        }
    )
}

// Item del menú drawer
@Composable
fun DrawerMenuItem(
    icon: ImageVector,
    title: String,
    colors: AppColors,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = title,
            tint = colors.text,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Text(
            title,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = colors.text
        )
    }
}

// Toggle de tema con animación
@Composable
fun ThemeToggleItem(
    isDarkMode: Boolean,
    onToggle: () -> Unit,
    colors: AppColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.background)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Default.Settings,
                contentDescription = "Tema",
                tint = colors.text,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    "Tema",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.text
                )
                Text(
                    if (isDarkMode) "Modo Oscuro" else "Modo Claro",
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
            }
        }
        
        // Switch animado
        Switch(
            checked = isDarkMode,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.primary,
                checkedTrackColor = colors.primary.copy(alpha = 0.5f),
                uncheckedThumbColor = colors.textSecondary,
                uncheckedTrackColor = colors.border
            )
        )
    }
}

// Toggle de rol (Espectador/Artista)
@Composable
fun RoleToggleItem(
    isArtist: Boolean,
    onToggle: () -> Unit,
    colors: AppColors
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.background)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                if (isArtist) Icons.Default.Star else Icons.Default.Person,
                contentDescription = "Rol",
                tint = colors.text,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column {
                Text(
                    "Modo de Usuario",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = colors.text
                )
                Text(
                    if (isArtist) "🎤 Artista" else "🎧 Espectador",
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
            }
        }
        
        // Switch animado
        Switch(
            checked = isArtist,
            onCheckedChange = { onToggle() },
            colors = SwitchDefaults.colors(
                checkedThumbColor = PopArtColors.Pink,
                checkedTrackColor = PopArtColors.Pink.copy(alpha = 0.5f),
                uncheckedThumbColor = PopArtColors.Cyan,
                uncheckedTrackColor = PopArtColors.Cyan.copy(alpha = 0.5f)
            )
        )
    }
}
