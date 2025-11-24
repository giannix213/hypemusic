# ✅ Checklist Rápido - Verificación de Email

## 🎯 Sigue estos pasos en orden:

### 1️⃣ Verifica la Plantilla en Firebase Console

En la pantalla que tienes abierta:

- [ ] Haz clic en **"Verificación de dirección de correo electrónico"**
- [ ] Verifica que el **toggle esté ACTIVADO** (debe estar azul/verde)
- [ ] Si está desactivado, **actívalo**
- [ ] Haz clic en **"Guardar"** si hiciste cambios

### 2️⃣ Ejecuta los Logs

Abre PowerShell y ejecuta:
```bash
cd [ruta-de-tu-proyecto]
.\ver_logs_email.bat
```

Deja esta ventana abierta.

### 3️⃣ Crea una Cuenta de Prueba

En tu app:
- [ ] Usa un email de **Gmail** (ejemplo: tuprueba@gmail.com)
- [ ] Crea una cuenta nueva
- [ ] Observa los logs en PowerShell

**¿Qué deberías ver?**
```
✅ Usuario creado exitosamente: [uid]
✅ Email de verificación enviado exitosamente
```

### 4️⃣ Revisa tu Email

En Gmail:
- [ ] Abre tu bandeja de entrada
- [ ] **IMPORTANTE:** Revisa la carpeta de **SPAM** 📧
- [ ] Revisa la carpeta de **Promociones**
- [ ] Busca un email de: `hype@hype-13966.firebaseapp.com`
- [ ] Espera **5 minutos** si no llega inmediatamente

### 5️⃣ Si NO llega el Email

Prueba envío manual desde Firebase:
- [ ] Ve a Firebase Console > **Authentication** > **Users**
- [ ] Encuentra tu usuario de prueba
- [ ] Haz clic en los **tres puntos** (⋮)
- [ ] Selecciona **"Send verification email"**
- [ ] Revisa tu email nuevamente (incluyendo spam)

### 6️⃣ Verifica Dominios Autorizados

En Firebase Console:
- [ ] Ve a **Authentication** > **Settings** (pestaña superior)
- [ ] Busca **"Authorized domains"**
- [ ] Verifica que esté: `hype-13966.firebaseapp.com`
- [ ] Si no está, agrégalo

### 7️⃣ Revisa el Plan de Firebase

- [ ] Ve a **Usage and billing** en Firebase Console
- [ ] Verifica que no hayas excedido límites
- [ ] Plan gratuito permite ~100 emails/día

---

## 🚨 Problemas Comunes

### Problema: Los logs muestran error
**Solución:** Copia el mensaje de error completo y compártelo

### Problema: Los logs muestran "✅ enviado" pero no llega
**Solución:** 
1. Revisa spam (90% de los casos está ahí)
2. Espera 10 minutos
3. Prueba con otro email de Gmail

### Problema: La plantilla está desactivada
**Solución:** Actívala y guarda los cambios

### Problema: No puedo ver los logs
**Solución:** 
```bash
# Alternativa manual:
adb logcat -s AuthManager:D FirebaseAuth:D
```

---

## 📊 Estado Actual

✅ **Código implementado correctamente**
✅ **Modo desarrollo activo** (puedes seguir trabajando)
⏳ **Esperando verificar configuración de Firebase**

---

## 💡 Mientras Tanto

Puedes seguir desarrollando normalmente porque el **modo desarrollo** está activo:
- Crea cuentas sin verificar
- Accede a todas las funciones
- El banner rosa te recordará que está en modo desarrollo

---

## 📝 Comparte Conmigo

Si sigue sin funcionar, comparte:

1. **Captura de la plantilla de email** (si está activada/desactivada)
2. **Los logs** que aparecen cuando creas una cuenta
3. **Captura de tus carpetas de email** (Inbox, Spam, Promociones)
4. **¿Qué proveedor de email usas?** (Gmail, Outlook, Yahoo, etc.)

---

## 🎯 Objetivo

Hacer que llegue el email de verificación para que puedas:
1. Desactivar el modo desarrollo
2. Tener verificación real de usuarios
3. Publicar la app con seguridad
