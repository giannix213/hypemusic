# 🚀 EJECUTAR LIMPIEZA AHORA - PASO A PASO

## ⚡ INICIO RÁPIDO

### PASO 1: Obtener Service Account Key (2 minutos)

```
1. Abre tu navegador
2. Ve a: https://console.firebase.google.com
3. Selecciona tu proyecto "HypeMatch" o similar
4. Click en el ícono de engranaje ⚙️ (arriba izquierda)
5. Click en "Configuración del proyecto"
6. Click en la pestaña "Cuentas de servicio"
7. Click en el botón "Generar nueva clave privada"
8. Confirma en el diálogo que aparece
9. Se descargará un archivo JSON
10. Renombra el archivo a: serviceAccountKey.json
11. Mueve el archivo a la carpeta: functions/
```

**Ubicación final:**
```
tu-proyecto/
  ├── functions/
  │   └── serviceAccountKey.json  ← AQUÍ
  ├── app/
  ├── limpiar-videos-concursos.js
  └── limpiar-videos-concursos.bat
```

---

### PASO 2: Ejecutar el Script (1 minuto)

#### Opción A: Doble Click (Más fácil)
```
1. Busca el archivo: limpiar-videos-concursos.bat
2. Doble click en él
3. Se abrirá una ventana de comandos
4. Lee la información que aparece
5. Presiona Enter para confirmar
6. Espera a que termine
7. Presiona Enter para cerrar
```

#### Opción B: Línea de Comandos
```bash
# Abre CMD o PowerShell en la carpeta del proyecto
# Ejecuta:
limpiar-videos-concursos.bat
```

---

### PASO 3: Verificar Resultado (30 segundos)

#### En la ventana de comandos verás:
```
========================================
  LIMPIEZA DE VIDEOS DE CONCURSOS
========================================

Verificando Node.js...
Node.js encontrado: v18.x.x

Ejecutando script de limpieza...

🧹 ===== INICIANDO LIMPIEZA DE VIDEOS DE CONCURSOS =====

📋 Paso 1: Obteniendo lista de videos...
📊 Total de videos encontrados: 5

📝 Videos que serán eliminados:
────────────────────────────────────────────────────────────────
1. ID: abc123
   👤 Usuario: Juan (user001)
   📝 Título: Mi video
   🏆 Concurso: Mejor Cover
   ❤️ Likes: 10 | 👁️ Views: 50

2. ID: def456
   ...

────────────────────────────────────────────────────────────────

⚠️  ADVERTENCIA: Esta acción eliminará TODOS los videos.
🔄 Procediendo con la eliminación en 3 segundos...

🗑️  Eliminando videos...

✅ ===== LIMPIEZA COMPLETADA =====
✅ Videos eliminados: 5
✅ La colección contest_entries está ahora vacía

📱 Ahora puedes:
   1. Abrir la app
   2. Ir al catálogo de concursos
   3. Grabar o subir nuevos videos
   4. Verificar que el carrusel funciona correctamente

========================================
  PROCESO COMPLETADO
========================================
Presione una tecla para continuar . . .
```

---

### PASO 4: Verificar en Firebase Console (30 segundos)

```
1. Ve a: https://console.firebase.google.com
2. Selecciona tu proyecto
3. Click en "Firestore Database" en el menú lateral
4. Busca la colección "contest_entries"
5. Debe estar vacía o no aparecer
```

**Si ves esto, está correcto:**
```
Firestore Database
  └── contest_entries (0 documentos)
```

---

### PASO 5: Verificar en la App (1 minuto)

```
1. Abre tu app en el emulador o dispositivo
2. Ve a la pantalla de Live (carrusel de videos)
3. Deberías ver:

   ┌──────────────────────────────┐
   │                              │
   │           🎬                 │
   │                              │
   │  No hay videos de            │
   │  concursos aún               │
   │                              │
   │  Sé el primero en participar │
   │                              │
   └──────────────────────────────┘
```

---

### PASO 6: Subir Video de Prueba (2 minutos)

```
1. En la app, swipe hacia la izquierda
2. Se abre el Catálogo
3. Click en cualquier concurso
4. Click en "GRABAR VIDEO"
5. Graba un video corto (5-10 segundos) O swipe arriba para galería
6. Click en "Detener"
7. En la preview, click en "SUBIR"
8. Espera a que se suba
9. Vuelve a la pantalla de Live
10. Tu video debe aparecer en el carrusel
```

---

### PASO 7: Verificar Funcionalidad (1 minuto)

#### Prueba estos gestos:
```
✅ Tap simple → Pausa/Reanuda
✅ Doble tap → Da like (corazón rojo)
✅ Long press → Pausa mientras presionas
✅ Swipe vertical → Cambia de video
✅ Swipe horizontal izquierda → Abre catálogo
✅ Click en perfil → Navega al perfil
✅ Click en badge concurso → Abre catálogo
```

