# 🎯 Guía Visual: Desplegar Cloud Function (Paso a Paso)

## 📋 Resumen Rápido

1. ✅ Instalar Node.js (5 min)
2. ✅ Instalar Firebase CLI (2 min)
3. ✅ Inicializar Functions (3 min)
4. ✅ Copiar código (1 min)
5. ✅ Desplegar (2 min)

**Total: ~15 minutos**

---

## 🚀 Opción Rápida: Script Automático

### Paso 1: Instalar Node.js

1. Ve a: **https://nodejs.org/**
2. Descarga la versión **LTS** (botón verde grande)
3. Ejecuta el instalador
4. Click en "Next" hasta terminar
5. **Reinicia PowerShell**

### Paso 2: Ejecutar el script automático

Abre PowerShell en tu proyecto y ejecuta:

```powershell
cd C:\Users\Freddy\HypeMatch
.\setup-firebase-functions.ps1
```

El script hará todo automáticamente. Solo sigue las instrucciones en pantalla.

---

## 🔧 Opción Manual: Paso a Paso

### Paso 1: Instalar Node.js

```
📥 Descargar: https://nodejs.org/
📦 Instalar: Ejecutar el .msi descargado
🔄 Reiniciar: Cerrar y abrir PowerShell
```

**Verificar:**
```powershell
node --version
# Debería mostrar: v20.x.x
```

### Paso 2: Instalar Firebase CLI

```powershell
npm install -g firebase-tools
```

**Espera 1-2 minutos...**

**Verificar:**
```powershell
firebase --version
# Debería mostrar: 13.x.x
```

### Paso 3: Iniciar sesión en Firebase

```powershell
firebase login
```

**Lo que verás:**
```
? Allow Firebase to collect CLI and Emulator Suite usage and error reporting information?
```

Responde: **Y** (Yes)

**Se abrirá tu navegador:**
1. Selecciona tu cuenta de Google
2. Click en "Permitir"
3. Verás: "Success! Logged in as tu-email@gmail.com"
4. Cierra el navegador
5. Vuelve a PowerShell

### Paso 4: Verificar proyecto

```powershell
cd C:\Users\Freddy\HypeMatch
firebase projects:list
```

**Deberías ver algo como:**
```
┌──────────────────────┬────────────────┬────────────────┐
│ Project Display Name │ Project ID     │ Resource       │
├──────────────────────┼────────────────┼────────────────┤
│ HypeMatch            │ hypematch-xxxx │ hypematch-xxxx │
└──────────────────────┴────────────────┴────────────────┘
```

### Paso 5: Inicializar Functions

```powershell
firebase init functions
```

**Pregunta 1:**
```
? Please select an option:
  Use an existing project
> Use an existing project  <-- Selecciona esta
  Create a new project
```
Presiona **Enter**

**Pregunta 2:**
```
? Select a default Firebase project for this directory:
> hypematch-xxxx (HypeMatch)  <-- Selecciona tu proyecto
```
Presiona **Enter**

**Pregunta 3:**
```
? What language would you like to use to write Cloud Functions?
> JavaScript  <-- Selecciona esta
  TypeScript
```
Presiona **Enter**

**Pregunta 4:**
```
? Do you want to use ESLint to catch probable bugs and enforce style?
> No  <-- Selecciona No
  Yes
```
Presiona **Enter**

**Pregunta 5:**
```
? Do you want to install dependencies with npm now?
> Yes  <-- Selecciona Yes
  No
```
Presiona **Enter**

**Espera 1-2 minutos mientras instala...**

**Verás:**
```
✔  Firebase initialization complete!
```

### Paso 6: Instalar dependencia de Agora

```powershell
cd functions
npm install agora-access-token
```

**Espera 30 segundos...**

**Verás:**
```
added 1 package
```

### Paso 7: Copiar el código

```powershell
cd ..
copy functions_index.js functions\index.js
```

**Verificar que se copió:**
```powershell
type functions\index.js
```

Deberías ver el código de la función.

### Paso 8: Desplegar

```powershell
firebase deploy --only functions
```

**Lo que verás:**

```
=== Deploying to 'hypematch-xxxx'...

i  deploying functions
i  functions: ensuring required API cloudfunctions.googleapis.com is enabled...
i  functions: ensuring required API cloudbuild.googleapis.com is enabled...
✔  functions: required API cloudfunctions.googleapis.com is enabled
✔  functions: required API cloudbuild.googleapis.com is enabled
i  functions: preparing codebase default for deployment
i  functions: preparing functions directory for uploading...
i  functions: packaged functions (XX.XX KB) for uploading
✔  functions: functions folder uploaded successfully

The following functions are found in your project but do not exist in your local source code:
...

i  functions: creating Node.js 18 function generateAgoraToken(us-central1)...
i  functions: creating Node.js 18 function generateStreamerToken(us-central1)...
i  functions: creating Node.js 18 function generateViewerToken(us-central1)...
✔  functions[generateAgoraToken(us-central1)] Successful create operation.
✔  functions[generateStreamerToken(us-central1)] Successful create operation.
✔  functions[generateViewerToken(us-central1)] Successful create operation.

✔  Deploy complete!
```

**Esto toma 2-3 minutos.**

### Paso 9: Verificar en Firebase Console

1. Ve a: **https://console.firebase.google.com/**
2. Click en tu proyecto
3. Click en **"Functions"** en el menú lateral
4. Deberías ver:

```
📋 Functions

generateAgoraToken          us-central1    ✅ Active
generateStreamerToken       us-central1    ✅ Active
generateViewerToken         us-central1    ✅ Active
```

---

## 🧪 Probar la función

En Firebase Console > Functions:

1. Click en **generateStreamerToken**
2. Click en **"Logs"**
3. Deberías ver logs cuando la función se ejecute

---

## ✅ Checklist de Verificación

- [ ] Node.js instalado (`node --version` funciona)
- [ ] Firebase CLI instalado (`firebase --version` funciona)
- [ ] Sesión iniciada (`firebase login` completado)
- [ ] Proyecto verificado (`firebase projects:list` muestra tu proyecto)
- [ ] Functions inicializadas (carpeta `functions/` existe)
- [ ] Dependencia instalada (`functions/node_modules/agora-access-token` existe)
- [ ] Código copiado (`functions/index.js` tiene el código)
- [ ] Funciones desplegadas (Firebase Console muestra 3 funciones)

---

## 🎉 ¡Listo!

Ahora continúa con: **Actualizar FirebaseManager.kt**

Ver: `DESPLEGAR_CLOUD_FUNCTION.md` sección "Actualizar FirebaseManager.kt"

---

## 🐛 Errores Comunes

### "node no se reconoce"
**Solución:** Reinicia PowerShell después de instalar Node.js

### "firebase no se reconoce"
**Solución:** 
```powershell
npm install -g firebase-tools
```

### "Permission denied"
**Solución:** Ejecuta PowerShell como Administrador

### "Project not found"
**Solución:**
```powershell
firebase use --add
```
Selecciona tu proyecto

### Deploy falla
**Solución:** Verifica que `functions/index.js` tenga el código correcto

---

## 📞 Siguiente Paso

Una vez desplegado, actualiza `FirebaseManager.kt` para usar la Cloud Function en lugar del token temporal.
