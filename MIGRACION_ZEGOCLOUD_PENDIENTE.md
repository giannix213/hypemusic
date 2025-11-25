# 🚧 Migración a ZegoCloud Pendiente

## ⚠️ Estado Actual

Los archivos de Live Streaming aún usan Agora SDK y necesitan ser migrados a ZegoCloud.

## 📁 Archivos que Necesitan Migración

1. **LiveRecordingScreen.kt** - Pantalla del streamer (broadcaster)
2. **LiveStreamViewerScreen.kt** - Pantalla del espectador (viewer)

## 🔄 Pasos para Migrar

### 1. Descargar SDK de ZegoCloud

Primero necesitas el SDK de ZegoCloud funcionando. Opciones:

**Opción A: Descargar AAR manualmente**
```
1. Ve a: https://www.zegocloud.com/downloads
2. Descarga "ZegoExpressEngine SDK for Android"
3. Coloca el .aar en app/libs/
4. En app/build.gradle.kts:
   implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))
```

**Opción B: Usar UIKit (Más fácil)**
```kotlin
// En app/build.gradle.kts
implementation("com.github.ZEGOCLOUD:zego_uikit_prebuilt_call_android:+")
```

### 2. Cambios Necesarios en el Código

#### Imports a Cambiar:

**Agora (Actual):**
```kotlin
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtc2.video.VideoEncoderConfiguration
```

**ZegoCloud (Nuevo):**
```kotlin
import im.zego.zegoexpress.ZegoExpressEngine
import im.zego.zegoexpress.callback.IZegoEventHandler
import im.zego.zegoexpress.constants.ZegoScenario
import im.zego.zegoexpress.constants.ZegoVideoConfigPreset
import im.zego.zegoexpress.entity.ZegoCanvas
import im.zego.zegoexpress.entity.ZegoUser
```

#### Inicialización del Engine:

**Agora:**
```kotlin
val config = RtcEngineConfig().apply {
    mContext = context
    mAppId = AgoraConfig.APP_ID
    mEventHandler = IRtcEngineEventHandler() { ... }
}
agoraEngine = RtcEngine.create(config)
```

**ZegoCloud:**
```kotlin
val engine = ZegoExpressEngine.createEngine(
    ZegoConfig.APP_ID,
    ZegoConfig.APP_SIGN,
    true,
    ZegoScenario.LIVE,
    context.applicationContext,
    object : IZegoEventHandler() {
        // Event handlers
    }
)
```

#### Iniciar Transmisión:

**Agora:**
```kotlin
val options = ChannelMediaOptions().apply {
    channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
    clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
    publishCameraTrack = true
    publishMicrophoneTrack = true
}
agoraEngine?.joinChannel(agoraToken, channelName, 0, options)
agoraEngine?.startPreview()
```

**ZegoCloud:**
```kotlin
// Iniciar preview
engine.startPreview(canvas)

// Iniciar transmisión
engine.startPublishingStream(streamID)
```

#### Ver Transmisión (Viewer):

**Agora:**
```kotlin
val options = ChannelMediaOptions().apply {
    channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
    clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
    autoSubscribeAudio = true
    autoSubscribeVideo = true
}
agoraEngine?.joinChannel(agoraToken, channelName, 0, options)
```

**ZegoCloud:**
```kotlin
// Ver transmisión
engine.startPlayingStream(streamID, canvas)
```

#### Configurar Video:

**Agora:**
```kotlin
val videoConfig = VideoEncoderConfiguration().apply {
    dimensions = VideoEncoderConfiguration.VD_1280x720
    frameRate = VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_30
    bitrate = VideoEncoderConfiguration.STANDARD_BITRATE
}
agoraEngine?.setVideoEncoderConfiguration(videoConfig)
```

**ZegoCloud:**
```kotlin
engine.setVideoConfig(ZegoVideoConfig(ZegoVideoConfigPreset.PRESET_720P))
```

#### Limpiar Recursos:

**Agora:**
```kotlin
agoraEngine?.leaveChannel()
RtcEngine.destroy()
```

**ZegoCloud:**
```kotlin
engine.stopPublishingStream()
engine.stopPreview()
ZegoExpressEngine.destroyEngine(null)
```

### 3. Estructura Recomendada

Crea un archivo `ZegoLiveManager.kt` para encapsular la lógica:

```kotlin
class ZegoLiveManager(private val context: Context) {
    private var engine: ZegoExpressEngine? = null
    
    fun initEngine(onEvent: (String) -> Unit) {
        engine = ZegoExpressEngine.createEngine(...)
    }
    
    fun startBroadcasting(streamID: String, canvas: ZegoCanvas) {
        engine?.startPreview(canvas)
        engine?.startPublishingStream(streamID)
    }
    
    fun startViewing(streamID: String, canvas: ZegoCanvas) {
        engine?.startPlayingStream(streamID, canvas)
    }
    
    fun cleanup() {
        engine?.stopPublishingStream()
        engine?.stopPreview()
        ZegoExpressEngine.destroyEngine(null)
    }
}
```

### 4. Documentación Útil

- **Quick Start:** https://www.zegocloud.com/docs/video-call/quick-start/quick-start-(with-call-invitation)?platform=android&language=kotlin
- **API Reference:** https://doc-en.zego.im/article/api?doc=Express_Video_SDK_API~java_android~class
- **Ejemplos:** https://github.com/ZEGOCLOUD/zego_uikit_prebuilt_call_example_android

### 5. Diferencias Clave

| Característica | Agora | ZegoCloud |
|----------------|-------|-----------|
| Inicialización | RtcEngine.create() | ZegoExpressEngine.createEngine() |
| Autenticación | Token opcional | App Sign requerido |
| Canales | joinChannel() | startPublishingStream() |
| Roles | BROADCASTER/AUDIENCE | Implícito en publish/play |
| Preview | startPreview() | startPreview(canvas) |
| Cleanup | RtcEngine.destroy() | ZegoExpressEngine.destroyEngine() |

## 🎯 Próximos Pasos

1. **Decide qué opción usar:**
   - SDK completo (más control)
   - UIKit (más rápido)

2. **Descarga e integra el SDK**

3. **Crea ZegoLiveManager.kt** con la lógica encapsulada

4. **Migra LiveRecordingScreen.kt** usando el nuevo manager

5. **Migra LiveStreamViewerScreen.kt** usando el nuevo manager

6. **Prueba la funcionalidad** de live streaming

## 💡 Recomendación

Te recomiendo usar el **UIKit de ZegoCloud** porque:
- ✅ Más fácil de integrar
- ✅ UI pre-construida
- ✅ Menos código para mantener
- ✅ Actualizaciones automáticas

Si necesitas más control, usa el SDK completo.

## 📞 ¿Necesitas Ayuda?

Avísame qué opción prefieres y te ayudo a implementarla paso a paso.
