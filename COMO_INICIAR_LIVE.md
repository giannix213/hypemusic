# 📺 Cómo Iniciar una Transmisión en Vivo

## 🎯 Resumen

Para iniciar un Live, necesitas agregar un botón que abra `LiveLauncherScreen`. Aquí te explico cómo:

## 📍 Opción 1: Agregar botón en la pantalla principal

En `LiveScreenNew.kt`, agrega un estado y un botón:

### 1. Agregar el estado (línea ~133, después de `showLiveStreams`):

```kotlin
var showLiveLauncher by remember { mutableStateOf(false) }
```

### 2. Agregar el botón flotante en la UI

Busca donde está el carrusel de videos y agrega un FloatingActionButton:

```kotlin
// Dentro del Box del carrusel, agrega:
FloatingActionButton(
    onClick = { showLiveLauncher = true },
    modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(16.dp),
    containerColor = PopArtColors.Pink
) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("🎥", fontSize = 24.sp)
        Spacer(Modifier.width(8.dp))
        Text(
            "Ir Live",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
```

### 3. Mostrar LiveLauncherScreen cuando se active

Al final de la función `LiveScreenNew`, antes del último `}`, agrega:

```kotlin
// Mostrar LiveLauncher cuando se active
if (showLiveLauncher) {
    LiveLauncherScreen(
        onClose = { showLiveLauncher = false }
    )
}
```

## 📍 Opción 2: Usar el botón existente del catálogo

Si ya tienes un botón "Iniciar Live" en el catálogo de concursos, solo necesitas cambiar:

```kotlin
onStartLive = {
    showLiveLauncher = true
}
```

## 🎬 Flujo Completo

1. **Usuario hace clic en "Ir Live"**
   - Se abre `LiveLauncherScreen`

2. **LiveLauncherScreen automáticamente:**
   - Muestra "Preparando Live..."
   - Llama a la Cloud Function para obtener token de Agora
   - Crea la sesión en Firestore

3. **Cuando está listo:**
   - Se abre `LiveRecordingScreen`
   - Solicita permisos de cámara y audio
   - Inicia la transmisión con Agora

4. **Durante la transmisión:**
   - Badge "LIVE" en rojo
   - Contador de espectadores
   - Botones para cambiar cámara y mutear

5. **Al finalizar:**
   - Click en X para cerrar
   - Se actualiza Firestore
   - Vuelve a la pantalla anterior

## 🔧 Código Completo para Agregar

Aquí está el código completo que necesitas agregar a `LiveScreenNew.kt`:

```kotlin
// 1. En la sección de estados (línea ~133)
var showLiveLauncher by remember { mutableStateOf(false) }

// 2. En la UI del carrusel (dentro del Box principal)
FloatingActionButton(
    onClick = { showLiveLauncher = true },
    modifier = Modifier
        .align(Alignment.BottomEnd)
        .padding(16.dp),
    containerColor = PopArtColors.Pink
) {
    Row(
        modifier = Modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("🎥", fontSize = 24.sp)
        Spacer(Modifier.width(8.dp))
        Text(
            "Ir Live",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

// 3. Al final de LiveScreenNew, antes del último }
if (showLiveLauncher) {
    LiveLauncherScreen(
        onClose = { showLiveLauncher = false }
    )
}
```

## 🎨 Personalización

Puedes personalizar el botón:

- **Posición**: Cambia `Alignment.BottomEnd` a `Alignment.TopStart`, etc.
- **Color**: Cambia `PopArtColors.Pink` a otro color
- **Icono**: Cambia "🎥" por otro emoji o usa un Icon
- **Texto**: Cambia "Ir Live" por otro texto

## 🧪 Probar

1. Ejecuta la app
2. Ve a la pantalla de videos
3. Busca el botón "Ir Live"
4. Haz clic
5. Concede permisos
6. ¡Deberías ver tu cámara transmitiendo!

## 📝 Notas

- Los permisos de cámara y audio se solicitan automáticamente
- El token de Agora se genera automáticamente desde la Cloud Function
- La sesión se guarda en Firestore automáticamente
- Los espectadores podrán ver tu Live en tiempo real (cuando implementes la pantalla de viewers)

---

¿Quieres que agregue el botón por ti o prefieres hacerlo tú mismo?
