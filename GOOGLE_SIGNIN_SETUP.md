# Configuración de Google Sign-In para HypeMatch

## Pasos para habilitar Google Sign-In:

### 1. Obtener el SHA-1 de tu app

En Android Studio, abre la terminal y ejecuta:

```bash
# Para debug
./gradlew signingReport
```

O en Windows:
```bash
gradlew.bat signingReport
```

Busca el SHA-1 en la salida, se verá algo así:
```
SHA1: A1:B2:C3:D4:E5:F6:G7:H8:I9:J0:K1:L2:M3:N4:O5:P6:Q7:R8:S9:T0
```

### 2. Agregar SHA-1 a Firebase

1. Ve a [Firebase Console](https://console.firebase.google.com)
2. Selecciona tu proyecto "Hype"
3. Ve a **Configuración del proyecto** (ícono de engranaje)
4. En la pestaña **General**, baja hasta **Tus apps**
5. Selecciona tu app Android
6. En **Huellas digitales de certificado SHA**, haz clic en **Agregar huella digital**
7. Pega tu SHA-1 y guarda

### 3. Habilitar Google Sign-In en Firebase

1. En Firebase Console, ve a **Authentication**
2. Pestaña **Sign-in method**
3. Habilita **Google**
4. Guarda los cambios

### 4. Descargar nuevo google-services.json

1. Después de agregar el SHA-1, descarga el nuevo archivo `google-services.json`
2. Reemplaza el archivo en `app/google-services.json`
3. El nuevo archivo incluirá el `oauth_client` con tu Web Client ID

### 5. Actualizar strings.xml

El nuevo `google-services.json` tendrá algo como:

```json
"oauth_client": [
  {
    "client_id": "880522213903-xxxxxxxxxx.apps.googleusercontent.com",
    "client_type": 3
  }
]
```

Copia el `client_id` que tiene `client_type: 3` y actualiza en `app/src/main/res/values/strings.xml`:

```xml
<string name="default_web_client_id">TU_CLIENT_ID_AQUI</string>
```

### 6. Sincronizar y compilar

1. En Android Studio: **File > Sync Project with Gradle Files**
2. Limpia y reconstruye: **Build > Clean Project** y luego **Build > Rebuild Project**
3. Ejecuta la app

## Verificación

Una vez configurado:
- El botón "Continuar con Google" aparecerá en la pantalla de login
- Al hacer clic, se abrirá el selector de cuentas de Google
- Después de seleccionar una cuenta, el usuario quedará autenticado
- El nombre y email del usuario se mostrarán en el perfil

## Solución de problemas

### Error: "Developer error"
- Verifica que el SHA-1 sea correcto
- Asegúrate de usar el SHA-1 del keystore correcto (debug o release)

### Error: "API not enabled"
- Ve a Google Cloud Console
- Habilita "Google Sign-In API"

### El botón de Google no aparece
- Verifica que `default_web_client_id` no sea "YOUR_WEB_CLIENT_ID_HERE"
- Revisa que el `google-services.json` tenga el `oauth_client`

## Modo actual (sin configurar)

Si no configuras Google Sign-In, la app funcionará con:
- ✅ Login como invitado (anónimo)
- ❌ Login con Google (botón oculto)

El usuario puede usar la app normalmente como invitado.
