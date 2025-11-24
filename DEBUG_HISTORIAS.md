# 🔍 Guía de Debugging - Historias No Visibles

## 🎯 Problema
Las historias no se muestran cuando haces tap en el perfil.

## 🛠️ Cambios Implementados para Debugging

### 1. Botón de Debug Temporal (🔄)
- **Ubicación**: Esquina superior derecha del perfil (botón rosa con emoji 🔄)
- **Función**: Recarga manualmente las historias y muestra logs detallados

### 2. Logs Mejorados

#### En ProfileScreen:
```
🔄 Cargando datos para userId: [tu_id]
📥 Solicitando historias de Firebase...
📚 Historias cargadas: [cantidad]
✅ Historias encontradas:
  [0] ID: [id], URL: [url]
  [1] ID: [id], URL: [url]
```

#### En FirebaseManager:
```
🔍 Buscando historias para userId: [tu_id]
⏰ Timestamp actual: [timestamp]
📊 Total historias en colección: [cantidad]
  - [id]: artistId=[id], expiresAt=[timestamp], expired=[true/false]
📊 Documentos encontrados para usuario: [cantidad]
✅ Historia cargada: [id]
```

## 📋 Pasos para Debugging

### Paso 1: Verificar que la historia se subió
1. Abre la app
2. Ve al perfil
3. Presiona el botón **+** (amarillo)
4. Toma una foto o selecciona de galería
5. Observa el indicador de progreso
6. Espera el mensaje "✓ Historia publicada"

**Logs esperados:**
```
📸 Foto capturada/seleccionada: [uri]
🚀 Iniciando subida de historia...
👤 Usuario: [userId] - [username]
📊 Progreso de subida: 0%
📊 Progreso de subida: 50%
📊 Progreso de subida: 100%
✅ Historia subida exitosamente con ID: [storyId]
🔄 Recargando historias...
📚 Historias recargadas. Total: [cantidad]
```

### Paso 2: Verificar en Firebase Console
1. Abre [Firebase Console](https://console.firebase.google.com)
2. Selecciona tu proyecto
3. Ve a **Firestore Database**
4. Busca la colección **`stories`**
5. Verifica que hay un documento con:
   - `artistId`: tu userId
   - `mediaUrl`: URL válida
   - `timestamp`: timestamp reciente
   - `expiresAt`: timestamp + 24 horas (debe ser mayor que el actual)

### Paso 3: Usar el botón de debug
1. En el perfil, presiona el botón **🔄** (esquina superior derecha)
2. Observa el toast que muestra "Historias: [cantidad]"
3. Revisa los logs en Logcat

**Logs esperados:**
```
🔄 DEBUG: Recargando historias manualmente...
👤 DEBUG: userId = [tu_id]
🔍 Buscando historias para userId: [tu_id]
⏰ Timestamp actual: [timestamp]
📊 Total historias en colección: [cantidad]
  - [id]: artistId=[id], expiresAt=[timestamp], expired=false
📊 Documentos encontrados para usuario: [cantidad]
✅ Historia cargada: [id]
📚 DEBUG: Historias recargadas: [cantidad]
  - [id]: [url]
```

### Paso 4: Verificar el anillo de gradiente
- Si `userStories.size > 0`, la foto de perfil debe tener un anillo de gradiente
- Si no tiene anillo, significa que `userStories` está vacío

### Paso 5: Verificar el click
1. Si hay anillo de gradiente, haz click en la foto de perfil
2. Debe aparecer el visor de historias

**Log esperado:**
```
👆 Click en foto de perfil - Abriendo visor
```

## 🔍 Posibles Problemas y Soluciones

### Problema 1: "Historias cargadas: 0"
**Causa**: No hay historias en Firebase o están expiradas

**Solución**:
1. Verifica en Firebase Console que la historia existe
2. Verifica que `expiresAt` > timestamp actual
3. Verifica que `artistId` coincide con tu `userId`

### Problema 2: "Total historias en colección: 0"
**Causa**: La colección `stories` está vacía

**Solución**:
1. Sube una nueva historia
2. Verifica que la subida fue exitosa (log "✅ Historia subida exitosamente")
3. Verifica en Firebase Console

### Problema 3: Historia existe pero no se muestra
**Causa**: El `artistId` no coincide con tu `userId`

**Solución**:
1. Compara el log "👤 Usuario: [userId]" con el `artistId` en Firebase
2. Si no coinciden, hay un problema con la autenticación

### Problema 4: "expiresAt" expirado
**Causa**: La historia tiene más de 24 horas

**Solución**:
1. Sube una nueva historia
2. O actualiza manualmente el campo `expiresAt` en Firebase Console

### Problema 5: Click en foto de perfil no hace nada
**Causa**: `userStories.isEmpty()` es true

**Solución**:
1. Verifica que el anillo de gradiente esté visible
2. Si no hay anillo, significa que no hay historias cargadas
3. Usa el botón 🔄 para recargar

## 📱 Cómo Ver los Logs

### En Android Studio:
1. Abre **Logcat** (View → Tool Windows → Logcat)
2. Filtra por:
   - `ProfileScreen`
   - `FirebaseManager`
   - `StoryCamera`

### Filtros útiles:
```
tag:ProfileScreen OR tag:FirebaseManager OR tag:StoryCamera
```

## 🎯 Checklist de Verificación

- [ ] La historia se sube correctamente (log "✅ Historia subida exitosamente")
- [ ] La historia aparece en Firebase Console
- [ ] El campo `expiresAt` es mayor que el timestamp actual
- [ ] El campo `artistId` coincide con tu `userId`
- [ ] El botón 🔄 muestra "Historias: 1" (o más)
- [ ] La foto de perfil tiene anillo de gradiente
- [ ] Click en foto de perfil abre el visor

## 🚀 Próximos Pasos

Una vez que identifiques el problema con los logs:
1. Reporta qué logs ves exactamente
2. Reporta qué logs NO ves (esperados pero ausentes)
3. Comparte screenshot de Firebase Console (colección stories)
4. Comparte el userId que estás usando

Con esta información podremos identificar exactamente dónde está el problema.
