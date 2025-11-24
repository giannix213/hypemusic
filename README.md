# 🎬 HypeMatch - Live Streaming App

App de streaming en vivo con Agora SDK y Firebase, desarrollada en Kotlin con Jetpack Compose.

## ✨ Características

- 🔴 **Live Streaming** en tiempo real con Agora
- 👥 **Múltiples espectadores** simultáneos
- 💬 **Chat en vivo** (próximamente)
- 🎵 **Catálogo de música** y videos
- 👤 **Perfiles de usuario** con galería
- 📱 **UI moderna** con Jetpack Compose

## 🚀 Setup para Desarrolladores

### Requisitos Previos

- Android Studio Hedgehog o superior
- JDK 17
- Cuenta de Firebase
- Cuenta de Agora
- Git instalado

### 1. Clonar el Repositorio

```bash
git clone https://github.com/TU_USUARIO/hypematch-app.git
cd hypematch-app
```

### 2. Configurar Firebase

1. Ve a [Firebase Console](https://console.firebase.google.com)
2. Selecciona tu proyecto (o crea uno nuevo)
3. Descarga `google-services.json`
4. Colócalo en `app/google-services.json`

**Firestore Rules necesarias:**
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /live_sessions/{sessionId} {
      allow read: if true;
      allow write: if request.auth != null;
    }
    match /songs/{songId} {
      allow read: if true;
      allow write: if request.auth != null;
    }
    match /users/{userId} {
      allow read: if true;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
  }
}
```

### 3. Configurar Agora

1. Ve a [Agora Console](https://console.agora.io)
2. Crea un proyecto o selecciona uno existente
3. Copia el **App ID** y **App Certificate**
4. Copia el archivo de ejemplo:
   ```bash
   copy app\src\main\java\com\metu\hypematch\AgoraConfig.example.kt AgoraConfig.kt
   ```
5. Edita `AgoraConfig.kt` con tus credenciales:
   ```kotlin
   object AgoraConfig {
       const val APP_ID = "tu_app_id_aqui"
       const val APP_CERTIFICATE = "tu_certificate_aqui"
   }
   ```

### 4. Desplegar Cloud Functions (para tokens de Agora)

```bash
cd functions
npm install
firebase deploy --only functions:generateAgoraToken
```

### 5. Compilar y Ejecutar

```bash
# Compilar
gradlew assembleDebug

# O desde Android Studio
# Run > Run 'app'
```

## 📱 Probar Live Streaming

### Dispositivo Emisor:
1. Abre la app
2. Ve a la pestaña "Lives"
3. Presiona el botón "Iniciar Live"
4. Acepta permisos de cámara y micrófono
5. Verás "LIVE 🔴" cuando esté activo

### Dispositivo Espectador:
1. Abre la app en otro dispositivo
2. Ve a la pestaña "Lives"
3. Desliza para ver Lives activos
4. Toca el Live para unirte

## 🔧 Scripts Útiles

### Compilar y probar Live:
```bash
probar-live.bat
```

### Subir cambios a GitHub:
```bash
subir-a-github.bat
```

### Ver logs en tiempo real:
```bash
adb logcat -s FirebaseManager:D LiveLauncher:D -v time
```

## 📚 Documentación

- [Guía de Inicio Rápido](INICIO_RAPIDO_LIVE.md)
- [Índice de Documentación](INDICE_SOLUCION_LIVE.md)
- [Guía de GitHub](GUIA_SUBIR_A_GITHUB.md)
- [Comandos Rápidos](COMANDOS_RAPIDOS_LIVE.md)

## 🏗️ Arquitectura

```
app/
├── src/main/java/com/metu/hypematch/
│   ├── MainActivity.kt              # Actividad principal
│   ├── FirebaseManager.kt           # Gestión de Firebase
│   ├── AuthManager.kt               # Autenticación
│   ├── LiveLauncherScreen.kt        # Pantalla de inicio de Live
│   ├── LiveRecordingScreen.kt       # Pantalla de transmisión
│   ├── LiveStreamViewerScreen.kt    # Pantalla de espectador
│   ├── LiveScreenNew.kt             # Pantalla principal de Lives
│   └── LiveSession.kt               # Modelo de datos
├── functions/                        # Cloud Functions
│   └── index.js                     # Generación de tokens Agora
└── google-services.json             # Configuración Firebase (no en Git)
```

## 🤝 Contribuir

### Flujo de Trabajo

1. **Actualizar antes de empezar:**
   ```bash
   git pull
   ```

2. **Crear rama para tu funcionalidad:**
   ```bash
   git checkout -b feature/nombre-funcionalidad
   ```

3. **Hacer cambios y commit:**
   ```bash
   git add .
   git commit -m "Add: Descripción de los cambios"
   ```

4. **Subir rama:**
   ```bash
   git push -u origin feature/nombre-funcionalidad
   ```

5. **Crear Pull Request en GitHub**

### Convenciones de Commits

- `Add:` Nueva funcionalidad
- `Fix:` Corrección de bugs
- `Improve:` Mejoras de rendimiento
- `Refactor:` Refactorización de código
- `Docs:` Cambios en documentación
- `Style:` Cambios de formato

## 🐛 Troubleshooting

### El Live no aparece para espectadores

1. Verificar logs del emisor:
   ```bash
   adb logcat -s FirebaseManager:D | findstr "Sesión creada"
   ```

2. Verificar Firebase Console:
   - Debe existir documento en `live_sessions`
   - Campo `isActive` debe ser `true`

3. Verificar Firestore Rules:
   - Debe permitir lectura sin autenticación

### Error al obtener token de Agora

1. Verificar que Cloud Function esté desplegada:
   ```bash
   firebase functions:list
   ```

2. Ver logs de la función:
   ```bash
   firebase functions:log --only generateAgoraToken
   ```

### Error de compilación

```bash
# Limpiar y recompilar
gradlew clean assembleDebug
```

## 📄 Licencia

[Especificar licencia]

## 👥 Equipo

- [Tu Nombre] - Desarrollador Principal
- [Colaborador 1] - [Rol]
- [Colaborador 2] - [Rol]

## 📞 Contacto

- Email: tu@email.com
- GitHub: [@tu_usuario](https://github.com/tu_usuario)

---

**Última actualización:** Noviembre 2025
**Versión:** 1.0.0
