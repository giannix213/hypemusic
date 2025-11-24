# 🔁 Nueva Lógica de Live - Carrusel de Videos

## 🎯 Cambio Principal

**El botón "Live" ahora abre un carrusel inmersivo de videos de concursos (tipo TikTok/Reels)**

---

## 📱 Flujo de Navegación Completo

```
┌─────────────────────────────────────────┐
│         BOTÓN "LIVE" (TAP)              │
└─────────────────────────────────────────┘
                  ↓
┌─────────────────────────────────────────┐
│   CARRUSEL DE VIDEOS DE CONCURSOS       │
│   (Pantalla Principal - Tipo TikTok)    │
│                                         │
│   [🎥 Iniciar Live] ← Superior Derecha │
│                                         │
│   Swipe ⬅️  → Catálogo                  │
│   Swipe ➡️  → Configuración             │
│   Swipe ⬆️  → Siguiente video           │
│   Swipe ⬇️  → Video anterior            │
└─────────────────────────────────────────┘
```

---

## 🎬 Pantalla Principal: Carrusel de Videos

### Características
- **Pantalla completa** (inmersiva, sin distracciones)
- **Reproducción automática** de videos
- **Navegación vertical** (swipe arriba/abajo)
- **Videos de todos los concursos** (Rápidos y Alto Impacto)

### Elementos en Pantalla

```
┌─────────────────────────────────────────┐
│                          [🎥 Iniciar]   │ ← Botón superior derecha
│                                         │
│                                         │
│          VIDEO EN REPRODUCCIÓN          │
│                                         │
│                                         │
│  @username                      ❤️ 234 │
│  Título del video               💬      │
│  Descripción...                 📤 1.5K │
│  [Mejor Cover de la Semana]             │
│                                         │
│  1 / 24                                 │ ← Indicador de posición
└─────────────────────────────────────────┘
```

### Información Mostrada
- **Username** del participante
- **Título** del video
- **Descripción** breve
- **Badge del concurso** (categoría)
- **Likes** y **vistas**
- **Botones de interacción** (like, comentar, compartir)

---

## 🎮 Gestos y Navegación

### Desde el Carrusel de Videos

| Gesto | Acción |
|-------|--------|
| Swipe ⬆️ (arriba) | Siguiente video de concurso |
| Swipe ⬇️ (abajo) | Video anterior |
| Swipe ⬅️ (izquierda) | Abrir catálogo de concursos |
| Swipe ➡️ (derecha) | Abrir configuración |
| Tap botón superior derecha | Ir a transmisiones en vivo |

---

## 🎥 Botón "Iniciar Live"

### Ubicación
- **Esquina superior derecha** del carrusel
- Botón circular con ícono de play
- Color rosa (PopArtColors.Pink)
- Tamaño: 48x48 dp

### Función
Al presionar este botón:
1. Se cierra el carrusel de videos
2. Se abre la pantalla de **transmisiones en vivo**
3. Muestra lives activos de otros usuarios

---

## 📺 Pantalla de Transmisiones en Vivo (Secundaria)

### Acceso
- Solo mediante el botón "Iniciar Live"
- No es la pantalla principal

### Características
- Muestra transmisiones en vivo activas
- Navegación vertical entre lives
- Botón de regreso para volver al carrusel

### Si no hay lives activos
```
┌─────────────────────────────────────────┐
│  [←]                                    │
│                                         │
│              📡                         │
│                                         │
│  Actualmente no hay                     │
│  transmisiones en vivo                  │
│                                         │
│  Swipe ⬅️ para ver catálogo             │
└─────────────────────────────────────────┘
```

---

## 📚 Catálogo de Concursos

### Acceso
- Swipe izquierda desde el carrusel
- Swipe izquierda desde transmisiones en vivo

### Contenido
- Tab "LIVES" - Próximos eventos
- Tab "CONCURSOS" - Rápidos y Alto Impacto
- Botón "Iniciar Live" al final

---

## 🔄 Comparación: Antes vs Ahora

### ❌ Antes
```
Live → Transmisión en vivo directa
     → Si no hay: Mensaje de error
     → Swipe para catálogo
```

