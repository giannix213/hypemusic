# 📧 Instrucciones: Verificación de Email

## ⚠️ MODO DESARROLLO ACTIVADO

He activado temporalmente el **modo desarrollo** para que puedas seguir trabajando mientras solucionamos el problema del email.

### ¿Qué significa esto?

- ✅ Puedes crear cuentas y acceder sin verificar el email
- ⚠️ Verás un banner rosa que dice "MODO DESARROLLO"
- 🔧 Esto es **SOLO para desarrollo**, NO para producción

### Cómo desactivar el modo desarrollo

Cuando soluciones el problema del email, edita el archivo:
```
app/src/main/java/com/metu/hypematch/DevConfig.kt
```

Cambia esta línea:
```kotlin
const val SKIP_EMAIL_VERIFICATION = true  // ⚠️ Cambiar a false en producción
```

A:
```kotlin
const val SKIP_EMAIL_VERIFICATION = false  // ✅ Listo para producción
```

---

## 🔍 Diagnóstico del Problema

### Paso 1: Ver los Logs

Ejecuta este comando en PowerShell:
```bash
.\ver_logs_email.bat
```

O manualmente:
```bash
adb logcat -s AuthManager:D FirebaseAuth:D
```

### Paso 2: Registra un Usuario

1. Abre tu app
2. Crea una cuenta con un email real (usa Gmail para mejores resultados)
3. Observa los logs

**Deberías ver:**
```
✅ Usuario creado exitosamente: [uid]
✅ Email de verificación enviado exitosamente
```

**Si ves errores**, cópialos y revísalos.

### Paso 3: Revisa tu Email

1. Abre tu bandeja de entrada
2. **Revisa la carpeta de SPAM** ⚠️ (muy importante)
3. Busca un email de: `noreply@[tu-proyecto].firebaseapp.com`
4. Espera hasta 5-10 minutos

---

## 🛠️ Soluciones Comunes

### Problema 1: Email en Spam

**Solución:** Revisa la carpeta de spam/correo no deseado

### Problema 2: Configuración de Firebase

1. Ve a [Firebase Console](https://console.firebase.google.com)
2. Selecciona tu proyecto
3. **Authentication** > **Templates**
4. Selecciona **Email address verification**
5. Verifica que esté **habilitado**

### Problema 3: Dominio no autorizado

1. Firebase Console > **Authentication** > **Settings**
2. **Authorized domains**
3. Asegúrate de que tu dominio esté en la lista

### Problema 4: Cuota excedida

1. Firebase Console > **Authentication** > **Usage**
2. Verifica que no hayas excedido el límite

### Problema 5: Proveedor de email

Algunos proveedores bloquean emails automáticos:
- ✅ **Gmail** (recomendado)
- ✅ **Outlook/Hotmail**
- ⚠️ Yahoo (a veces bloquea)
- ⚠️ Dominios corporativos

---

## 🧪 Prueba Manual desde Firebase

Para confirmar que Firebase puede enviar emails:

1. Ve a Firebase Console
2. **Authentication** > **Users**
3. Encuentra tu usuario de prueba
4. Haz clic en los tres puntos (⋮)
5. Selecciona **"Send verification email"**
6. Revisa tu email

Si esto tampoco funciona, el problema está en la configuración de Firebase, no en tu código.

---

## 📊 Checklist de Verificación

- [ ] Ejecuté `ver_logs_email.bat` y vi los logs
- [ ] Los logs muestran "✅ Email de verificación enviado exitosamente"
- [ ] Revisé la carpeta de spam
- [ ] Usé Gmail o Outlook
- [ ] Esperé al menos 5 minutos
- [ ] Verifiqué la configuración en Firebase Console
- [ ] Probé envío manual desde Firebase Console
- [ ] El email que usé es válido y existe

---

## 🚀 Cuando Todo Funcione

1. Edita `DevConfig.kt`
2. Cambia `SKIP_EMAIL_VERIFICATION` a `false`
3. Recompila la app
4. Prueba el flujo completo de verificación

---

## 📝 Notas Importantes

- El modo desarrollo está **SOLO** para facilitar el desarrollo
- **NUNCA** publiques la app con `SKIP_EMAIL_VERIFICATION = true`
- Los logs con emojis (✅ ❌ ⚠️) te ayudarán a identificar problemas rápidamente
- Firebase tiene límites en el plan gratuito (100 emails/día)

---

## 🆘 Si Nada Funciona

1. Revisa los logs completos: `adb logcat > logs.txt`
2. Busca errores de Firebase
3. Contacta al soporte de Firebase desde la consola
4. Verifica que tu proyecto de Firebase esté activo y configurado correctamente

---

## 📞 Información de Depuración

Cuando pidas ayuda, comparte:
- Los logs de `AuthManager`
- Captura de pantalla de Firebase Console > Authentication > Templates
- El proveedor de email que estás usando (Gmail, Outlook, etc.)
- Si el email llega a spam o no llega en absoluto
