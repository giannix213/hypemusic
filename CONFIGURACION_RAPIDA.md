# 🚀 Configuración Rápida de Google Sign-In

## Tu SHA-256 (ya obtenido):
```
38:98:B0:42:23:55:32:E7:C8:EA:0E:C6:E1:C9:2A:1D:19:D5:5A:67:AE:ED:33:47:4D:E9:0D:18:0A:9E:D2:38
```

## Pasos a seguir (5 minutos):

### 1️⃣ Agregar SHA-256 a Firebase

1. Abre este link: https://console.firebase.google.com/project/hype-13966/settings/general
2. Baja hasta **"Tus apps"**
3. Encuentra tu app **HypeMatch (Android)**
4. En **"Huellas digitales de certificado SHA"**, haz clic en **"Agregar huella digital"**
5. Pega esto:
   ```
   38:98:B0:42:23:55:32:E7:C8:EA:0E:C6:E1:C9:2A:1D:19:D5:5A:67:AE:ED:33:47:4D:E9:0D:18:0A:9E:D2:38
   ```
6. Haz clic en **"Guardar"**

### 2️⃣ Descargar nuevo google-services.json

1. En la misma página, baja un poco más
2. Haz clic en el botón **"google-services.json"** para descargarlo
3. Reemplaza el archivo en tu proyecto: `app/google-services.json`

### 3️⃣ Obtener Web Client ID

Abre el nuevo `google-services.json` que descargaste y busca esta sección:

```json
"oauth_client": [
  {
    "client_id": "880522213903-XXXXXXXXXX.apps.googleusercontent.com",
    "client_type": 3
  }
]
```

Copia el `client_id` que tiene `"client_type": 3`

### 4️⃣ Actualizar strings.xml

Abre: `app/src/main/res/values/strings.xml`

Reemplaza esta línea:
```xml
<string name="default_web_client_id">YOUR_WEB_CLIENT_ID_HERE</string>
```

Con tu client_id:
```xml
<string name="default_web_client_id">880522213903-XXXXXXXXXX.apps.googleusercontent.com</string>
```

### 5️⃣ Sincronizar y ejecutar

1. En Android Studio: **File > Sync Project with Gradle Files**
2. Ejecuta la app (Shift + F10)
3. ¡Listo! El botón de Google debería aparecer

## ✅ Verificación

Después de configurar, deberías ver:
- ✅ Botón "Continuar con Google" en la pantalla de login
- ✅ Al hacer clic, se abre el selector de cuentas de Google
- ✅ Después de seleccionar una cuenta, entras a la app
- ✅ Tu nombre de Google aparece en el perfil

## ❌ Solución de problemas

### El botón de Google no aparece
- Verifica que `default_web_client_id` no sea "YOUR_WEB_CLIENT_ID_HERE"
- Asegúrate de haber sincronizado el proyecto

### Error "Developer error" o "API not enabled"
- Espera 5-10 minutos después de agregar el SHA
- Verifica que el SHA-256 esté correctamente agregado en Firebase

### Error "Sign in failed"
- Verifica que el `client_id` sea el correcto (client_type: 3)
- Asegúrate de haber descargado el nuevo `google-services.json`

## 📱 Mientras tanto...

La app funciona perfectamente con el botón **"Continuar como invitado"** (login anónimo).
Los usuarios pueden usar todas las funciones de la app sin configurar Google Sign-In.

---

**¿Necesitas ayuda?** Avísame cuando hayas completado cada paso y te ayudo con el siguiente.
