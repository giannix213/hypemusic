# 🔍 Diagnóstico de Configuración Firebase

## Lo que veo en tu captura:

### ✅ Configuración Actual:
- **Nombre del remitente:** Hype
- **Email del remitente:** hype@hype-13966.firebaseapp.com
- **Responder a:** noreply
- **Asunto:** Verify your email for Hype@music

### 📋 Verificaciones Necesarias:

## 1. Verifica que la plantilla esté HABILITADA

En la captura veo "Verificación de dirección de correo electrónico" pero necesito que:

1. Hagas clic en **"Verificación de dirección de correo electrónico"**
2. Verifica que el toggle/switch esté **ACTIVADO** (azul/verde)
3. Si está desactivado, actívalo

## 2. Personaliza el Idioma

El asunto está en inglés: "Verify your email for Hype@music"

Para cambiarlo a español:
1. Haz clic en **"Verificación de dirección de correo electrónico"**
2. Busca la opción de **idioma** o **language**
3. Cámbialo a **Español**
4. O edita manualmente el asunto y mensaje

## 3. Verifica el Dominio Autorizado

1. En el menú izquierdo, ve a **Authentication** > **Settings**
2. Busca la sección **"Authorized domains"**
3. Asegúrate de que estos dominios estén en la lista:
   - `hype-13966.firebaseapp.com`
   - `localhost` (para desarrollo)

## 4. Prueba Manual

Desde Firebase Console:
1. Ve a **Authentication** > **Users**
2. Si tienes usuarios de prueba, selecciona uno
3. Haz clic en los tres puntos (⋮)
4. Selecciona **"Send verification email"**
5. Revisa si llega el email

## 5. Revisa los Logs de tu App

Ejecuta:
```bash
.\ver_logs_email.bat
```

Luego en tu app:
1. Crea una nueva cuenta
2. Observa los logs en la consola

**Busca estas líneas:**
```
✅ Usuario creado exitosamente: [uid]
✅ Email de verificación enviado exitosamente
```

**Si ves errores como:**
```
❌ Error en signUpWithEmail: [mensaje]
```
Copia el mensaje completo.

## 6. Prueba con Gmail

Usa una cuenta de **Gmail** para probar:
- Gmail tiene mejor compatibilidad con Firebase
- Revisa **todas** las carpetas:
  - Bandeja de entrada
  - Spam
  - Promociones
  - Actualizaciones

## 7. Espera Tiempo Suficiente

Los emails pueden tardar:
- Normal: 1-2 minutos
- A veces: 5-10 minutos
- Raro: hasta 15 minutos

## 8. Verifica el Plan de Firebase

1. En Firebase Console, ve a **Usage and billing**
2. Verifica que no hayas excedido límites
3. Plan gratuito: ~100 emails/día

## 🔧 Acciones Inmediatas

### Paso 1: Activa la Plantilla
Haz clic en "Verificación de dirección de correo electrónico" y asegúrate de que esté activada.

### Paso 2: Ejecuta los Logs
```bash
.\ver_logs_email.bat
```

### Paso 3: Crea una Cuenta de Prueba
Usa un email de Gmail y observa los logs.

### Paso 4: Revisa Spam
Espera 5 minutos y revisa la carpeta de spam.

## 📸 Capturas que Necesito

Si sigue sin funcionar, comparte capturas de:

1. **Plantilla de Email:**
   - Authentication > Templates > Verificación de dirección de correo electrónico
   - Muestra si está activada/desactivada

2. **Dominios Autorizados:**
   - Authentication > Settings > Authorized domains

3. **Logs de la App:**
   - La salida de `ver_logs_email.bat` cuando creas una cuenta

4. **Usuarios:**
   - Authentication > Users (para ver si se creó el usuario)

## ⚠️ Nota Importante

Mientras tanto, el **modo desarrollo** está activo, así que puedes:
- Crear cuentas sin verificar
- Acceder a la app normalmente
- Seguir desarrollando

Cuando soluciones el problema, recuerda cambiar en `DevConfig.kt`:
```kotlin
const val SKIP_EMAIL_VERIFICATION = false
```
