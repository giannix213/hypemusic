# 🚫 RESTRICCIÓN PARA USUARIOS INVITADOS (SIN PERFIL)

## 🎯 Objetivo
Restringir a los usuarios que ingresan con Google (sin crear perfil completo) para que solo puedan ver contenido pero NO participar en concursos ni subir videos.

---

## 📋 ESTRATEGIA

### Tipos de Usuarios:

**Usuario Completo (Puede participar):**
- ✅ Tiene documento en Firestore `users/{userId}`
- ✅ Tiene `username` configurado
- ✅ Puede subir videos
- ✅ Puede participar en concursos
- ✅ Puede dar likes y comentar

**Usuario Invitado/Google (Solo visualización):**
- ❌ NO tiene documento en Firestore
- ❌ Solo autenticado con Google
- ❌ NO puede subir videos
- ❌ NO puede participar en concursos
- ✅ Puede ver videos
- ✅ Puede ver concursos
- ⚠️ Debe crear perfil para participar

---

## 🔧 IMPLEMENTACIÓN

### 1. Función de Verificación en FirebaseManager

```kotlin
// Verificar si el usuario tiene perfil completo
suspend fun hasCompleteProfile(userId: String): Boolean {
    return try {
        if (userId.isEmpty()) return false
        
        val doc = firestore.collection("users")
            .document(userId)
            .get()
            .await()
        
        // Verificar que existe y tiene username
        val exists = doc.exists()
        val hasUsername = doc.getString("username")?.isNotEmpty() == true
        
        android.util.Log.d("FirebaseManager", "👤 Usuario $userId - Perfil completo: ${exists && hasUsername}")
        
        exists && hasUsername
    } catch (e: Exception) {
        android.util.Log.e("FirebaseManager", "Error verificando perfil: ${e.message}")
        false
    }
}
```

### 2. Modificar ContestDetailScreen

Agregar verificación antes de permitir grabar:

```kotlin
@Composable
fun ContestDetailScreen(
    contest: Contest,
    onBack: () -> Unit,
    onRecordVideo: () -> Unit,
    onViewGallery: () -> Unit = {}
) {
    val context = LocalContext.current
    val firebaseManager = remember { FirebaseManager() }
    val scope = rememberCoroutineScope()
    
    // Estado para verificar perfil
    var hasProfile by remember { mutableStateOf(true) }
    var isCheckingProfile by remember { mutableStateOf(true) }
    var showProfileRequiredDialog by remember { mutableStateOf(false) }
    
    // Verificar perfil al cargar
    LaunchedEffect(Unit) {
        scope.launch {
            try {
                val userId = getCurrentUserId(context) // Función helper
                hasProfile = firebaseManager.hasCompleteProfile(userId)
                isCheckingProfile = false
            } catch (e: Exception) {
                hasProfile = false
                isCheckingProfile = false
            }
        }
    }
    
    // ... resto del código ...
    
    // Botón de grabar video
    Button(
        onClick = {
            if (hasProfile) {
                onRecordVideo()
            } else {
                showProfileRequiredDialog = true
            }
        },
        // ... resto de propiedades ...
    ) {
        Text("GRABAR VIDEO")
    }
    
    // Diálogo de perfil requerido
    if (showProfileRequiredDialog) {
        AlertDialog(
            onDismissRequest = { showProfileRequiredDialog = false },
            title = { Text("Perfil Requerido") },
            text = {
                Text(
                    "Para participar en concursos necesitas crear tu perfil.\n\n" +
                    "Ve a Configuración → Crear Perfil"
                )
            },
            confirmButton = {
                TextButton(onClick = { showProfileRequiredDialog = false }) {
                    Text("Entendido")
                }
            }
        )
    }
}
```

### 3. Modificar LiveCatalogScreen

Deshabilitar botón "Iniciar Live" para usuarios sin perfil:

```kotlin
// En LiveCatalogScreen, al final:
Button(
    onClick = {
        scope.launch {
            val userId = getCurrentUserId(context)
            val hasProfile = firebaseManager.hasCompleteProfile(userId)
            
            if (hasProfile) {
                onStartLive()
            } else {
                // Mostrar diálogo
                showProfileRequiredDialog = true
            }
        }
    },
    enabled = hasProfile, // Deshabilitar si no tiene perfil
    // ... resto de propiedades ...
) {
    Text("INICIAR TRANSMISIÓN EN VIVO")
}
```

---

## 🎨 EXPERIENCIA DE USUARIO

### Usuario SIN Perfil (Invitado):

```
1. Abre la app con Google
2. Ve el carrusel de videos ✅
3. Puede dar like y comentar ✅
4. Swipe izquierda → Catálogo ✅
5. Click en concurso → Detalles ✅
6. Click en "GRABAR VIDEO" → ⚠️ DIÁLOGO:

   ┌─────────────────────────────────┐
   │     Perfil Requerido            │
   ├─────────────────────────────────┤
   │                                 │
   │ Para participar en concursos    │
   │ necesitas crear tu perfil.      │
   │                                 │
   │ Ve a Configuración →            │
   │ Crear Perfil                    │
   │                                 │
   │         [Entendido]             │
   └─────────────────────────────────┘

7. Botón "INICIAR LIVE" deshabilitado (gris)
```

