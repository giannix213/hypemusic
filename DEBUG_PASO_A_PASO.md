# 🔍 Debug Paso a Paso - Historias No Se Cargan

## 📋 Síntomas Reportados

- ✅ Sale "Historia subida"
- ❌ No cambia el color del aro
- ❌ No suma el contador

## 🎯 Qué Verificar en Logcat

### Paso 1: Abrir Logcat
1. En Android Studio: View → Tool Windows → Logcat
2. Filtrar por: `ProfileScreen` o `FirebaseManager`

### Paso 2: Subir una Historia

Cuando presiones el botón + y subas una foto, deberías ver esta secuencia EXACTA de logs:

```
📸 Foto capturada/seleccionada: content://...
🚀 Iniciando subida de historia...
👤 Usuario: [tu_userId] - [tu_username]
📊 Progreso de subida: 0%
📊 Progreso de subida: 50%
📊 Progreso de subida: 100%

[En FirebaseManager]
🚀 uploadStory iniciado
📝 Datos: artistId=[tu_userId], name=[tu_username], type=image
📎 URI: content://...
📤 Subiendo archivo a Storage...
✅ Archivo subido. URL: https://firebasestorage...
💾 Guardando en Firestore colección 'stories'...
📊 Datos del documento: {artistId=..., mediaUrl=..., ...}
✅ Historia guardada con ID: [storyId]
⏰ Expira en: [fecha]

[De vuelta en ProfileScreen]
✅ Historia subida exitosamente con ID: [storyId]
⏳ Esperando 2 segundos para que Firestore procese...
🔄 Intento 1: Recargando historias...

[En FirebaseManager]
🔍 Buscando historias para userId: [tu_userId]
⏰ Timestamp actual: [timestamp]
📊 Total historias en colección: X
  - [id1]: artistId=[userId], expiresAt=[timestamp], expired=false
  - [id2]: artistId=[userId], expiresAt=[timestamp], expired=false
📊 Documentos encontrados para usuario: X
✅ Historia cargada: [id1]
✅ Historia cargada: [id2]

[De vuelta en ProfileScreen]
📊 Intento 1: X historias encontradas
📚 Estado actualizado. Total: X
🔄 Trigger incrementado a: 1
```

## 🚨 Problemas Comunes y Soluciones

### Problema 1: "Historia guardada" pero "Documentos encontrados: 0"

**Causa**: El `artistId` no coincide con el `userId`

**Verificar**:
```
Buscar en logs:
- "👤 Usuario: [userId]"
- "artistId=[artistId]"

¿Son iguales? Si NO → Problema de autenticación
```

**Solución**:
1. Cierra sesión
2. Vuelve a iniciar sesión
3. Intenta de nuevo

### Problema 2: "expired=true" en todas las historias

**Causa**: El `expiresAt` es menor que el timestamp actual

**Verificar**:
```
Buscar en logs:
- "⏰ Timestamp actual: [now]"
- "expiresAt=[expiresAt]"

¿expiresAt > now? Si NO → Historia expirada
```

**Solución**:
- Las historias expiran después de 24 horas
- Sube una nueva historia

### Problema 3: "Total historias en colección: 0"

**Causa**: La colección `stories` está vacía en Firestore

**Verificar**:
1. Abre Firebase Console
2. Ve a Firestore Database
3. Busca la colección `stories`
4. ¿Hay documentos?

**Solución**:
- Si no hay documentos, la subida falló
- Verifica los permisos de Firestore
- Verifica la conexión a internet

### Problema 4: "Estado actualizado. Total: 0" después de subir

**Causa**: La historia se subió pero la consulta no la encuentra

**Verificar en logs**:
```
1. ¿Aparece "✅ Historia guardada con ID: [id]"?
   → SÍ: La historia se guardó
   
2. ¿Aparece ese mismo ID en "Total historias en colección"?
   → NO: Problema con la consulta
   
3. ¿El artistId coincide con tu userId?
   → NO: Problema de autenticación
```

## 🔧 Soluciones Rápidas

### Solución 1: Hacer Click en el Contador

El contador en la esquina inferior derecha es clickable:
1. Haz click en él
2. Espera 1 segundo
3. Debe aparecer toast con "Historias: X"
4. Verifica los logs

### Solución 2: Esperar 10 Segundos

El sistema recarga automáticamente cada 10 segundos:
1. Después de subir, espera 10 segundos
2. Verifica si el contador se actualiza
3. Verifica los logs: "🔄 Recarga automática de historias..."

### Solución 3: Reiniciar la App

A veces el estado se corrompe:
1. Cierra completamente la app
2. Vuelve a abrirla
3. Ve al perfil
4. Verifica el contador

## 📊 Checklist de Verificación

Marca cada item después de verificarlo:

- [ ] Los logs muestran "✅ Historia guardada con ID: [id]"
- [ ] Los logs muestran "📊 Total historias en colección: 1" (o más)
- [ ] El `artistId` en los logs coincide con tu `userId`
- [ ] El `expiresAt` es mayor que el timestamp actual
- [ ] Los logs muestran "✅ Historia cargada: [id]"
- [ ] Los logs muestran "📚 Estado actualizado. Total: 1" (o más)
- [ ] El toast muestra "✓ Historia publicada (1)" (o más)
- [ ] El contador en la esquina muestra el número correcto
- [ ] El aro de la foto de perfil tiene gradiente multicolor
- [ ] Al hacer click en la foto de perfil, se abre el visor

## 🎯 Qué Reportar

Si después de verificar todo lo anterior el problema persiste, reporta:

1. **Logs completos** desde que presionas + hasta que aparece el toast
2. **Screenshot** de Firebase Console mostrando la colección `stories`
3. **Tu userId** (búscalo en los logs: "👤 Usuario: [userId]")
4. **El artistId** de la historia (búscalo en los logs)
5. **Los timestamps** (actual vs expiresAt)

## 🔍 Comando de Logcat

Para filtrar solo los logs relevantes:

```bash
adb logcat | grep -E "ProfileScreen|FirebaseManager"
```

O en Android Studio Logcat, usa este filtro:
```
tag:ProfileScreen | tag:FirebaseManager
```

## ✅ Resultado Esperado

Después de subir una historia, deberías ver:

1. ✅ Toast: "✓ Historia publicada (1)"
2. ✅ Contador: Muestra "1" con fondo amarillo
3. ✅ Aro: Gradiente multicolor alrededor de la foto
4. ✅ Logs: "📚 Estado actualizado. Total: 1"
5. ✅ Click en foto: Abre el visor de historias

Si ves todo esto, ¡funciona correctamente! 🎉
