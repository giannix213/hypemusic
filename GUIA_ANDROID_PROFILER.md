# 🔍 GUÍA: USAR ANDROID PROFILER PARA DIAGNOSTICAR LENTITUD

## 🎯 Objetivo
Identificar exactamente qué está causando los 3-4 segundos de retraso usando el Android Profiler.

---

## 📋 PASO 1: Configurar Android Profiler

### A. Abrir el Profiler

1. **Conecta tu dispositivo** o inicia el emulador
2. En Android Studio, ve a: **View → Tool Windows → Profiler**
3. O presiona: `Alt + 6` (Windows) / `Cmd + 6` (Mac)

### B. Iniciar Sesión de Profiling

1. Click en el botón **"+"** (New Session)
2. Selecciona tu dispositivo
3. Selecciona la app: **com.metu.hypematch**
4. Click en **"Run"**

---

## 📋 PASO 2: Grabar CPU Trace

### A. Seleccionar CPU Profiler

1. En la barra superior del Profiler, click en **"CPU"**
2. Verás un gráfico de uso de CPU en tiempo real

### B. Iniciar Grabación

1. Click en el botón **"Record"** (círculo rojo)
2. Selecciona: **"System Trace"** (recomendado para UI)
3. Click en **"OK"**

### C. Reproducir el Problema

1. **Navega a ProfileScreen:**
   - Abre la app
   - Tap en el icono de "Perfil"
   - Observa el tiempo de carga

2. **Navega a DiscoverScreen:**
   - Tap en el icono de "Descubre"
   - Observa el tiempo de carga
   - Cambia de canción (swipe)

3. **Detén la grabación:**
   - Click en el botón **"Stop"** (cuadrado)
   - Espera a que se procese el trace

---

## 📋 PASO 3: Analizar el Trace

### A. Vista General

Verás una línea de tiempo con:
- **Threads:** Hilos de ejecución
- **Main Thread:** El más importante (UI)
- **Frames:** Cuadros de renderizado

### B. Buscar Problemas en Main Thread

#### 🔴 Problema 1: Frames Lentos (> 16ms)

**Qué buscar:**
```
Main Thread
  └─ Choreographer.doFrame()  [50ms] ← ❌ PROBLEMA
       └─ performTraversals()
            └─ onDraw()
```

**Significa:** El renderizado de UI es lento

**Solución:**
- Reducir complejidad de UI
- Usar LazyColumn en lugar de Column
- Optimizar imágenes

#### 🔴 Problema 2: Operaciones de Red en Main Thread

**Qué buscar:**
```
Main Thread
  └─ FirebaseManager.getAllSongs()  [2000ms] ← ❌ PROBLEMA
       └─ Firestore.get()
```

**Significa:** Llamadas de red bloqueando UI

**Solución:**
- Ya implementado: `withContext(Dispatchers.IO)`
- Verificar que todas las funciones usen Dispatchers.IO

#### 🔴 Problema 3: Decodificación de Imágenes

**Qué buscar:**
```
Main Thread
  └─ BitmapFactory.decodeStream()  [300ms] ← ❌ PROBLEMA
       └─ Coil.load()
```

**Significa:** Imágenes se decodifican en hilo principal

**Solución:**
- Implementar precarga de imágenes (Fase 2)
- Configurar Coil para usar Dispatchers.IO

#### 🔴 Problema 4: Garbage Collection (GC)

**Qué buscar:**
```
GC: Alloc concurrent mark sweep GC freed [100ms] ← ❌ PROBLEMA
```

**Significa:** Demasiados objetos temporales

**Solución:**
- Usar `remember` para objetos pesados
- Evitar crear listas nuevas en cada recomposición
- Reutilizar objetos cuando sea posible

#### 🔴 Problema 5: Recomposiciones Excesivas

**Qué buscar:**
```
Main Thread
  └─ Composer.recompose()  [repetido muchas veces]
       └─ DiscoverScreen()
```

**Significa:** Composables se recomponen demasiado

**Solución:**
- Usar `remember` para estado derivado
- Estabilizar managers con CompositionLocal
- Usar `key()` en LazyColumn

---

## 📋 PASO 4: Identificar el Cuello de Botella

### A. Ordenar por Tiempo

1. En la vista de trace, click derecho en Main Thread
2. Selecciona: **"Sort by Wall Clock Time"**
3. Busca las funciones que toman más tiempo

### B. Expandir Llamadas

1. Click en una función lenta
2. Expande el árbol de llamadas
3. Identifica la función más interna que toma tiempo

### C. Tomar Nota

Anota:
- **Función:** Nombre de la función lenta
- **Tiempo:** Cuánto tarda (ms)
- **Thread:** En qué hilo se ejecuta
- **Frecuencia:** Cuántas veces se llama

---

## 📊 EJEMPLO DE ANÁLISIS

### Caso 1: ProfileScreen Lento

**Trace encontrado:**
```
Main Thread [3200ms total]
  └─ LaunchedEffect  [3200ms]
       ├─ getFullUserProfile()  [1200ms]
       ├─ getUserSongMedia()    [1000ms]
       └─ getUserStories()      [1000ms]
```