### Usuario CON Perfil:

```
1. Abre la app
2. Ve el carrusel de videos ✅
3. Puede dar like y comentar ✅
4. Swipe izquierda → Catálogo ✅
5. Click en concurso → Detalles ✅
6. Click en "GRABAR VIDEO" → ✅ Abre cámara
7. Graba y sube video ✅
8. Botón "INICIAR LIVE" habilitado ✅
```

---

## 📱 LUGARES DONDE APLICAR RESTRICCIÓN

### 1. ContestDetailScreen
- ✅ Botón "GRABAR VIDEO"
- ✅ Mostrar diálogo si no tiene perfil

### 2. LiveCatalogScreen
- ✅ Botón "INICIAR TRANSMISIÓN EN VIVO"
- ✅ Deshabilitar si no tiene perfil

### 3. LiveScreenNew (opcional)
- ⚠️ Botón "LIVE" en esquina superior
- ⚠️ Mostrar diálogo si no tiene perfil

### 4. Perfil de Usuario
- ✅ Botón "Subir Historia"
- ✅ Botón "Subir Video"
- ✅ Mostrar mensaje si no tiene perfil

---

## 🔍 VERIFICACIÓN

### Cómo probar:

**Escenario 1: Usuario nuevo con Google**
```
1. Desinstala la app
2. Instala de nuevo
3. Inicia sesión con Google
4. NO crees perfil
5. Intenta grabar video → Debe mostrar diálogo
6. Intenta iniciar live → Botón deshabilitado
```

**Escenario 2: Usuario con perfil**
```
1. Inicia sesión
2. Crea perfil (username, etc.)
3. Intenta grabar video → Debe abrir cámara
4. Intenta iniciar live → Debe funcionar
```

---

## 💡 MEJORAS OPCIONALES

### 1. Indicador Visual
Agregar badge "Invitado" en el perfil:

```kotlin
if (!hasProfile) {
    Surface(
        color = PopArtColors.Yellow,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(
            "👤 INVITADO",
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
```

### 2. Banner Promocional
Mostrar banner en la parte superior:

```kotlin
if (!hasProfile) {
    Surface(
        color = PopArtColors.Pink.copy(alpha = 0.9f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("⚠️", fontSize = 20.sp)
            Spacer(Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Crea tu perfil para participar",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "Sube videos y participa en concursos",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
            TextButton(onClick = { /* Navegar a crear perfil */ }) {
                Text("CREAR", color = Color.White)
            }
        }
    }
}
```

### 3. Contador de Funciones Bloqueadas
Mostrar cuántas funciones están bloqueadas:

```kotlin
Text(
    "🔒 3 funciones bloqueadas sin perfil",
    fontSize = 12.sp,
    color = Color.Gray
)
```

---

## 🎯 BENEFICIOS

### Para la App:
- ✅ Incentiva a crear perfiles completos
- ✅ Reduce spam y contenido de baja calidad
- ✅ Mejora la calidad de la comunidad
- ✅ Facilita moderación

### Para el Usuario:
- ✅ Puede explorar antes de comprometerse
- ✅ Entiende el valor de crear perfil
- ✅ Experiencia clara y transparente
- ✅ No se siente bloqueado completamente

---

## 📊 MÉTRICAS A MONITOREAR

1. **Tasa de conversión:**
   - % de usuarios invitados que crean perfil

2. **Tiempo hasta conversión:**
   - Cuánto tardan en crear perfil

3. **Intentos bloqueados:**
   - Cuántas veces intentan participar sin perfil

4. **Abandono:**
   - % que abandonan al ver la restricción

---

## ✅ CHECKLIST DE IMPLEMENTACIÓN

- [ ] Agregar función `hasCompleteProfile()` en FirebaseManager
- [ ] Modificar ContestDetailScreen con verificación
- [ ] Agregar diálogo de "Perfil Requerido"
- [ ] Modificar LiveCatalogScreen
- [ ] Deshabilitar botón "INICIAR LIVE"
- [ ] Agregar indicadores visuales (opcional)
- [ ] Probar con usuario nuevo
- [ ] Probar con usuario con perfil
- [ ] Verificar logs
- [ ] Documentar cambios

---

**Fecha:** 26/11/2025
**Propósito:** Restringir participación a usuarios sin perfil
**Impacto:** Mejora calidad de contenido y comunidad
**Dificultad:** Media
**Tiempo estimado:** 30-45 minutos

---

## 🚀 PRÓXIMO PASO

1. Implementar función `hasCompleteProfile()` en FirebaseManager
2. Modificar ContestDetailScreen
3. Probar con usuario invitado
4. Ajustar según feedback

**Estado:** ✅ DISEÑO COMPLETO - LISTO PARA IMPLEMENTAR
