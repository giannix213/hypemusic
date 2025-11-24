# 🚀 INICIO RÁPIDO - Live Streaming

## ⚡ PRUEBA EN 3 PASOS

### 1️⃣ Ejecutar script automático
```bash
probar-live.bat
```

Este script:
- ✅ Compila el APK
- ✅ Instala en el dispositivo
- ✅ Limpia logs
- ✅ Muestra logs en tiempo real

### 2️⃣ En el dispositivo
1. Abre la app
2. Ve a la pantalla de Lives
3. Presiona "Iniciar Live"

### 3️⃣ Verificar logs
Deberías ver:
```
✅ INICIANDO NUEVA SESIÓN DE LIVE
✅ Token de Agora recibido
✅ Sesión creada en Firestore: [sessionId]
```

## 🎯 ¿QUÉ SE ARREGLÓ?

### Problema anterior:
```
Usuario → LiveLauncher → ❌ LiveViewModel no existe → FALLA
```

### Solución actual:
```
Usuario → LiveLauncher → Firebase.startNewLiveSession() → ✅ FUNCIONA
```

## 📊 VERIFICACIÓN RÁPIDA

### ¿El emisor puede iniciar Live?
- ✅ Debe aparecer "LIVE 🔴" en pantalla
- ✅ Debe verse el contador de espectadores

### ¿El espectador puede ver Lives?
- ✅ Debe aparecer en la lista de Lives activos
- ✅ Debe poder unirse al Live

### ¿Firebase tiene el documento?
1. Abrir: https://console.firebase.google.com
2. Ir a: Firestore Database > live_sessions
3. Verificar: Existe documento con `isActive: true`

## 🐛 SI HAY PROBLEMAS

### El emisor no puede iniciar Live

**Ver logs:**
```bash
adb logcat -s FirebaseManager:D LiveLauncher:D -v time
```

**Buscar:**
- ❌ "Error iniciando Live"
- ❌ "No se recibió token de Agora"

**Solución:**
1. Verificar que Cloud Function esté desplegada
2. Verificar Agora App ID en `AgoraConfig.kt`

### El espectador no ve Lives

**Ver logs:**
```bash
adb logcat -s FirebaseManager:D LiveScreenNew:D -v time
```

**Buscar:**
- "Lives encontrados: 0"

**Solución:**
1. Verificar que el emisor haya iniciado Live
2. Verificar Firebase Console (debe existir documento)
3. Verificar Firestore Rules (debe permitir lectura)

### Error de compilación

**Limpiar y recompilar:**
```bash
gradlew clean assembleDebug
```

## 📝 ARCHIVOS CLAVE

### Código modificado:
- `LiveLauncherScreen.kt` - Reescrito sin LiveViewModel

### Documentación:
- `DIAGNOSTICO_PROBLEMA_LIVE_ENCONTRADO.md` - Análisis del problema
- `SOLUCION_LIVE_IMPLEMENTADA.md` - Detalles técnicos
- `COMANDOS_RAPIDOS_LIVE.md` - Comandos útiles

### Scripts:
- `probar-live.bat` - Prueba automática
- `verificar-live-completo.bat` - Verificación completa

## 🎬 FLUJO COMPLETO

```
1. Ejecutar: probar-live.bat
   ↓
2. Abrir app en dispositivo
   ↓
3. Ir a Lives > Iniciar Live
   ↓
4. Ver logs: "Sesión creada en Firestore"
   ↓
5. En otro dispositivo: Ver Lives activos
   ↓
6. Debe aparecer el Live ✅
```

## ✅ CHECKLIST

- [ ] Compilar: `probar-live.bat`
- [ ] Instalar en dispositivo(s)
- [ ] Iniciar Live en Dispositivo 1
- [ ] Ver logs: "Sesión creada"
- [ ] Verificar Firebase Console
- [ ] Ver Lives en Dispositivo 2
- [ ] Unirse al Live
- [ ] Verificar contador de espectadores

## 🎉 RESULTADO ESPERADO

**Dispositivo 1 (Emisor):**
- ✅ Botón "Iniciar Live" funciona
- ✅ Aparece "LIVE 🔴"
- ✅ Se ve la cámara
- ✅ Contador de espectadores aumenta

**Dispositivo 2 (Espectador):**
- ✅ Ve el Live en la lista
- ✅ Puede unirse
- ✅ Ve la transmisión
- ✅ Puede salir

**Firebase Console:**
- ✅ Documento en `live_sessions`
- ✅ Campo `isActive: true`
- ✅ Campo `viewerCount` se actualiza

---

**¿Listo para probar?** → Ejecuta `probar-live.bat` 🚀
