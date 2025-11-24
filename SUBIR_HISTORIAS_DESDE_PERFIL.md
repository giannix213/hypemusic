# 📸 Subir Historias desde el Perfil

## ✅ Funcionalidad Implementada

Ahora puedes subir historias directamente desde tu perfil haciendo clic en tu foto de perfil.

## Cómo Funciona

### 1. Indicador Visual
- La foto de perfil tiene un **borde amarillo** y un **botón "+"** amarillo en la esquina
- Esto indica que puedes agregar una historia

### 2. Al Hacer Clic en la Foto de Perfil
Se abre un diálogo con 3 opciones:

#### 📷 **Tomar Foto**
- Abre la cámara del dispositivo
- Toma una foto
- Se sube automáticamente como historia

#### 🖼️ **Seleccionar de Galería**
- Abre la galería de fotos
- Selecciona una imagen existente
- Se sube como historia

#### ✏️ **Cambiar Foto de Perfil**
- Permite cambiar tu foto de perfil
- No crea una historia

## Flujo de Usuario

```
1. Usuario abre su Perfil
2. Hace clic en su foto de perfil
3. Selecciona "Tomar Foto" o "Seleccionar de Galería"
4. [Si eligió Tomar Foto] → Se abre la cámara → Toma foto
5. [Si eligió Galería] → Selecciona imagen
6. La historia se sube automáticamente
7. Aparece en "Tu Música" para quienes te dieron like
```

## Componentes Agregados

### En `ProfileScreen.kt`:

#### Estados:
```kotlin
var showStoryCamera by remember { mutableStateOf(false) }
var showStoryOptions by remember { mutableStateOf(false) }
```

#### Launcher para Historia:
```kotlin
val storyImageLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
) { uri: Uri? ->
    // Sube la imagen como historia
}
```

#### Componente StoryCamera:
```kotlin
@Composable
fun StoryCamera(
    onBack: () -> Unit,
    onPhotoTaken: (Uri) -> Unit
)
```

#### Diálogo de Opciones:
- Tomar Foto (amarillo)
- Seleccionar de Galería (cyan)
- Cambiar Foto de Perfil (rosa)

## Configuración Necesaria

### AndroidManifest.xml
Agregado FileProvider para compartir archivos:
```xml
<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

### file_paths.xml
Archivo de configuración para rutas de archivos:
```xml
<paths>
    <cache-path name="cache" path="." />
    <external-path name="external" path="." />
    <files-path name="files" path="." />
</paths>
```

## Permisos Requeridos

Ya están en el AndroidManifest:
- ✅ `CAMERA` - Para tomar fotos
- ✅ `READ_EXTERNAL_STORAGE` - Para leer galería
- ✅ `WRITE_EXTERNAL_STORAGE` - Para guardar fotos temporales

## Características

### ✅ Subida Automática
- La historia se sube automáticamente a Firebase
- Muestra progreso de subida
- Notifica cuando termina

### ✅ Integración con Sistema de Historias
- Las historias aparecen en "Tu Música"
- Solo las ven usuarios que te dieron like
- Expiran en 24 horas

### ✅ UI Intuitiva
- Borde amarillo indica que puedes agregar historia
- Botón "+" visible
- Diálogo claro con opciones

## Ejemplo de Uso

### Usuario Artista:
1. Sube una canción nueva
2. Va a su Perfil
3. Hace clic en su foto
4. Selecciona "Tomar Foto"
5. Toma una foto promocionando su nueva canción
6. La historia se sube
7. Todos los fans que le dieron like ven la historia en "Tu Música"

### Usuario Fan:
1. Da like a canciones de varios artistas
2. Va a "Tu Música"
3. Ve las historias de esos artistas en la parte superior
4. Hace clic para ver las historias

## Mejoras Futuras

### Funcionalidades Adicionales:
- [ ] Agregar texto a las historias
- [ ] Agregar stickers y emojis
- [ ] Agregar música de fondo
- [ ] Filtros para fotos
- [ ] Historias de video
- [ ] Ver quién vio tu historia
- [ ] Responder a historias con mensajes
- [ ] Compartir historias de otros

### UI/UX:
- [ ] Preview antes de subir
- [ ] Editar foto antes de subir
- [ ] Agregar caption/descripción
- [ ] Temporizador para fotos
- [ ] Flash y cambio de cámara

## Notas Técnicas

### FileProvider
- Necesario para compartir archivos entre apps (cámara y tu app)
- Usa el directorio de caché para fotos temporales
- Se limpia automáticamente

### Cámara
- Usa `ActivityResultContracts.TakePicture()`
- Guarda foto temporal en caché
- Sube a Firebase Storage
- Elimina archivo temporal después

### Seguridad
- FileProvider no expone rutas de archivos directamente
- Solo comparte archivos específicos
- Permisos temporales

## Solución de Problemas

### "No se puede abrir la cámara"
- Verifica que los permisos estén otorgados
- Ve a Configuración > Apps > HypeMatch > Permisos
- Activa "Cámara"

### "Error al subir historia"
- Verifica conexión a internet
- Verifica que Firebase esté configurado
- Revisa los logs: `adb logcat | grep ProfileScreen`

### "FileProvider error"
- Verifica que `file_paths.xml` exista en `res/xml/`
- Verifica que el provider esté en AndroidManifest
- Limpia y reconstruye el proyecto

## Resumen

✅ **Implementado:**
- Clic en foto de perfil abre opciones
- Tomar foto con cámara
- Seleccionar de galería
- Subida automática a Firebase
- Integración con sistema de historias

✅ **Configurado:**
- FileProvider
- Permisos de cámara
- Rutas de archivos

✅ **Funciona:**
- Las historias aparecen en "Tu Música"
- Solo las ven quienes te dieron like
- Expiran en 24 horas

¡Listo para usar! 🎉
