# 📸 Guía Visual: Sistema de Fotos de Perfil en Videos

## 🔄 Flujo Completo del Sistema

```
┌─────────────────────────────────────────────────────────────┐
│                    USUARIO SUBE VIDEO                        │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│  1. VideoPreviewScreen - Usuario confirma subida            │
│     • Graba video                                            │
│     • Presiona "Subir"                                       │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│  2. LiveScreenNew.kt - onUpload()                            │
│     ✅ Obtener userId y username                             │
│     ✅ Subir video a Firebase Storage                        │
│     ✅ Obtener foto de perfil del usuario                    │
│     ✅ Crear entrada en Firestore                            │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│  3. FirebaseManager.createContestEntry()                     │
│     • Guarda en Firestore:                                   │
│       - userId                                               │
│       - username                                             │
│       - profilePictureUrl ✨ NUEVO                           │
│       - videoUrl                                             │
│       - title, description, contestId                        │
│       - likes, views, timestamp                              │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│  4. Firestore Database                                       │
│     Collection: contest_entries                              │
│     {                                                         │
│       "userId": "abc123",                                    │
│       "username": "Luna Beats",                              │
│       "profilePictureUrl": "https://...",  ✨                │
│       "videoUrl": "https://...",                             │
│       "title": "Mi video",                                   │
│       ...                                                     │
│     }                                                         │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│  5. LiveScreenNew - Cargar videos                            │
│     • firebaseManager.getAllContestEntries()                 │
│     • Carga todos los campos incluyendo profilePictureUrl   │
└─────────────────────────────────────────────────────────────┘
                            │
                            ▼
┌─────────────────────────────────────────────────────────────┐
│  6. ContestVideoCarouselScreen - Mostrar videos             │
│     • VerticalPager con videos                               │
│     • Para cada video:                                       │
│       ✅ Reproductor de video                                │
│       ✅ Foto de perfil (AsyncImage)                         │
│       ✅ Nombre de usuario                                   │
│       ✅ Título y descripción                                │
│       ✅ Botones de interacción                              │
└─────────────────────────────────────────────────────────────┘
```

## 🎨 Visualización en la UI

```
┌─────────────────────────────────────────────────────────────┐
│                                                               │
│                     [VIDEO REPRODUCIÉNDOSE]                   │
│                                                               │
│  [🔴 LIVE]                                    [<<< Swipe]    │
│                                                               │
│                                                               │
│                                                               │
│                                                               │
│                                                               │
│                                                               │
│                                                               │
│                                                               │
│  ┌─────────────────────────────┐                             │
│  │  👤 Luna Beats              │              ❤️ 1.2K       │
│  └─────────────────────────────┘                             │
│                                                 💬 45         │
│  Mi primer video de concurso                                 │
│  Participando en el mejor cover...             📤 856        │
│                                                               │
│  [Mejor Cover de la Semana]                                  │
│                                                               │
└─────────────────────────────────────────────────────────────┘
```

## 🔍 Detalle de la Foto de Perfil

### Con Foto de Perfil:
```
┌─────────────────────────────┐
│  [📸]  Luna Beats           │  ← AsyncImage carga la foto
└─────────────────────────────┘
   32dp   Nombre del usuario
```

### Sin Foto de Perfil (Fallback):
```
┌─────────────────────────────┐
│  [L]  Luna Beats            │  ← Avatar con inicial
└─────────────────────────────┘
   32dp   Nombre del usuario
   Rosa   (Primera letra)
```

## 💾 Estructura de Datos

### ContestEntry (Modelo)
```kotlin
data class ContestEntry(
    val id: String = "",
    val userId: String = "",              // ID del usuario
    val username: String = "",            // Nombre visible
    val profilePictureUrl: String = "",   // ✨ URL de la foto
    val videoUrl: String = "",            // URL del video
    val thumbnailUrl: String = "",
    val title: String = "",
    val description: String = "",
    val timestamp: Long = 0L,
    val likes: Int = 0,
    val views: Int = 0,
    val comments: Int = 0,
    val contestId: String = ""
)
```

### Firestore Document
```json
{
  "userId": "abc123",
  "username": "Luna Beats",
  "profilePictureUrl": "https://firebasestorage.../profile.jpg",
  "videoUrl": "https://firebasestorage.../video.mp4",
  "title": "Mi video de concurso",
  "description": "Participación en el mejor cover",
  "contestId": "Mejor Cover de la Semana",
  "likes": 0,
  "views": 0,
  "timestamp": 1700000000000
}
```

## 🎯 Componentes Clave

### 1. AsyncImage (Coil)
```kotlin
AsyncImage(
    model = currentVideo.profilePictureUrl,
    contentDescription = "Foto de perfil",
    modifier = Modifier
        .size(32.dp)
        .clip(CircleShape),
    contentScale = ContentScale.Crop
)
```

**Características:**
- ✅ Carga asíncrona (no bloquea la UI)
- ✅ Caché automático
- ✅ Manejo de errores
- ✅ Placeholder mientras carga

### 2. Avatar Fallback
```kotlin
Surface(
    modifier = Modifier.size(32.dp),
    shape = CircleShape,
    color = PopArtColors.Pink
) {
    Box(contentAlignment = Alignment.Center) {
        Text(
            currentVideo.username.firstOrNull()?.uppercase() ?: "U",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}
```

**Características:**
- ✅ Muestra inicial del nombre
- ✅ Color distintivo (rosa)
- ✅ Siempre visible (no hay "sin foto")

### 3. Navegación al Perfil
```kotlin
Surface(
    color = Color.Black.copy(alpha = 0.6f),
    shape = RoundedCornerShape(20.dp),
    modifier = Modifier.clickable {
        onNavigateToProfile(currentVideo.userId)
    }
) {
    Row {
        // Foto de perfil + Nombre
    }
}
```

**Características:**
- ✅ Clickeable
- ✅ Navega al perfil del usuario
- ✅ Feedback visual al tocar

## 📊 Flujo de Datos

```
Usuario → AuthManager → FirebaseManager → Firestore
  ↓           ↓              ↓               ↓
userId    getUserId()   getUserProfile()   users/{userId}
                            ↓
                    profileImageUrl
                            ↓
                    createContestEntry()
                            ↓
                    contest_entries/{entryId}
                            ↓
                    getAllContestEntries()
                            ↓
                    ContestEntry (modelo)
                            ↓
                    ContestVideoCarouselScreen
                            ↓
                    AsyncImage (UI)
```

## ✅ Checklist de Verificación

- [x] Modelo `ContestEntry` tiene campo `profilePictureUrl`
- [x] `createContestEntry()` acepta parámetro `profilePictureUrl`
- [x] `getAllContestEntries()` carga el campo `profilePictureUrl`
- [x] LiveScreen obtiene foto de perfil antes de crear entrada
- [x] UI usa `AsyncImage` para cargar fotos
- [x] UI tiene fallback con avatar de inicial
- [x] Foto de perfil es clickeable para navegar al perfil
- [x] Dependencia de Coil está en build.gradle
- [x] No hay errores de compilación

## 🎉 Resultado Final

Ahora cada video en el carrusel muestra:

1. **Foto de perfil real** del usuario que lo subió
2. **Nombre de usuario correcto**
3. **Avatar elegante** si no hay foto
4. **Navegación al perfil** al hacer clic
5. **Carga rápida** con caché de Coil
6. **Experiencia fluida** sin bloqueos

¡La experiencia de usuario ha mejorado significativamente! 🚀
