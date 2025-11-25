# 🎥 Instrucciones para Configurar ZegoCloud Manualmente

## ✅ Credenciales Configuradas

Ya están configuradas en `ZegoConfig.kt`:
- **APP_ID:** 2127871637
- **APP_SIGN:** 56d09390b8f52b9cc8992915a0629ebeaa22a0a15aa2981b1d4f3fa4f9f7f87e

## ⚠️ Problema Actual

Las versiones del SDK de ZegoCloud no están disponibles en los repositorios Maven configurados.

## 🔧 Soluciones

### Opción 1: Descargar SDK Manualmente (Recomendado)

1. **Descargar el SDK:**
   - Ve a: https://www.zegocloud.com/downloads
   - O: https://doc-zh.zego.im/article/13789
   - Descarga "ZegoExpressEngine SDK for Android"

2. **Agregar el AAR al proyecto:**
   ```
   1. Crea la carpeta: app/libs/
   2. Copia el archivo .aar descargado a app/libs/
   3. En app/build.gradle.kts, agrega:
   
   implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.aar"))))
   ```

### Opción 2: Usar Maven Central con Versión Correcta

Verifica las versiones disponibles en:
- https://mvnrepository.com/artifact/im.zego/zego-express-engine

Luego actualiza en `app/build.gradle.kts`:
```kotlin
implementation("im.zego:zego-express-engine:VERSIÓN_DISPONIBLE")
```

### Opción 3: Usar UIKits de ZegoCloud (Más Fácil)

ZegoCloud ofrece UIKits pre-construidos que son más fáciles de integrar:

```kotlin
// En app/build.gradle.kts
implementation("com.github.ZEGOCLOUD:zego_uikit_prebuilt_call_android:+")
```

Y en `settings.gradle.kts`:
```kotlin
maven { url = uri("https://jitpack.io") }
maven { url = uri("https://www.jitpack.io") }
```

## 📚 Documentación Oficial

- **Guía de Inicio:** https://www.zegocloud.com/docs/video-call/quick-start/quick-start-(with-call-invitation)?platform=android&language=kotlin
- **SDK Reference:** https://doc-en.zego.im/article/api?doc=Express_Video_SDK_API~java_android~class
- **GitHub Examples:** https://github.com/ZEGOCLOUD

## 🎯 Pasos Recomendados

1. **Visita la documentación oficial** de ZegoCloud
2. **Descarga el SDK** desde su sitio web
3. **Sigue su guía de Quick Start** para Android
4. **Usa tus credenciales** que ya están configuradas en `ZegoConfig.kt`

## 💡 Alternativa: Usar Agora de Nuevo

Si ZegoCloud es muy complicado, podemos volver a Agora que tiene mejor soporte en Maven:

```kotlin
implementation("io.agora.rtc:full-sdk:4.2.6")
```

Agora es más maduro y tiene mejor documentación.

## 📞 Siguiente Paso

Decide qué opción prefieres:
1. Descargar SDK de ZegoCloud manualmente
2. Usar UIKits de ZegoCloud
3. Volver a Agora

Avísame cuál prefieres y te ayudo a implementarla.