### ✅ Ahora
```
Live → Carrusel de videos de concursos
     → Botón para transmisiones en vivo
     → Swipe para catálogo
     → Swipe para configuración
```

---

## 🎨 Diseño del Carrusel

### Estilo TikTok/Reels
- Pantalla completa negra
- Video centrado
- Overlay con gradiente
- Información en la parte inferior
- Botones de interacción a la derecha

### Botones de Interacción

```
Lado Derecho:
┌────────┐
│   ❤️   │ ← Like + contador
│  234   │
├────────┤
│   💬   │ ← Comentarios
│        │
├────────┤
│   📤   │ ← Compartir + vistas
│  1.5K  │
└────────┘
```

---

## 📊 Fuente de Videos

### Videos Mostrados
- Participaciones en **Concursos Rápidos**
- Participaciones en **Concursos de Alto Impacto**
- Ordenados por: Recientes, Populares, o Aleatorio

### Data Model
```kotlin
ContestEntry(
    id: String,
    userId: String,
    username: String,
    videoUrl: String,
    title: String,
    description: String,
    contestId: String,
    likes: Int,
    views: Int
)
```

---

## 💡 Ventajas de la Nueva UX

### Para Usuarios
1. **Descubrimiento inmediato** - Ven contenido al instante
2. **Navegación intuitiva** - Igual que TikTok/Reels
3. **Engagement alto** - Videos cortos y atractivos
4. **Participación fácil** - Ven ejemplos de otros

### Para Artistas
1. **Mayor visibilidad** - Sus videos se muestran primero
2. **Feedback inmediato** - Likes y comentarios
3. **Inspiración** - Ven trabajos de otros
4. **Competencia sana** - Motiva a mejorar

### Para la Plataforma
1. **Retención** - Usuarios pasan más tiempo
2. **Contenido UGC** - Videos generados por usuarios
3. **Viralidad** - Formato compartible
4. **Monetización** - Más oportunidades

---

## 🚀 Implementación Técnica

### Componentes Principales

1. **ContestVideoCarouselScreen**
   - Carrusel principal de videos
   - Navegación vertical
   - Botón "Iniciar Live"

2. **LiveStreamViewerScreen**
   - Pantalla de transmisiones en vivo
   - Acceso secundario
   - Botón de regreso

3. **LiveCatalogScreen**
   - Catálogo de concursos
   - Tabs y sub-tabs
   - Navegación por swipe

### Estados
```kotlin
var showLiveStreams by remember { mutableStateOf(false) }
var showCatalog by remember { mutableStateOf(false) }
var currentVideoIndex by remember { mutableStateOf(0) }
```

---

## 📱 Flujo Detallado

### Escenario 1: Usuario entra a Live
```
1. Tap en botón "Live"
2. Ve carrusel de videos de concursos
3. Swipe arriba/abajo para ver más videos
4. Tap en ❤️ para dar like
5. Swipe izquierda para ver catálogo
```

### Escenario 2: Usuario quiere ver transmisiones
```
1. Tap en botón "Live"
2. Ve carrusel de videos
3. Tap en botón "🎥 Iniciar Live" (superior derecha)
4. Ve transmisiones en vivo activas
5. Swipe arriba/abajo para cambiar de live
```

### Escenario 3: Usuario quiere participar
```
1. Tap en botón "Live"
2. Ve carrusel de videos
3. Swipe izquierda → Catálogo
4. Tap en "CONCURSOS"
5. Selecciona concurso
6. Graba y sube video
```

---

## 🎯 Próximos Pasos

### Fase 1: Básico ✅
- [x] Carrusel de videos
- [x] Navegación por gestos
- [x] Botón "Iniciar Live"
- [x] Pantalla de transmisiones

### Fase 2: Funcionalidad
- [ ] Reproducción real de videos
- [ ] Sistema de likes
- [ ] Comentarios en tiempo real
- [ ] Compartir videos

### Fase 3: Avanzado
- [ ] Algoritmo de recomendación
- [ ] Filtros por concurso
- [ ] Búsqueda de usuarios
- [ ] Analytics de visualizaciones

---

**Estado:** ✅ Implementado
**Versión:** 2.0
**Fecha:** Noviembre 2025
**Cambio:** De transmisiones en vivo a carrusel de videos
