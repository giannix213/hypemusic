# 📋 RESUMEN: LIMPIEZA DE VIDEOS DE CONCURSOS

## ✅ ARCHIVOS CREADOS

### 1. `limpiar-videos-concursos.js`
**Tipo:** Script Node.js
**Función:** Elimina todos los documentos de `contest_entries` en Firestore
**Uso:** `node limpiar-videos-concursos.js`

### 2. `limpiar-videos-concursos.bat`
**Tipo:** Batch script (Windows)
**Función:** Ejecutor automático con verificaciones
**Uso:** Doble click o `limpiar-videos-concursos.bat`

### 3. `INSTRUCCIONES_LIMPIEZA_VIDEOS.md`
**Tipo:** Documentación completa
**Contenido:**
- Prerequisitos detallados
- Métodos de ejecución
- Solución de problemas
- Verificación post-limpieza

### 4. `LIMPIAR_VIDEOS_RAPIDO.md`
**Tipo:** Guía express
**Contenido:**
- Pasos rápidos
- Comandos esenciales
- Troubleshooting básico

---

## 🎯 PROPÓSITO

### Problema a resolver:
```
¿El carrusel falla por videos corruptos o por un bug en el código?
```

### Solución:
```
1. Eliminar TODOS los videos existentes
2. Subir videos nuevos desde cero
3. Verificar si funcionan correctamente
```

### Resultado esperado:
```
✅ Videos nuevos funcionan → Problema eran los videos antiguos
❌ Videos nuevos fallan → Problema está en el código
```

---

## 🚀 CÓMO USAR

### Opción 1: Automático (Windows)
```bash
# Paso 1: Obtener serviceAccountKey.json de Firebase Console
# Paso 2: Guardar en functions/serviceAccountKey.json
# Paso 3: Ejecutar
limpiar-videos-concursos.bat
```

### Opción 2: Manual (Todas las plataformas)
```bash
# Instalar dependencias
npm install firebase-admin

# Ejecutar script
node limpiar-videos-concursos.js
```

---

## 📊 QUÉ HACE EL SCRIPT

### 1. Conexión
```javascript
// Conecta a Firebase usando Service Account
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});
```

### 2. Lectura
```javascript
// Lee todos los videos de contest_entries
const snapshot = await db.collection('contest_entries').get();
console.log(`Total: ${snapshot.size} videos`);
```

### 3. Información
```javascript
// Muestra detalles de cada video
snapshot.forEach((doc) => {
  console.log(`ID: ${doc.id}`);
  console.log(`Usuario: ${data.username}`);
  console.log(`Título: ${data.title}`);
  // ...
});
```

### 4. Eliminación
```javascript
// Elimina todos en batch
const batch = db.batch();
snapshot.forEach((doc) => batch.delete(doc.ref));
await batch.commit();
```

---

## ⚠️ IMPORTANTE

### Lo que SE elimina:
- ✅ Documentos de `contest_entries`
- ✅ Metadata de videos (título, descripción, etc.)
- ✅ Likes y comentarios asociados
- ✅ Contadores de vistas

### Lo que NO se elimina:
- ❌ Archivos de video en Storage
- ❌ Código de la aplicación
- ❌ Funcionalidad del carrusel
- ❌ Usuarios y perfiles
- ❌ Otros datos de Firestore

---

## 🔍 VERIFICACIÓN

### 1. En Firebase Console
```
Firestore Database → contest_entries → (vacía o no existe)
```

### 2. En la App
```
Pantalla Live → Mensaje: "No hay videos de concursos aún"
```

### 3. Subir video nuevo
```
Catálogo → Concurso → Grabar/Subir → Verificar en carrusel
```

---

## 🧪 PRUEBA POST-LIMPIEZA

### Checklist:
- [ ] Firestore vacía
- [ ] App muestra mensaje de "sin videos"
- [ ] Puedo subir un video nuevo
- [ ] Video aparece en carrusel
- [ ] Video se reproduce
- [ ] Puedo dar like
- [ ] Puedo comentar
- [ ] Puedo compartir
- [ ] Gestos funcionan (tap, doble tap, swipe)
- [ ] No hay errores en logcat

---