#### Verifica estos elementos:
```
✅ Video se reproduce automáticamente
✅ Contador de likes funciona
✅ Botón de comentarios abre el diálogo
✅ Botón de compartir abre el selector
✅ Indicador de posición (ej: "1 / 1")
✅ Información del usuario visible
```

---

## ✅ RESULTADO ESPERADO

### Si TODO funciona correctamente:
```
✅ Video se reproduce sin problemas
✅ No hay pantallas negras
✅ No hay videos repetidos
✅ Transiciones suaves
✅ Gestos responden bien
✅ Likes y comentarios funcionan

CONCLUSIÓN: El problema eran los videos antiguos
ACCIÓN: Continuar usando la app normalmente
```

### Si algo NO funciona:
```
❌ Video no se reproduce
❌ Pantalla negra
❌ App se congela
❌ Errores en logcat

CONCLUSIÓN: Hay un bug en el código
ACCIÓN: Revisar logs y debuggear
```

---

## 🐛 SI ALGO SALE MAL

### Error: "serviceAccountKey.json not found"
```
CAUSA: El archivo no está en la ubicación correcta

SOLUCIÓN:
1. Verifica que el archivo esté en: functions/serviceAccountKey.json
2. Verifica que el nombre sea exacto (sin espacios, sin mayúsculas extra)
3. Verifica que sea un archivo .json válido
```

### Error: "Node.js not found"
```
CAUSA: Node.js no está instalado

SOLUCIÓN:
1. Ve a: https://nodejs.org
2. Descarga la versión LTS (recomendada)
3. Instala con las opciones por defecto
4. Reinicia CMD/PowerShell
5. Intenta de nuevo
```

### Error: "Permission denied"
```
CAUSA: La cuenta de servicio no tiene permisos

SOLUCIÓN:
1. Firebase Console → IAM y administración
2. Busca la cuenta de servicio
3. Verifica que tenga rol "Editor" o "Firebase Admin"
4. Si no, agrégalo
5. Intenta de nuevo
```

### Error: "Cannot connect to Firebase"
```
CAUSA: Problema de conexión o configuración

SOLUCIÓN:
1. Verifica tu conexión a internet
2. Verifica que el proyecto en serviceAccountKey.json sea el correcto
3. Verifica que el proyecto esté activo en Firebase Console
4. Intenta de nuevo
```

---

## 📊 LOGS PARA DEBUGGING

### Si necesitas ayuda, comparte estos logs:

#### 1. Salida del script:
```
Copia TODO el texto que aparece en la ventana de comandos
```

#### 2. Logs de la app (Android Studio):
```
Logcat → Filtro: "LiveScreen"
Copia los últimos 50 logs
```

#### 3. Estado de Firestore:
```
Firebase Console → Firestore → Screenshot de contest_entries
```

---

## 🎯 CHECKLIST COMPLETO

### Antes de empezar:
- [ ] Tengo acceso a Firebase Console
- [ ] Tengo Node.js instalado (o lo voy a instalar)
- [ ] Tengo 5 minutos disponibles
- [ ] Entiendo que se eliminarán todos los videos

### Durante la ejecución:
- [ ] Descargué serviceAccountKey.json
- [ ] Lo guardé en functions/
- [ ] Ejecuté el script
- [ ] Vi el mensaje de confirmación
- [ ] Esperé a que terminara
- [ ] Vi el mensaje "LIMPIEZA COMPLETADA"

### Después de la limpieza:
- [ ] Verifiqué Firestore (vacía)
- [ ] Verifiqué la app (mensaje "sin videos")
- [ ] Subí un video de prueba
- [ ] El video aparece en el carrusel
- [ ] El video se reproduce correctamente
- [ ] Los gestos funcionan
- [ ] No hay errores en logcat

---

## ⏱️ TIEMPO TOTAL ESTIMADO

```
Paso 1: Obtener Service Account Key    → 2 minutos
Paso 2: Ejecutar script                 → 1 minuto
Paso 3: Verificar resultado             → 30 segundos
Paso 4: Verificar Firebase Console      → 30 segundos
Paso 5: Verificar en la app             → 1 minuto
Paso 6: Subir video de prueba           → 2 minutos
Paso 7: Verificar funcionalidad         → 1 minuto
                                        ─────────────
TOTAL:                                    8 minutos
```

---

## 🎉 ¡LISTO!

Una vez completados todos los pasos, tendrás:
- ✅ Base de datos limpia
- ✅ Videos nuevos funcionando
- ✅ Confirmación de que el código funciona
- ✅ App lista para usar

---

**¿Listo para empezar?**

```bash
# Ejecuta este comando:
limpiar-videos-concursos.bat
```

**¡Buena suerte! 🚀**
