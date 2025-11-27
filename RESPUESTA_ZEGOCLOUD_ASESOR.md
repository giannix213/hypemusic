# 📧 Respuesta para Asesor de ZegoCloud

## Información del Proyecto HypeMatch

---

### 1. **Scenarios (Escenarios)**
Live streaming vertical estilo TikTok/Instagram Live para artistas musicales y sus fans.

**Funcionalidades principales:**
- Transmisiones en vivo verticales (portrait mode)
- Chat en tiempo real durante el live
- Sistema de regalos virtuales para los streamers
- Notificaciones push cuando un artista inicia un live
- Grabación automática de lives para verlos después
- Visualización de espectadores en tiempo real

---

### 2. **Use Case (Caso de Uso)**

**Aplicación:** Red social musical tipo TikTok con live streaming

**Flujo de Usuario:**

**Para Streamers (Artistas):**
- El artista presiona "Iniciar Live" desde su perfil
- Configura título y descripción del live
- Inicia transmisión en vivo vertical
- Ve comentarios y regalos en tiempo real
- Finaliza el live (se guarda automáticamente)

**Para Espectadores (Fans):**
- Reciben notificación cuando su artista favorito inicia live
- Entran al live desde el catálogo de "Lives Activos"
- Ven el stream en pantalla completa vertical
- Envían comentarios en tiempo real
- Pueden enviar regalos virtuales
- Swipe vertical para cambiar entre diferentes lives

---

### 3. **Platform (Plataforma)**
**Android** (nativo)

**Versiones soportadas:**
- minSdk: 24 (Android 7.0)
- targetSdk: 36 (Android 14+)
- compileSdk: 36

---

### 4. **Framework & Language**

**Lenguaje:** Kotlin 100%

**Framework UI:** Jetpack Compose (UI moderna de Android)

**Arquitectura:**
- MVVM (Model-View-ViewModel)
- Jetpack Compose para UI
- Coroutines para operaciones asíncronas
- StateFlow para manejo de estados

**Backend:**
- Firebase Firestore (base de datos)
- Firebase Storage (videos/imágenes)
- Firebase Auth (autenticación)
- Firebase Cloud Functions (notificaciones)

**Otras librerías:**
- ExoPlayer (reproducción de videos)
- CameraX (grabación de videos)
- Coil (carga de imágenes)
- Material3 (diseño)

---

### 5. **Company Name & Website**

**Nombre del Proyecto:** HypeMatch

**Tipo:** Startup / Proyecto independiente

**Descripción:** Plataforma de red social musical que conecta artistas emergentes con sus fans a través de videos cortos y transmisiones en vivo.

**Website:** En desarrollo (la app es el producto principal)

**GitHub Repository:** https://github.com/giannix213/hypemusic

---

### 6. **Email ID for ZEGO Account**

[TU_EMAIL_AQUÍ] ← **Reemplaza con tu email**

**App ID actual:** 2127871637  
**App Sign:** Ya configurado en el proyecto

---

## 📋 Información Técnica Adicional

### Requisitos Específicos de ZegoCloud:

1. **Live Streaming:**
   - Video vertical (9:16 aspect ratio)
   - Calidad adaptativa según conexión
   - Latencia baja (< 3 segundos)
   - Soporte para múltiples espectadores simultáneos

2. **Chat en Tiempo Real:**
   - Mensajes de texto durante el live
   - Sistema de emojis/reacciones
   - Moderación básica

3. **Características Deseadas:**
   - Grabación automática del live
   - Estadísticas de espectadores
   - Reconexión automática si se pierde conexión
   - Optimización de batería

### Integración Actual:

Ya tenemos implementado:
- ✅ Configuración de ZegoConfig.kt con credenciales
- ✅ LiveRecordingScreen.kt (pantalla para streamers)
- ✅ LiveStreamViewerScreen.kt (pantalla para espectadores)
- ✅ Activity nativa preparada
- ✅ Permisos configurados en AndroidManifest

**Solo necesitamos:**
- El SDK de ZegoCloud para Android
- Documentación de integración con Jetpack Compose
- Ejemplos de código para live streaming vertical

---

## 🎯 Objetivo Inmediato

Integrar el SDK de ZegoCloud en nuestra app Android (Kotlin + Jetpack Compose) para permitir:
1. Transmisiones en vivo verticales
2. Chat en tiempo real
3. Visualización de múltiples espectadores

---

## 📱 Screenshots de la App (Opcional)

Si tienes capturas de pantalla de tu app, puedes adjuntarlas para mostrar el contexto visual.

---

## ✉️ Plantilla de Email Lista para Copiar

```
Subject: HypeMatch - Android Live Streaming Integration Request

Hello,

Thank you for reaching out. Here are the details about my project:

1. Scenarios: 
Live streaming platform for music artists (similar to TikTok Live/Instagram Live) with vertical video streaming, real-time chat, and virtual gifts.

2. Use case: 
Social music app where artists can start live streams and fans can watch, comment, and send virtual gifts in real-time. Vertical (portrait) streaming with swipe navigation between different live streams.

3. Platform: 
Android (Native)
- minSdk: 24
- targetSdk: 36

4. Framework & language: 
Kotlin with Jetpack Compose
- Backend: Firebase (Firestore, Storage, Auth, Functions)
- Video: ExoPlayer, CameraX

5. Company name and website: 
HypeMatch - Independent startup project
GitHub: https://github.com/giannix213/hypemusic

6. Email id for ZEGO account: 
[TU_EMAIL_AQUÍ]

Current App ID: 2127871637

I have already implemented the UI screens and configuration for ZegoCloud integration. I need:
- ZegoCloud SDK for Android
- Integration documentation for Jetpack Compose
- Sample code for vertical live streaming

The code is ready, I just need the SDK to complete the integration.

Thank you for your assistance!

Best regards,
[TU_NOMBRE]
```

---

## 📝 Notas Importantes

- Reemplaza `[TU_EMAIL_AQUÍ]` con tu email real
- Reemplaza `[TU_NOMBRE]` con tu nombre
- Si tienes un sitio web, agrégalo
- Puedes adjuntar screenshots de la app si los tienes
- Menciona que ya tienes el App ID configurado (2127871637)

---

¡Buena suerte con ZegoCloud! 🚀
