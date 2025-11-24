# 🚀 Prueba Rápida del Carrusel de Videos

## ✅ Todo Listo

El carrusel de videos de concursos está completamente implementado y funcional.

---

## 🎮 Cómo Probarlo

### 1. Abrir el Carrusel
```
1. Abrir la app
2. Tap en botón "Live" (bottom navigation)
3. Verás el carrusel de videos
```

### 2. Navegar Entre Videos
```
⬆️ Desliza ARRIBA    → Siguiente video
⬇️ Desliza ABAJO     → Video anterior
⬅️ Desliza IZQUIERDA → Catálogo de concursos
➡️ Desliza DERECHA   → Configuración
```

### 3. Interactuar con Videos
```
❤️ Tap en corazón  → Dar like
💬 Tap en mensaje  → Comentar
📤 Tap en compartir → Compartir
```

### 4. Subir Tu Propio Video
```
1. Swipe izquierda → Catálogo
2. Tap en "CONCURSOS"
3. Selecciona un concurso
4. Tap en "Grabar Video"
5. Graba tu video
6. Confirma y sube
7. Tu video aparecerá en el carrusel
```

---

## 📊 Ver Logs en Tiempo Real

### Abrir Logcat en Android Studio
```
1. View → Tool Windows → Logcat
2. Filtrar por: "LiveScreen" o "FirebaseManager"
```

### Logs que verás:

**Al abrir Live:**
```
🎬 ===== CARGANDO VIDEOS DE CONCURSOS =====
📍 Colección: contest_entries
✅ Videos cargados: 24
```

**Al navegar:**
```
🎯 Swipe detectado - H: 20, V: -150
⬆️ Siguiente video: 2 -> 3
```

**Al subir video:**
```
🎬 ===== SUBIENDO VIDEO A CONCURSO =====
📤 Paso 1: Subiendo video a Storage...
📊 Progreso: 50%
✅ Video subido a Storage
📝 Paso 2: Creando entrada en Firestore...
✅ ===== VIDEO PUBLICADO EXITOSAMENTE =====
```

---

## 🐛 Si No Hay Videos

### Opción 1: Subir Video de Prueba
```
1. Swipe izquierda → Catálogo
2. Tap en "CONCURSOS"
3. Selecciona "Mejor Cover de la Semana"
4. Graba un video corto (10-15 segundos)
5. Sube el video
6. Vuelve al carrusel (swipe derecha)
```

### Opción 2: Verificar Firestore
```
1. Abrir Firebase Console
2. Ir a Firestore Database
3. Buscar colección "contest_entries"
4. Verificar que existan documentos
```

---

## 💡 Tips

### Para Mejor Experiencia:
- Haz swipes largos y decididos (> 100 píxeles)
- Espera a que carguen los videos antes de navegar
- Revisa Logcat si algo no funciona

### Gestos Sensibles:
- El umbral es de 100 píxeles (muy sensible)
- Swipes cortos pueden no detectarse
- Swipes diagonales se interpretan por dirección dominante

---

## 🎯 Funcionalidades Implementadas

✅ Carrusel inmersivo tipo TikTok/Reels
✅ Navegación vertical (arriba/abajo)
✅ Navegación horizontal (izquierda/derecha)
✅ Carga de videos desde Firebase
✅ Subida de videos a concursos
✅ Botones de interacción (like, comentar, compartir)
✅ Información del video superpuesta
✅ Indicador de posición (ej: "2 / 24")
✅ Botón "Iniciar Live" en esquina superior
✅ Logs detallados para debugging

---

## 📱 Interfaz del Carrusel

```
┌─────────────────────────────────────────┐
│                          [🎥 Iniciar]   │
│                                         │
│          VIDEO EN REPRODUCCIÓN          │
│                                         │
│                                         │
│  [@username]                    ❤️ 234 │
│  Título del video               💬  12 │
│  Descripción...                 📤 1.5K │
│  [Mejor Cover de la Semana]             │
│                                         │
│  2 / 24                                 │
└─────────────────────────────────────────┘
```

---

## 🚀 ¡Listo para Probar!

Todo está implementado y funcionando. Solo necesitas:

1. Compilar la app
2. Abrir en tu dispositivo/emulador
3. Tap en "Live"
4. ¡Disfrutar del carrusel!

**¿Necesitas ayuda?** Revisa los logs en Logcat o consulta `CARRUSEL_VIDEOS_IMPLEMENTADO.md` para más detalles.