**Diagnóstico:** Carga secuencial (ya corregido en Fase 1)

**Solución aplicada:** Carga paralela con `async`

**Resultado esperado:**
```
Main Thread [1200ms total]
  └─ LaunchedEffect  [1200ms]
       └─ coroutineScope
            ├─ async { getFullUserProfile() }  [1200ms]
            ├─ async { getUserSongMedia() }    [1000ms]
            └─ async { getUserStories() }      [1000ms]
```

### Caso 2: DiscoverScreen Lento

**Trace encontrado:**
```
Main Thread [4000ms total]
  └─ LaunchedEffect  [4000ms]
       └─ getAllSongs()  [4000ms]
            └─ Firestore.get()  [3800ms]
                 └─ [100 documentos]
```

**Diagnóstico:** Carga de todas las canciones (ya corregido en Fase 1)

**Solución aplicada:** Paginación con límite de 10

**Resultado esperado:**
```
Main Thread [500ms total]
  └─ LaunchedEffect  [500ms]
       └─ getAllSongs(limit=10)  [500ms]
            └─ Firestore.get()  [480ms]
                 └─ [10 documentos]
```

### Caso 3: Imágenes Lentas (Fase 2)

**Trace encontrado:**
```
Main Thread [800ms total]
  └─ AsyncImage  [800ms]
       └─ BitmapFactory.decodeStream()  [750ms]
```

**Diagnóstico:** Decodificación de imagen en hilo principal

**Solución (Fase 2):** Precarga con Coil

**Resultado esperado:**
```
Main Thread [50ms total]
  └─ AsyncImage  [50ms]
       └─ [imagen ya en caché]
```

---

## 📋 PASO 5: Verificar Mejoras

### A. Grabar Nuevo Trace

Después de implementar optimizaciones:
1. Grabar nuevo trace
2. Repetir las mismas acciones
3. Comparar tiempos

### B. Comparar Resultados

| Acción | Antes | Después | Mejora |
|--------|-------|---------|--------|
| Abrir ProfileScreen | 3200ms | 1200ms | 62% |
| Abrir DiscoverScreen | 4000ms | 500ms | 87% |
| Cambiar canción | 800ms | 50ms | 94% |

---

## 🎯 CHECKLIST DE ANÁLISIS

### Antes de Optimizar
- [ ] Grabar trace de ProfileScreen
- [ ] Grabar trace de DiscoverScreen
- [ ] Grabar trace de cambio de canción
- [ ] Identificar funciones > 100ms en Main Thread
- [ ] Identificar GC frecuente
- [ ] Identificar recomposiciones excesivas

### Después de Optimizar
- [ ] Grabar nuevo trace de ProfileScreen
- [ ] Grabar nuevo trace de DiscoverScreen
- [ ] Grabar nuevo trace de cambio de canción
- [ ] Verificar que Main Thread < 500ms
- [ ] Verificar que no hay GC frecuente
- [ ] Verificar que frames < 16ms

---

## 🔍 PROBLEMAS COMUNES Y SOLUCIONES

### Problema: "No puedo ver el Main Thread"

**Solución:**
- Asegúrate de usar "System Trace" no "Java/Kotlin Method Trace"
- Zoom in en la línea de tiempo
- Busca el thread llamado "main" o "UI Thread"

### Problema: "El trace es muy grande"

**Solución:**
- Graba solo 5-10 segundos
- Enfócate en una acción específica
- Usa filtros para mostrar solo Main Thread

### Problema: "No veo nombres de funciones"

**Solución:**
- Asegúrate de compilar en modo Debug
- No uses ProGuard/R8 durante profiling
- Actualiza Android Studio a la última versión

---

## 📊 MÉTRICAS OBJETIVO

### Frames (60 FPS)
- ✅ Cada frame < 16ms
- ⚠️ Frame entre 16-32ms (lag visible)
- ❌ Frame > 32ms (lag severo)

### Main Thread
- ✅ Operaciones < 50ms
- ⚠️ Operaciones entre 50-100ms
- ❌ Operaciones > 100ms

### GC (Garbage Collection)
- ✅ GC cada 10+ segundos
- ⚠️ GC cada 5-10 segundos
- ❌ GC cada < 5 segundos

---

## 🎉 RESULTADO ESPERADO

Después de usar el Profiler, deberías:

1. ✅ Identificar exactamente qué funciones son lentas
2. ✅ Saber en qué thread se ejecutan
3. ✅ Tener datos concretos para optimizar
4. ✅ Poder medir el impacto de las optimizaciones

**El Profiler es la herramienta más poderosa para optimización** 🚀

---

## 📞 PRÓXIMOS PASOS

1. **Ejecuta el Profiler** en tu app actual
2. **Identifica los cuellos de botella** específicos
3. **Comparte los resultados** (screenshots del trace)
4. **Implementa las optimizaciones** correspondientes
5. **Verifica las mejoras** con nuevo trace

**¡El Profiler te dirá exactamente qué optimizar!** 🔍
