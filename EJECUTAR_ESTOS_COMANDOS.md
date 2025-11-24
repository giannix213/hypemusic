# 🚀 Ejecuta Estos 3 Comandos en PowerShell

Ya preparé todo. Solo necesitas ejecutar estos comandos:

## Paso 1: Iniciar sesión en Firebase

```powershell
firebase login
```

**Se abrirá tu navegador:**
- Inicia sesión con la cuenta que tiene tu proyecto
- Cierra el navegador cuando diga "Success"

## Paso 2: Instalar dependencias

```powershell
cd C:\Users\Freddy\HypeMatch\functions
npm install
```

Esto instalará:
- firebase-functions
- firebase-admin
- agora-access-token

**Espera 1-2 minutos...**

## Paso 3: Desplegar

```powershell
cd ..
firebase deploy --only functions
```

**La primera vez te preguntará:**
- ¿Qué proyecto usar? → Selecciona tu proyecto
- ¿Habilitar APIs? → Sí

**Espera 2-3 minutos...**

Verás:
```
✔  functions[generateAgoraToken] Successful create operation.
✔  functions[generateStreamerToken] Successful create operation.
✔  functions[generateViewerToken] Successful create operation.

✔  Deploy complete!
```

## ✅ Verificar

Ve a: https://console.firebase.google.com/
- Selecciona tu proyecto
- Ve a Functions
- Deberías ver 3 funciones desplegadas

## 🎉 ¡Listo!

Después continúa con actualizar `FirebaseManager.kt`
