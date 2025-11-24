# Solución: No Llega el Email de Verificación

## Causas Comunes y Soluciones

### 1. **Configuración de Firebase Console** ⚙️

#### Verificar que el dominio esté autorizado:
1. Ve a Firebase Console: https://console.firebase.google.com
2. Selecciona tu proyecto
3. Ve a **Authentication** > **Settings** > **Authorized domains**
4. Asegúrate de que tu dominio esté en la lista

#### Verificar plantilla de email:
1. En Firebase Console, ve a **Authentication** > **Templates**
2. Selecciona **Email address verification**
3. Verifica que:
   - El remitente esté configurado (por defecto es `noreply@[tu-proyecto].firebaseapp.com`)
   - La plantilla esté habilitada
   - El idioma esté configurado

### 2. **Revisar Carpeta de Spam** 📧

Los emails de Firebase a menudo caen en spam. Revisa:
- Carpeta de Spam/Correo no deseado
- Carpeta de Promociones (Gmail)
- Carpeta de Actualizaciones (Gmail)

### 3. **Verificar el Email en Logcat** 🔍

Agrega logs para confirmar que se está enviando:

```kotlin
// En signUpWithEmail, después de enviar el email
android.util.Log.d("AuthManager", "Email de verificación enviado a: ${result.user?.email}")
```

### 4. **Probar con Diferentes Proveedores de Email** 📮

Algunos proveedores bloquean emails automáticos:
- ✅ Gmail (generalmente funciona bien)
- ✅ Outlook/Hotmail
- ⚠️ Yahoo (a veces bloquea)
- ⚠️ Dominios corporativos (pueden tener filtros estrictos)

### 5. **Verificar Cuota de Firebase** 📊

Firebase tiene límites en el plan gratuito:
1. Ve a Firebase Console > **Authentication** > **Usage**
2. Verifica que no hayas excedido el límite de emails diarios

### 6. **Configurar un Dominio Personalizado** (Opcional) 🌐

Para mejorar la entrega:
1. Firebase Console > **Authentication** > **Templates**
2. Configura un dominio personalizado verificado
3. Esto mejora la reputación del remitente

### 7. **Usar ActionCodeSettings para Personalizar** 🎨

Puedes personalizar el email con configuraciones adicionales:

```kotlin
// En AuthManager.kt
suspend fun sendEmailVerificationWithSettings() {
    try {
        val user = auth.currentUser ?: throw Exception("No hay usuario autenticado")
        
        val actionCodeSettings = com.google.firebase.auth.ActionCodeSettings.newBuilder()
            .setUrl("https://tu-app.page.link/verify") // Deep link a tu app
            .setHandleCodeInApp(true)
            .setAndroidPackageName(
                "com.metu.hypematch",
                true, // Instalar app si no está instalada
                null  // Versión mínima
            )
            .build()
        
        user.sendEmailVerification(actionCodeSettings).await()
        android.util.Log.d("AuthManager", "Email enviado con configuración personalizada")
    } catch (e: Exception) {
        android.util.Log.e("AuthManager", "Error: ${e.message}", e)
        throw e
    }
}
```

### 8. **Verificar Errores en Logcat** 🐛

Ejecuta la app y revisa los logs:
```bash
adb logcat | grep -i "AuthManager\|FirebaseAuth"
```

Busca mensajes como:
- "Email de verificación enviado"
- Errores de Firebase
- Problemas de red

### 9. **Probar Manualmente desde Firebase Console** 🧪

Para confirmar que Firebase puede enviar emails:
1. Firebase Console > **Authentication** > **Users**
2. Encuentra tu usuario
3. Haz clic en los tres puntos > **Send verification email**
4. Si esto tampoco funciona, el problema está en la configuración de Firebase

### 10. **Esperar Unos Minutos** ⏰

A veces los emails tardan:
- Normalmente llegan en 1-2 minutos
- En casos raros pueden tardar hasta 10-15 minutos
- Revisa periódicamente

## Solución Temporal: Modo de Desarrollo

Mientras solucionas el problema, puedes permitir acceso sin verificación en desarrollo:

```kotlin
// En AuthScreen.kt, en el botón de verificación
onCheckVerification = {
    // SOLO PARA DESARROLLO - REMOVER EN PRODUCCIÓN
    val isDevelopment = true // Cambiar a false en producción
    
    if (isDevelopment) {
        // Permitir acceso sin verificar (solo desarrollo)
        onAuthSuccess()
    } else {
        // Código normal de verificación
        isLoading = true
        scope.launch {
            try {
                authManager.reloadUser()
                if (authManager.isEmailVerified()) {
                    onAuthSuccess()
                } else {
                    errorMessage = "Email no verificado"
                }
                isLoading = false
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
                isLoading = false
            }
        }
    }
}
```

## Checklist de Verificación ✅

- [ ] Revisar carpeta de spam
- [ ] Verificar configuración en Firebase Console
- [ ] Probar con Gmail
- [ ] Revisar logs en Logcat
- [ ] Verificar cuota de Firebase
- [ ] Esperar 5-10 minutos
- [ ] Probar envío manual desde Firebase Console
- [ ] Verificar conexión a internet
- [ ] Confirmar que el email es válido

## Comando para Ver Logs en Tiempo Real

```bash
# Windows PowerShell
adb logcat -s AuthManager:D FirebaseAuth:D

# Ver todos los logs relacionados con email
adb logcat | Select-String "email|verification|AuthManager"
```

## Contacto con Soporte de Firebase

Si nada funciona:
1. Ve a Firebase Console
2. Haz clic en el ícono de ayuda (?)
3. Selecciona "Contact Support"
4. Describe el problema: "Email verification not being sent"