## 🐛 TROUBLESHOOTING

### Error común 1: Service Account Key
```
Error: serviceAccountKey.json not found

Solución:
1. Firebase Console → Configuración → Cuentas de servicio
2. Generar nueva clave privada
3. Guardar en functions/serviceAccountKey.json
```

### Error común 2: Node.js
```
Error: 'node' is not recognized

Solución:
Instalar Node.js desde https://nodejs.org
```

### Error común 3: Dependencias
```
Error: Cannot find module 'firebase-admin'

Solución:
npm install firebase-admin
```

---

## 📈 ANÁLISIS DE RESULTADOS

### Escenario A: Videos nuevos funcionan ✅
```
Conclusión: Los videos antiguos tenían problemas
Causa probable:
  - Formato de video incompatible
  - URLs rotas o expiradas
  - Metadata corrupta
  - Estructura de datos antigua

Acción: Continuar usando la app normalmente
```

### Escenario B: Videos nuevos NO funcionan ❌
```
Conclusión: Hay un bug en el código
Áreas a revisar:
  - SlotPlayerPool (gestión de players)
  - VideoPlayerComp (reproducción)
  - ContestVideoCarouselScreen (UI)
  - FirebaseManager.getAllContestEntries()

Acción: Debuggear el código con logs
```

---

## 📝 LOGS GENERADOS

### Logs del script:
```
🧹 ===== INICIANDO LIMPIEZA =====
📋 Obteniendo lista de videos...
📊 Total: 5 videos
📝 Videos que serán eliminados:
   1. ID: abc123
      Usuario: Juan
      ...
🗑️  Eliminando videos...
✅ Videos eliminados: 5
✅ Colección vacía
```

### Logs de la app (después):
```
D/LiveScreen: 🎬 ===== CARGANDO VIDEOS =====
D/LiveScreen: ✅ Videos cargados: 0
D/LiveScreen: ⚠️ No se encontraron videos
```

---

## 🎯 OBJETIVO FINAL

### Determinar la causa del problema:
```
¿Videos corruptos? → Limpiar y subir nuevos
¿Bug en el código? → Debuggear y corregir
```

### Resultado esperado:
```
✅ Carrusel funcionando perfectamente
✅ Videos reproduciéndose sin problemas
✅ Todas las interacciones funcionando
✅ Sin errores en logs
```

---

## 📞 SIGUIENTE PASO

### Después de ejecutar la limpieza:

1. **Verificar que Firestore está vacía**
2. **Abrir la app y confirmar mensaje "sin videos"**
3. **Subir 2-3 videos nuevos de prueba**
4. **Verificar que funcionan correctamente**
5. **Reportar resultados:**
   - ✅ Si funciona: Problema resuelto
   - ❌ Si no funciona: Compartir logs para debugging

---

## 📚 DOCUMENTACIÓN RELACIONADA

- `IMPLEMENTACION_COMPLETA_FINAL.md` - Estado general del proyecto
- `SOLUCION_CHATGPT_IMPLEMENTADA.md` - Implementación del carrusel
- `CARRUSEL_POOL_IMPLEMENTADO.md` - Detalles técnicos del pool
- `OPTIMIZACION_CARRUSEL_VIDEOS.md` - Optimizaciones aplicadas

---

**Creado:** 26/11/2025
**Propósito:** Diagnóstico y limpieza de datos
**Impacto:** Solo datos, no afecta código
**Reversible:** No (pero se pueden subir videos nuevos)
**Tiempo:** 2-3 minutos
**Dificultad:** Fácil

---

## ✅ CHECKLIST FINAL

Antes de ejecutar:
- [ ] Tengo `serviceAccountKey.json` en `functions/`
- [ ] Node.js está instalado
- [ ] Entiendo que se eliminarán TODOS los videos
- [ ] Tengo backup si es necesario (opcional)

Después de ejecutar:
- [ ] Script completado sin errores
- [ ] Firestore vacía verificada
- [ ] App muestra "sin videos"
- [ ] Listo para subir videos nuevos

---

**Estado:** ✅ LISTO PARA USAR
**Próximo paso:** Ejecutar `limpiar-videos-concursos.bat`
