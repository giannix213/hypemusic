# 🚨 Instrucciones de Debug URGENTE

## ❗ Problema: No aparece NADA en los logs

Si no aparece ningún log cuando subes una historia, sigue estos pasos EXACTAMENTE:

## 📱 Paso 1: Verificar Logcat

1. Abre Android Studio
2. Ve a: **View → Tool Windows → Logcat**
3. En el filtro, escribe: `ProfileScreen`
4. Limpia los logs anteriores (botón de basura)

## 🎯 Paso 2: Probar el Botón +

1. En la app, ve al perfil
2. Presiona el botón **+** (amarillo, abajo a la derecha)
3. **INMEDIATAMENTE** verifica Logcat

### ¿Qué deberías ver?

```
➕ Click en botón + - Abriendo cámara
📷 StoryCamera está visible
```

### ❌ Si NO ves estos logs:

**Problema**: El botón + no está funcionando

**Solución**:
1. Cierra completamente la app
2. Limpia el build: Build → Clean Project
3. Rebuild: Build → Rebuild Project
4. Vuelve a ejecutar la app

## 📸 Paso 3: Tomar/Seleccionar Foto

Después de presionar +, deberías ver la pantalla de StoryCamera con 2 opciones:
- **Tomar Foto**
- **Seleccionar de Galería**

### Opción A: Tomar Foto

1. Presiona "Tomar Foto"
2. Toma una foto
3. Acepta la foto

### Opción B: Seleccionar de Galería

1. Presiona "Seleccionar de Galería"
2. Selecciona una imagen
3. Confirma

### ¿Qué deberías ver en Logcat?

```
📸 ===== onPhotoTaken LLAMADO =====
📸 Foto capturada/seleccionada: content://...
🚀 Iniciando subida de historia...
👤 Usuario: [userId] - [username]
```

### ❌ Si NO ves "onPhotoTaken LLAMADO":

**Problema**: El callback no se está ejecutando

**Causas posibles**:
1. Cancelaste la foto
2. No diste permisos de cámara/galería
3. Error en el launcher

**Solución**:
1. Verifica permisos: Settings → Apps → HypeMatch → Permissions
2. Asegúrate de dar permisos de Cámara y Almacenamiento
3. Intenta con "Seleccionar de Galería" en lugar de cámara

## 🔍 Paso 4: Verificar Subida

Si ves "🚀 Iniciando subida de historia...", deberías ver:

```
📊 Progreso de subida: 0%
📊 Progreso de subida: 50%
📊 Progreso de subida: 100%
✅ Historia subida exitosamente con ID: [id]
⏳ Esperando 2 segundos...
🔄 Intento 1: Recargando historias...
```

### ❌ Si se detiene en "Iniciando subida":

**Problema**: Error en uploadStory

**Solución**:
1. Verifica conexión a internet
2. Verifica que Firebase esté configurado
3. Busca logs de error: `❌ Error subiendo historia`

## 📊 Paso 5: Verificar Recarga

Después de "⏳ Esperando 2 segundos...", deberías ver:

```
🔄 Intento 1: Recargando historias...
📊 Intento 1: X historias encontradas
📚 Estado actualizado. Total: X
🔄 Trigger incrementado a: 1
```

### ❌ Si dice "0 historias encontradas":

**Problema**: La historia no se guardó o no se encuentra

**Solución**:
1. Verifica Firebase Console
2. Ve a Firestore Database
3. Busca colección `stories`
4. Verifica que hay un documento con tu `userId`

## 🎯 Paso 6: Verificar UI

Después de ver todos los logs, verifica:

1. **Toast**: Debe aparecer "✓ Historia publicada (1)"
2. **Contador**: Esquina inferior derecha debe mostrar "1"
3. **Aro**: Foto de perfil debe tener gradiente multicolor
4. **Color contador**: Debe cambiar de rosa a amarillo

### ❌ Si los logs son correctos pero la UI no cambia:

**Problema**: Problema de recomposición de Compose

**Solución**:
1. Haz click en el contador (esquina inferior derecha)
2. Debe aparecer toast "Historias: 1"
3. Si aún no cambia, reinicia la app

## 🔧 Soluciones Rápidas

### Solución 1: Reiniciar App
```
1. Cierra completamente la app
2. Vuelve a abrirla
3. Ve al perfil
4. Verifica el contador
```

### Solución 2: Limpiar Build
```
1. Build → Clean Project
2. Build → Rebuild Project
3. Run
```

### Solución 3: Reinstalar App
```
1. Desinstala la app del dispositivo
2. Run desde Android Studio
```

## 📋 Checklist de Verificación

Marca cada item:

- [ ] Logcat está abierto y filtrado por "ProfileScreen"
- [ ] Veo "➕ Click en botón +" cuando presiono +
- [ ] Veo "📷 StoryCamera está visible"
- [ ] Veo "📸 onPhotoTaken LLAMADO" después de seleccionar foto
- [ ] Veo "🚀 Iniciando subida de historia..."
- [ ] Veo "✅ Historia subida exitosamente"
- [ ] Veo "📚 Estado actualizado. Total: 1"
- [ ] Aparece toast "✓ Historia publicada (1)"
- [ ] El contador muestra "1"
- [ ] El aro tiene gradiente multicolor

## 🆘 Si Nada Funciona

Si después de todo esto no funciona, reporta:

1. **Screenshot de Logcat** completo
2. **Screenshot del perfil** mostrando el contador
3. **Screenshot de Firebase Console** (colección stories)
4. **Versión de Android** del dispositivo
5. **¿Es emulador o dispositivo físico?**

## 🎯 Logs Mínimos Esperados

Al subir una historia, DEBES ver al menos estos 3 logs:

```
1. ➕ Click en botón +
2. 📸 onPhotoTaken LLAMADO
3. 🚀 Iniciando subida de historia
```

Si no ves estos 3, hay un problema antes de la subida.
Si los ves pero no continúa, hay un problema en la subida.
Si todo continúa pero la UI no cambia, hay un problema de recomposición.
