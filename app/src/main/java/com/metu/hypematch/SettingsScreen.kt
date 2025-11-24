package com.metu.hypematch

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    isDarkMode: Boolean = false,
    colors: AppColors = getAppColors(false)
) {
    val context = LocalContext.current
    val authManager = remember { AuthManager(context) }
    val firebaseManager = remember { FirebaseManager() }
    val scope = rememberCoroutineScope()
    
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showNotificationsDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showPermissionsDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.surface)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        Icons.Default.ArrowBack,
                        contentDescription = "Volver",
                        tint = colors.text
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    "Configuración",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = colors.text
                )
            }
            
            // Lista de opciones
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    Text(
                        "CUENTA",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = colors.textSecondary,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                
                item {
                    SettingsItem(
                        icon = Icons.Default.Notifications,
                        title = "Notificaciones",
                        subtitle = "Gestiona tus notificaciones",
                        colors = colors,
                        onClick = { showNotificationsDialog = true }
                    )
                }
                
                item {
                    SettingsItem(
                        icon = Icons.Default.Lock,
                        title = "Privacidad",
                        subtitle = "Controla tu privacidad",
                        colors = colors,
                        onClick = { showPrivacyDialog = true }
                    )
                }
                
                item {
                    SettingsItem(
                        icon = Icons.Default.Settings,
                        title = "Permisos del Dispositivo",
                        subtitle = "Cámara, micrófono, almacenamiento",
                        colors = colors,
                        onClick = { showPermissionsDialog = true }
                    )
                }
                
                item {
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "ZONA PELIGROSA",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = PopArtColors.Pink,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                }
                
                item {
                    SettingsItem(
                        icon = Icons.Default.Delete,
                        title = "Eliminar Cuenta",
                        subtitle = "Elimina permanentemente tu cuenta",
                        colors = colors,
                        iconTint = PopArtColors.Pink,
                        onClick = { showDeleteAccountDialog = true }
                    )
                }
            }
        }
    }
    
    // Diálogo de Notificaciones
    if (showNotificationsDialog) {
        NotificationsDialog(
            onDismiss = { showNotificationsDialog = false },
            colors = colors
        )
    }
    
    // Diálogo de Privacidad
    if (showPrivacyDialog) {
        PrivacyDialog(
            onDismiss = { showPrivacyDialog = false },
            colors = colors
        )
    }
    
    // Diálogo de Permisos
    if (showPermissionsDialog) {
        PermissionsDialog(
            onDismiss = { showPermissionsDialog = false },
            colors = colors
        )
    }
    
    // Diálogo de Eliminar Cuenta
    if (showDeleteAccountDialog) {
        DeleteAccountDialog(
            onDismiss = { showDeleteAccountDialog = false },
            onConfirm = {
                isDeleting = true
                scope.launch {
                    try {
                        val userId = authManager.getUserId()
                        if (userId != null) {
                            android.util.Log.d("SettingsScreen", "🗑️ Iniciando eliminación de cuenta: $userId")
                            
                            // 1. Primero eliminar todos los datos del usuario en Firebase
                            firebaseManager.deleteUserAccount(userId)
                            android.util.Log.d("SettingsScreen", "✅ Datos de Firebase eliminados")
                            
                            // 2. Luego eliminar la cuenta de autenticación
                            authManager.deleteAccount()
                            android.util.Log.d("SettingsScreen", "✅ Cuenta de autenticación eliminada")
                            
                            // 3. Cerrar sesión completamente
                            authManager.signOut()
                            android.util.Log.d("SettingsScreen", "✅ Sesión cerrada")
                            
                            isDeleting = false
                            showDeleteAccountDialog = false
                            
                            // 4. Redirigir a la pantalla de inicio
                            onLogout()
                        } else {
                            android.util.Log.e("SettingsScreen", "❌ No hay usuario autenticado")
                            isDeleting = false
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("SettingsScreen", "❌ Error eliminando cuenta: ${e.message}", e)
                        isDeleting = false
                        // Intentar cerrar sesión de todas formas
                        try {
                            authManager.signOut()
                            onLogout()
                        } catch (ex: Exception) {
                            android.util.Log.e("SettingsScreen", "Error en signOut: ${ex.message}")
                        }
                    }
                }
            },
            isDeleting = isDeleting,
            colors = colors
        )
    }
}

