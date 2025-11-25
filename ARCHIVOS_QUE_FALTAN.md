# 📋 Archivos que Necesitas de tu Hermana

## Archivos de Configuración que NO están en GitHub

Estos archivos contienen información sensible y cada desarrollador debe tenerlos:

### 1. google-services.json ⚠️ OBLIGATORIO
- **Ubicación:** `app/google-services.json`
- **Qué es:** Configuración de Firebase (base de datos, autenticación, etc.)
- **Cómo obtenerlo:** 
  - Opción 1: Tu hermana te lo envía
  - Opción 2: Descargarlo de Firebase Console

### 2. AgoraConfig.kt (si existe)
- **Ubicación:** `app/src/main/java/com/metu/hypematch/AgoraConfig.kt`
- **Qué es:** Credenciales de Agora (para live streaming)
- **Cómo obtenerlo:** Tu hermana te lo envía o te da las credenciales

### 3. local.properties (opcional)
- **Ubicación:** `local.properties`
- **Qué es:** Ruta del SDK de Android en tu computadora
- **Cómo obtenerlo:** Android Studio lo crea automáticamente

## ¿Qué hacer ahora?

1. **Contacta a tu hermana** y pídele:
   - `google-services.json`
   - `AgoraConfig.kt` (si existe)

2. **Coloca los archivos en las ubicaciones correctas**

3. **Rebuild el proyecto:** Build → Rebuild Project

## Verificar que todo esté bien

Ejecuta este comando en la terminal:
```
dir app\google-services.json
```

Si dice "File Not Found", el archivo no está.
Si muestra información del archivo, está correcto.
