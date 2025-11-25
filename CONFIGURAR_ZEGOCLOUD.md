# 🎥 Configurar ZegoCloud para Live Streaming

## ✅ Agora Eliminado

Agora SDK ha sido eliminado del proyecto. Ahora usaremos ZegoCloud.

## 📋 Pasos para Configurar ZegoCloud

### 1. Crear Cuenta en ZegoCloud

1. Ve a: https://console.zegocloud.com
2. Crea una cuenta o inicia sesión
3. Crea un nuevo proyecto o selecciona uno existente

### 2. Obtener Credenciales

1. En el dashboard, ve a **"Project Management"**
2. Selecciona tu proyecto
3. Copia:
   - **App ID** (número largo)
   - **App Sign** (string de 64 caracteres)

### 3. Configurar la Dependencia

La dependencia correcta de ZegoCloud depende de tu caso de uso:

#### Opción A: SDK Completo (Recomendado para Live Streaming)
```kotlin
// En app/build.gradle.kts
dependencies {
    // ZegoCloud Express SDK
    implementation("im.zego:express-video:3.14.5")
}
```

#### Opción B: SDK Básico
```kotlin
implementation("im.zego:zego-express-engine:3.14.5")
```

### 4. Agregar Repositorio Maven

En `settings.gradle.kts`, asegúrate de tener:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://storage.zego.im/maven2") }
    }
}
```

### 5. Configurar ZegoConfig.kt

1. Abre `app/src/main/java/com/metu/hypematch/ZegoConfig.kt`
2. Reemplaza los valores:

```kotlin
object ZegoConfig {
    const val APP_ID: Long = TU_APP_ID_AQUI // Número largo
    const val APP_SIGN: String = "TU_APP_SIGN_AQUI" // String de 64 caracteres
}
```

### 6. Sincronizar Gradle

```bash
./gradlew --refresh-dependencies
./gradlew assembleDebug
```

## 🔧 Versiones Disponibles

Puedes verificar las versiones disponibles en:
- https://storage.zego.im/maven2/im/zego/express-video/

Versiones recomendadas:
- `3.14.5` - Estable
- `3.15.0` - Más reciente (si está disponible)

## 📱 Permisos Necesarios

Agrega en `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.CAMERA" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

## 🚀 Uso Básico

```kotlin
import im.zego.zegoexpress.ZegoExpressEngine
import im.zego.zegoexpress.constants.ZegoScenario

// Inicializar
val engine = ZegoExpressEngine.createEngine(
    ZegoConfig.APP_ID,
    ZegoConfig.APP_SIGN,
    true,
    ZegoScenario.LIVE,
    application,
    null
)

// Iniciar transmisión
engine.startPublishingStream("stream_id")

// Ver transmisión
engine.startPlayingStream("stream_id", canvas)
```

## 📚 Documentación Oficial

- Documentación: https://docs.zegocloud.com/
- SDK Android: https://docs.zegocloud.com/article/5562
- Ejemplos: https://github.com/zegoim/zego-express-example-topics-android

## ⚠️ Notas Importantes

1. **NO subas `ZegoConfig.kt` a GitHub** - Ya está en `.gitignore`
2. **Usa `ZegoConfig.example.kt`** como referencia
3. **En producción**, genera tokens en el backend, no uses App Sign en el cliente
4. **Prueba primero** con una versión específica antes de usar `latest.release`

## 🐛 Troubleshooting

### Error: Could not find im.zego:express-video

**Solución:**
1. Verifica que el repositorio Maven esté agregado en `settings.gradle.kts`
2. Usa una versión específica en lugar de `latest.release`
3. Sincroniza Gradle: `./gradlew --refresh-dependencies`

### Error: App ID o App Sign inválido

**Solución:**
1. Verifica que copiaste correctamente las credenciales
2. App ID debe ser un número Long (sin comillas)
3. App Sign debe ser un String (con comillas)

## 📞 Soporte

Si tienes problemas:
1. Revisa la documentación oficial
2. Verifica que la versión del SDK sea compatible
3. Consulta los ejemplos en GitHub de ZegoCloud
