# Cómo obtener el SHA-1 para Google Sign-In

## Opción 1: Desde Android Studio (MÁS FÁCIL)

1. Abre Android Studio
2. En el panel derecho, haz clic en **Gradle** (o ve a View > Tool Windows > Gradle)
3. Navega a: **HypeMatch > app > Tasks > android > signingReport**
4. Haz doble clic en **signingReport**
5. En la consola de salida, busca:
   ```
   Variant: debug
   Config: debug
   Store: C:\Users\Gianna\.android\debug.keystore
   Alias: androiddebugkey
   MD5: XX:XX:XX:...
   SHA1: XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX
   SHA-256: 38:98:B0:42:23:55:32:E7:C8:EA:0E:C6:E1:C9:2A:1D:19:D5:5A:67:AE:ED:33:47:4D:E9:0D:18:0A:9E:D2:38
   ```
6. Copia el valor de **SHA1**

## Opción 2: Desde Firebase Console (AUTOMÁTICO)

Firebase puede detectar automáticamente tu SHA-1:

1. Ve a [Firebase Console](https://console.firebase.google.com)
2. Selecciona tu proyecto "Hype"
3. Ve a **Configuración del proyecto** (⚙️)
4. En **Tus apps**, selecciona tu app Android
5. Haz clic en **Agregar huella digital**
6. Firebase te mostrará instrucciones para obtener el SHA-1

## Opción 3: Usar el SHA-256 que ya tenemos

Firebase también acepta SHA-256. Ya tenemos este:

```
SHA-256: 38:98:B0:42:23:55:32:E7:C8:EA:0E:C6:E1:C9:2A:1D:19:D5:5A:67:AE:ED:33:47:4D:E9:0D:18:0A:9E:D2:38
```

Puedes usar este directamente en Firebase Console.

## Pasos siguientes después de obtener el SHA:

### 1. Agregar a Firebase Console

1. Ve a [Firebase Console](https://console.firebase.google.com/project/hype-13966/settings/general)
2. En **Tus apps** → **HypeMatch (Android)**
3. Baja hasta **Huellas digitales de certificado SHA**
4. Haz clic en **Agregar huella digital**
5. Pega tu SHA-1 o SHA-256
6. Haz clic en **Guardar**

### 2. Descargar nuevo google-services.json

1. Después de agregar el SHA, descarga el nuevo `google-services.json`
2. Haz clic en el botón de descarga en la configuración de tu app
3. Reemplaza el archivo en: `app/google-services.json`

### 3. Obtener el Web Client ID

El nuevo `google-services.json` tendrá una sección `oauth_client` como esta:

```json
"oauth_client": [
  {
    "client_id": "880522213903-xxxxxxxxxxxxxxxxxx.apps.googleusercontent.com",
    "client_type": 3
  },
  {
    "client_id": "880522213903-yyyyyyyyyyyyyyyy.apps.googleusercontent.com",
    "client_type": 1,
    "android_info": {
      "package_name": "com.metu.hypematch",
      "certificate_hash": "3898b04223553..."
    }
  }
]
```

**Busca el que tiene `"client_type": 3`** (Web client) y copia su `client_id`.

### 4. Actualizar strings.xml

Abre `app/src/main/res/values/strings.xml` y reemplaza:

```xml
<string name="default_web_client_id">TU_CLIENT_ID_AQUI</string>
```

Con tu client_id real:

```xml
<string name="default_web_client_id">880522213903-xxxxxxxxxxxxxxxxxx.apps.googleusercontent.com</string>
```

### 5. Sincronizar y probar

1. En Android Studio: **File > Sync Project with Gradle Files**
2. Ejecuta la app
3. El botón "Continuar con Google" ahora debería aparecer
4. Al hacer clic, se abrirá el selector de cuentas de Google

## Verificación rápida

Para verificar que todo está configurado:

1. Abre `app/google-services.json`
2. Busca `"oauth_client": []`
3. Si está vacío `[]`, necesitas agregar el SHA y descargar el nuevo archivo
4. Si tiene contenido, busca el `client_id` con `client_type: 3`

## ¿Necesitas ayuda?

Si tienes problemas:
- Asegúrate de estar usando el keystore correcto (debug vs release)
- Verifica que el package name sea correcto: `com.metu.hypematch`
- Espera unos minutos después de agregar el SHA en Firebase
- Limpia y reconstruye el proyecto en Android Studio