@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    colors: AppColors,
    iconTint: androidx.compose.ui.graphics.Color = colors.text,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = colors.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text
                )
                Text(
                    subtitle,
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
            }
            Icon(
                Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
fun NotificationsDialog(
    onDismiss: () -> Unit,
    colors: AppColors
) {
    var pushNotifications by remember { mutableStateOf(true) }
    var emailNotifications by remember { mutableStateOf(true) }
    var newFollowers by remember { mutableStateOf(true) }
    var newLikes by remember { mutableStateOf(true) }
    var newComments by remember { mutableStateOf(true) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Notificaciones",
                fontWeight = FontWeight.Black,
                color = colors.text
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SwitchItem(
                    title = "Notificaciones Push",
                    checked = pushNotifications,
                    onCheckedChange = { pushNotifications = it },
                    colors = colors
                )
                SwitchItem(
                    title = "Notificaciones por Email",
                    checked = emailNotifications,
                    onCheckedChange = { emailNotifications = it },
                    colors = colors
                )
                Divider(color = colors.border)
                Text(
                    "Actividad",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text
                )
                SwitchItem(
                    title = "Nuevos seguidores",
                    checked = newFollowers,
                    onCheckedChange = { newFollowers = it },
                    colors = colors
                )
                SwitchItem(
                    title = "Me gusta",
                    checked = newLikes,
                    onCheckedChange = { newLikes = it },
                    colors = colors
                )
                SwitchItem(
                    title = "Comentarios",
                    checked = newComments,
                    onCheckedChange = { newComments = it },
                    colors = colors
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
            ) {
                Text("Guardar", color = PopArtColors.Black, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = colors.background
    )
}

@Composable
fun PrivacyDialog(
    onDismiss: () -> Unit,
    colors: AppColors
) {
    var privateAccount by remember { mutableStateOf(false) }
    var showActivity by remember { mutableStateOf(true) }
    var allowMessages by remember { mutableStateOf(true) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Privacidad",
                fontWeight = FontWeight.Black,
                color = colors.text
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SwitchItem(
                    title = "Cuenta Privada",
                    subtitle = "Solo tus seguidores pueden ver tu contenido",
                    checked = privateAccount,
                    onCheckedChange = { privateAccount = it },
                    colors = colors
                )
                SwitchItem(
                    title = "Mostrar Actividad",
                    subtitle = "Otros pueden ver cuando estás en línea",
                    checked = showActivity,
                    onCheckedChange = { showActivity = it },
                    colors = colors
                )
                SwitchItem(
                    title = "Permitir Mensajes",
                    subtitle = "Recibe mensajes de otros usuarios",
                    checked = allowMessages,
                    onCheckedChange = { allowMessages = it },
                    colors = colors
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
            ) {
                Text("Guardar", color = PopArtColors.Black, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = colors.background
    )
}

@Composable
fun PermissionsDialog(
    onDismiss: () -> Unit,
    colors: AppColors
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                "Permisos del Dispositivo",
                fontWeight = FontWeight.Black,
                color = colors.text
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PermissionItem(
                    icon = Icons.Default.AccountCircle,
                    title = "Cámara",
                    description = "Para grabar videos y tomar fotos",
                    colors = colors
                )
                PermissionItem(
                    icon = Icons.Default.Info,
                    title = "Micrófono",
                    description = "Para grabar audio en videos",
                    colors = colors
                )
                PermissionItem(
                    icon = Icons.Default.Star,
                    title = "Almacenamiento",
                    description = "Para guardar y subir contenido",
                    colors = colors
                )
                
                Text(
                    "💡 Puedes cambiar estos permisos en la configuración de tu dispositivo",
                    fontSize = 12.sp,
                    color = colors.textSecondary,
                    lineHeight = 16.sp
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = colors.primary)
            ) {
                Text("Entendido", color = PopArtColors.Black, fontWeight = FontWeight.Bold)
            }
        },
        containerColor = colors.background
    )
}

@Composable
fun DeleteAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    isDeleting: Boolean,
    colors: AppColors
) {
    AlertDialog(
        onDismissRequest = if (!isDeleting) onDismiss else { {} },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Warning,
                    contentDescription = null,
                    tint = PopArtColors.Pink,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    "¿Eliminar Cuenta?",
                    fontWeight = FontWeight.Black,
                    color = PopArtColors.Pink
                )
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    "Esta acción es permanente y no se puede deshacer.",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text
                )
                Text(
                    "Se eliminará:",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = colors.text
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("• Tu perfil y datos personales", fontSize = 14.sp, color = colors.text)
                    Text("• Todas tus canciones y videos", fontSize = 14.sp, color = colors.text)
                    Text("• Tus seguidores y seguidos", fontSize = 14.sp, color = colors.text)
                    Text("• Tus likes y comentarios", fontSize = 14.sp, color = colors.text)
                    Text("• Todas tus historias", fontSize = 14.sp, color = colors.text)
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "¿Estás seguro de que quieres continuar?",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = PopArtColors.Pink
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = PopArtColors.Pink),
                enabled = !isDeleting
            ) {
                if (isDeleting) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = PopArtColors.White
                    )
                } else {
                    Text("Eliminar", color = PopArtColors.White, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isDeleting
            ) {
                Text("Cancelar", color = colors.text)
            }
        },
        containerColor = colors.background
    )
}

@Composable
fun SwitchItem(
    title: String,
    subtitle: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    colors: AppColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = colors.text
            )
            if (subtitle != null) {
                Text(
                    subtitle,
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
            }
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = colors.primary,
                checkedTrackColor = colors.primary.copy(alpha = 0.5f),
                uncheckedThumbColor = colors.textSecondary,
                uncheckedTrackColor = colors.border
            )
        )
    }
}

@Composable
fun PermissionItem(
    icon: ImageVector,
    title: String,
    description: String,
    colors: AppColors
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icon,
            contentDescription = title,
            tint = colors.primary,
            modifier = Modifier.size(32.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column {
            Text(
                title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = colors.text
            )
            Text(
                description,
                fontSize = 12.sp,
                color = colors.textSecondary
            )
        }
    }
}
