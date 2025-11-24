# 🔍 Diagnóstico Rápido - Error al Iniciar Live

## ⚡ Verificación Rápida (5 minutos)

### 1️⃣ Verificar Cloud Functions (MÁS PROBABLE)

Ejecuta este comando en la terminal:
```bash
firebase functions:list
```

**¿Qué deberías ver?**
```
✔ functions list
┌────────────────────────┬────────────────────────────────────┐
│ Function Name          │ Status                             │
├────────────────────────┼────────────────────────────────────┤
│ generateAgoraToken     │ Deployed                           │
│ generateStreamerToken  │ Deployed                           │
│ generateViewerToken    │ Deployed                           │
└────────────────────────┴────────────────────────────────────┘
```

**❌ Si NO ves las funciones o ves un error:**
```bash
# Opción 1: Usar el script automático
verificar-y-desplegar-functions.bat

# Opción 2: Manual
cd functions
npm install
cd ..
firebase deploy --only functions
```

### 2️⃣ Verificar Logs de la App

Abre Logcat en Android Studio y filtra por: `FirebaseManager`

**Intenta iniciar un Live y busca:**

✅ **Si funciona, verás:**
```
🎬 ===== INICIANDO NUEVA SESIÓN DE LIVE =====
📺 Canal generado: live_...
🔑 Solicitando token de Agora...
✅ Token de Agora recibido: ...
✅ Sesión creada en Firestore: ...
✅ ===== SESIÓN DE LIVE LISTA =====
```

❌ **Si falla, verás uno de estos errores:**

**Error A: "Cloud Function not found"**
```
❌ Error: Cloud Function 'generateAgoraToken' not found
```
**Solución:** Las funciones no están desplegadas → Ejecuta `verificar-y-desplegar-functions.bat`

**Error B: "Permission denied"**
```
❌ Error: Permission denied
```
**Solución:** Problema de autenticación → Verifica que el usuario esté logueado

**Error C: "Network error"**
```
❌ Error: Network error
```
**Solución:** Problema de conexión → Verifica tu internet

**Error D: "Invalid credentials"**
```
❌ Error: Invalid Agora credentials
```
**Solución:** Credenciales de Agora incorrectas → Ver sección 3

### 3️⃣ Verificar Credenciales de Agora

Las credenciales están en `functions/index.js`:
```javascript
const APP_ID = '72117baf2c874766b556e6f83ac9c58d';
const APP_CERTIFICATE = 'f907826ae8ff4c00b7057d15b6f2e628';
```

**Para verificar:**
1. Ve a https://console.agora.io/
2. Inicia sesión
3. Ve a "Project Management"
4. Compara el APP_ID con el que tienes en el código
5. Si no coincide, actualiza `functions/index.js` y redespliega

### 4️⃣ Rebuild de la App

Después de hacer cambios:
```bash
# Opción 1: Desde Android Studio
Build > Clean Project
Build > Rebuild Project

# Opción 2: Desde terminal
gradlew clean
gradlew build
```

## 🎯 Solución Más Probable

**El 90% de las veces el problema es que las Cloud Functions no están desplegadas.**

**Solución rápida:**
```bash
verificar-y-desplegar-functions.bat
```

O manualmente:
```bash
cd functions
npm install
cd ..
firebase deploy --only functions
```

Espera a que termine (puede tomar 2-3 minutos) y luego prueba de nuevo.

## 📱 Probar el Live

1. Abre la app
2. Ve a la pestaña "Live" (ícono de play ▶️)
3. Toca el botón para iniciar Live
4. Deberías ver:
   - ⏳ "Preparando Live..." (2-3 segundos)
   - 📹 Pantalla de cámara activa
   - 🔴 Botón rojo para finalizar

## 🆘 Si Nada Funciona

1. **Captura los logs completos** de Logcat (filtra por `FirebaseManager`)
2. **Ejecuta** `firebase functions:list` y captura el resultado
3. **Verifica** que estés logueado en Firebase: `firebase login`
4. **Comparte** los logs para diagnóstico detallado

## ✅ Checklist Final

- [ ] Firebase CLI instalado (`firebase --version`)
- [ ] Logueado en Firebase (`firebase login`)
- [ ] Cloud Functions desplegadas (`firebase functions:list`)
- [ ] Dependencias instaladas en `functions/` (`npm install`)
- [ ] App rebuildeada (`Clean + Rebuild`)
- [ ] Usuario logueado en la app
- [ ] Internet funcionando

---

**Tiempo estimado de solución:** 5-10 minutos
**Dificultad:** Baja
**Causa más común:** Cloud Functions no desplegadas (90%)
