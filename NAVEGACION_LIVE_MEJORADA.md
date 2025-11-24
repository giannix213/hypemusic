# 🎮 Navegación Live Mejorada - Guía Visual

## 📱 Flujo de Navegación Completo

```
┌─────────────────────────────────────────────────────────────┐
│                    BOTÓN "LIVE" (TAP)                       │
└─────────────────────────────────────────────────────────────┘
                            ↓
                    ¿Hay Lives Activos?
                            ↓
        ┌───────────────────┴───────────────────┐
        │                                       │
       SÍ                                      NO
        ↓                                       ↓
┌───────────────────┐                  ┌──────────────────┐
│  LIVE VIEWER      │                  │  NO LIVES        │
│  (Transmisión)    │                  │  (Mensaje)       │
└───────────────────┘                  └──────────────────┘
        │                                       │
        │ Swipe ⬅️                              │ Swipe ⬅️
        │ Swipe ➡️                              │ Swipe ➡️
        │ Swipe ⬆️                              │
        │ Swipe ⬇️                              │
        ↓                                       ↓
┌───────────────────┐                  ┌──────────────────┐
│  CATÁLOGO         │←─────────────────│  CATÁLOGO        │
│  Lives/Concursos  │                  │  Lives/Concursos │
└───────────────────┘                  └──────────────────┘
        │                                       
        │ Swipe ➡️ (Volver)                    
        │ Tap "Iniciar Live"                   
        ↓                                       
┌───────────────────┐                          
│  LIVE RECORDING   │                          
│  (Grabar Live)    │                          
└───────────────────┘                          
```

## 🎯 Gestos por Pantalla

### 1️⃣ Live Viewer (Transmisión Activa)

```
┌─────────────────────────────────────┐
│  ⬅️ SWIPE IZQUIERDA                 │
│     → Abrir Catálogo                │
│                                     │
│  ➡️ SWIPE DERECHA                   │
│     → Abrir Configuración           │
│                                     │
│  ⬆️ SWIPE ARRIBA                    │
│     → Siguiente Live                │
│                                     │
│  ⬇️ SWIPE ABAJO                     │
│     → Live Anterior                 │
└─────────────────────────────────────┘
```

**Elementos en Pantalla:**
- Badge "EN VIVO" (rosa, pulsante)
- Contador de espectadores (ej: 1.2K 👤)
- Nombre del artista y ubicación
- Indicador "Desliza para ver catálogo"

### 2️⃣ No Lives (Sin Transmisiones)

```
┌─────────────────────────────────────┐
│           📡                        │
│                                     │
│  Actualmente no hay                 │
│  transmisiones en vivo              │
│                                     │
│  ⬅️ SWIPE IZQUIERDA                 │
│     → Ver Catálogo                  │
│                                     │
│  ➡️ SWIPE DERECHA                   │
│     → Abrir Configuración           │
└─────────────────────────────────────┘
```

**Elementos en Pantalla:**
- Emoji grande 📡
- Mensaje claro y directo
- Indicador visual "⬅️ Desliza ⬅️"

### 3️⃣ Catálogo (Lives y Concursos)

```
┌─────────────────────────────────────┐
│  Catálogo          Desliza ➡️       │
│  ─────────────────────────────────  │
│                                     │
│  [LIVES] [CONCURSOS]                │
│                                     │
│  📋 Lista de eventos...             │
│                                     │
│  ➡️ SWIPE DERECHA                   │
│     → Volver al Live                │
│                                     │
│  ▶️ [Iniciar Live]                  │
│     (Botón al final)                │
└─────────────────────────────────────┘
```

**Elementos en Pantalla:**
- Header sin botón X
- Indicador "Desliza ➡️" en el header
- Tabs: LIVES / CONCURSOS
- Botón cuadrado "Iniciar Live" al final
- Cards de eventos/concursos

## 🎨 Diseño del Botón "Iniciar Live"

```
┌──────────────────────────────────┐
│                                  │
│  ┌────────┐                      │
│  │   ▶️   │  ← Botón 56x56dp     │
│  └────────┘                      │
│  Iniciar Live  ← Texto pequeño   │
│                                  │
└──────────────────────────────────┘
```

**Especificaciones:**
- Tamaño: 56x56 dp
- Forma: Cuadrado con bordes redondeados (12dp)
- Color: `colors.primary` (amarillo)
- Ícono: PlayArrow (32dp, negro)
- Texto: "Iniciar Live" (12sp, gris)
- Posición: Centrado, al final del LazyColumn

## 🔄 Tabla de Navegación Completa

| Desde | Gesto | Destino | Descripción |
|-------|-------|---------|-------------|
| Live Viewer | Swipe ⬅️ | Catálogo | Ver eventos y concursos |
| Live Viewer | Swipe ➡️ | Configuración | Abrir drawer/settings |
| Live Viewer | Swipe ⬆️ | Siguiente Live | Cambiar de transmisión |
| Live Viewer | Swipe ⬇️ | Live Anterior | Volver a live previo |
| No Lives | Swipe ⬅️ | Catálogo | Ver eventos programados |
| No Lives | Swipe ➡️ | Configuración | Abrir drawer/settings |
| Catálogo | Swipe ➡️ | Live Viewer | Volver a transmisión |
| Catálogo | Tap Botón | Live Recording | Iniciar transmisión propia |
| Catálogo | Tap Card | Detalle | Ver info del evento/concurso |

## 🎯 Cambios Clave vs Versión Anterior

### ❌ Eliminado
- Botón X en el catálogo
- Navegación con botones tradicionales
- Menú inicial de categorías

### ✅ Agregado
- Swipe derecha para configuración
- Swipe derecha en catálogo para volver
- Botón "Iniciar Live" minimalista
- Indicadores visuales de gestos
- Navegación vertical entre lives (swipe arriba/abajo)

## 💡 Tips de UX

1. **Feedback Visual:** Los swipes muestran indicadores sutiles
2. **Consistencia:** Swipe derecha siempre va "atrás" o a configuración
3. **Descubrimiento:** Indicadores de texto guían al usuario
4. **Minimalismo:** Botón de iniciar live no interrumpe la experiencia
5. **Fluidez:** Sin transiciones bruscas, todo es natural

## 🚀 Próximos Pasos

Para implementación completa:
1. Conectar con Firebase para lives reales
2. Implementar streaming de video
3. Agregar chat en vivo
4. Sistema de notificaciones para nuevos lives
5. Analytics de gestos para optimizar UX

---

**Versión:** 2.0
**Fecha:** Noviembre 2025
**Estado:** ✅ Implementado y listo para pruebas
