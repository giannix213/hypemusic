# 🧹 LIMPIEZA DE VIDEOS DE CONCURSOS

## 🎯 Objetivo
Eliminar todos los videos de la colección `contest_entries` en Firestore para empezar desde cero y verificar si el problema está en los videos o en el código.

---

## ⚠️ IMPORTANTE

### Lo que SE eliminará:
- ✅ Todos los documentos de la colección `contest_entries` en Firestore
- ✅ Registros de videos (metadata, likes, comentarios, etc.)

### Lo que NO se eliminará:
- ❌ Archivos de video en Firebase Storage (quedan ahí)
- ❌ Código de la aplicación
- ❌ Funcionalidad de la app
- ❌ Otros datos de Firestore (usuarios, perfiles, etc.)

---

## 📋 PREREQUISITOS

### 1. Node.js Instalado
Verifica que tienes Node.js:
```bash
node --version
```

Si no está instalado, descárgalo de: https://nodejs.org

### 2. Service Account Key
Necesitas el archivo `serviceAccountKey.json` en la carpeta `functions/`

**Cómo obtenerlo:**
1. Ve a Firebase Console: https://console.firebase.google.com
2. Selecciona tu proyecto
3. Ve a **Configuración del proyecto** (⚙️)
4. Pestaña **Cuentas de servicio**
5. Click en **Generar nueva clave privada**
6. Guarda el archivo como `functions/serviceAccountKey.json`

---

## 🚀 MÉTODO 1: Usar el Script Automático (Windows)

### Paso 1: Ejecutar el Batch
```bash
limpiar-videos-concursos.bat
```

### Paso 2: Confirmar
El script te mostrará:
- Cuántos videos se encontraron
- Información de cada video
- Advertencia de confirmación

### Paso 3: Esperar
El script eliminará todos los videos automáticamente.

---

## 🚀 MÉTODO 2: Ejecutar Manualmente (Todas las plataformas)

### Paso 1: Instalar dependencias
```bash
npm install firebase-admin
```

### Paso 2: Ejecutar script
```bash
node limpiar-videos-concursos.js
```

### Paso 3: Verificar resultado
El script mostrará:
```
✅ ===== LIMPIEZA COMPLETADA =====
✅ Videos eliminados: X
✅ La colección contest_entries está ahora vacía
```

---

## 🔍 QUÉ HACE EL SCRIPT

### 1. Conexión a Firebase
```javascript
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});
```

### 2. Obtener todos los videos
```javascript
const snapshot = await db.collection('contest_entries').get();
```

### 3. Mostrar información
```
📝 Videos que serán eliminados:
1. ID: abc123
   👤 Usuario: Juan (user001)
   📝 Título: Mi video
   🏆 Concurso: Mejor Cover
   ...
```

### 4. Eliminar en batch
```javascript
const batch = db.batch();
snapshot.forEach((doc) => {
  batch.delete(doc.ref);
});
await batch.commit();
```

---

## 📊 SALIDA ESPERADA

### Ejemplo de ejecución exitosa:
```
🧹 ===== INICIANDO LIMPIEZA DE VIDEOS DE CONCURSOS =====

📋 Paso 1: Obteniendo lista de videos...
📊 Total de videos encontrados: 5

📝 Videos que serán eliminados:
────────────────────────────────────────────────────────────────────────────────
1. ID: video001
   👤 Usuario: Juan (user123)
   📝 Título: Mi primer video
   🏆 Concurso: Mejor Cover de la Semana
   🎬 Video URL: https://firebasestorage.googleapis.com/...
   ❤️ Likes: 10 | 👁️ Views: 50
   📅 Fecha: 26/11/2025, 10:30:00

2. ID: video002
   👤 Usuario: María (user456)
   📝 Título: Cover de Shakira
   🏆 Concurso: Talento Emergente del Mes
   🎬 Video URL: https://firebasestorage.googleapis.com/...
   ❤️ Likes: 25 | 👁️ Views: 120
   📅 Fecha: 25/11/2025, 15:45:00

...

────────────────────────────────────────────────────────────────────────────────

⚠️  ADVERTENCIA: Esta acción eliminará TODOS los videos de concursos.
⚠️  Los archivos de video en Storage NO serán eliminados (solo los registros).

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

💡 Nota: Los archivos de video en Firebase Storage siguen ahí.
   Si quieres eliminarlos también, usa la consola de Firebase.
```

---

## 🔧 VERIFICACIÓN POST-LIMPIEZA

### 1. Verificar en Firebase Console
1. Ve a Firestore Database
2. Busca la colección `contest_entries`
3. Debe estar vacía o no existir

### 2. Verificar en la App
1. Abre la app
2. Ve a la pantalla de Live (carrusel)
3. Deberías ver el mensaje:
   ```
   🎬
   No hay videos de concursos aún
   Sé el primero en participar
   ```

### 3. Subir un nuevo video
1. Swipe izquierda → Catálogo
2. Selecciona un concurso
3. Graba o sube un video
4. Verifica que aparece en el carrusel
5. Verifica que se reproduce correctamente

---

## 🐛 SOLUCIÓN DE PROBLEMAS

### Error: "serviceAccountKey.json not found"
**Solución:**
1. Descarga la clave desde Firebase Console
2. Guárdala en `functions/serviceAccountKey.json`
3. Verifica que el nombre sea exacto

### Error: "firebase-admin not installed"
**Solución:**
```bash
npm install firebase-admin
```

### Error: "Permission denied"
**Solución:**
1. Verifica que la cuenta de servicio tenga permisos
2. En Firebase Console → IAM → Verifica roles
3. Debe tener rol "Firebase Admin" o "Editor"

### Error: "Cannot find module"
**Solución:**
```bash
cd functions
npm install
cd ..
node limpiar-videos-concursos.js
```

---

## 🔄 MÉTODO ALTERNATIVO: Firebase Console

Si el script no funciona, puedes eliminar manualmente:

### Opción 1: Eliminar colección completa
1. Ve a Firebase Console
2. Firestore Database
3. Selecciona `contest_entries`
4. Click en los 3 puntos (⋮)
5. "Delete collection"

### Opción 2: Eliminar documentos uno por uno
1. Ve a Firebase Console
2. Firestore Database
3. Abre `contest_entries`
4. Selecciona cada documento
5. Click en "Delete document"

---

## 📝 LOGS Y DEBUGGING

### El script genera logs detallados:
```javascript
console.log('📋 Paso 1: Obteniendo lista de videos...');
console.log(`📊 Total de videos encontrados: ${snapshot.size}`);
console.log('🗑️  Eliminando videos...');
console.log('✅ Videos eliminados: ${deleteCount}');
```

### Si algo falla:
```javascript
console.error('❌ ===== ERROR EN LA LIMPIEZA =====');
console.error('❌ Mensaje:', error.message);
console.error('❌ Detalles:', error);
```

---

## ✅ CHECKLIST POST-LIMPIEZA

- [ ] Script ejecutado sin errores
- [ ] Firestore `contest_entries` vacía
- [ ] App muestra "No hay videos"
- [ ] Puedo subir un nuevo video
- [ ] Nuevo video aparece en carrusel
- [ ] Nuevo video se reproduce correctamente
- [ ] Gestos funcionan (like, comentar, etc.)
- [ ] No hay errores en logcat

---

## 🎯 OBJETIVO DE LA LIMPIEZA

### Verificar si el problema es:

**Opción A: Videos corruptos/mal formateados**
- ✅ Después de limpiar, subir videos nuevos funciona
- ✅ Conclusión: Los videos antiguos tenían problemas

**Opción B: Problema en el código**
- ❌ Después de limpiar, videos nuevos tampoco funcionan
- ❌ Conclusión: Hay un bug en el código del carrusel

### Próximos pasos según resultado:

**Si funciona con videos nuevos:**
1. ✅ El código está bien
2. ✅ Problema era con videos antiguos
3. ✅ Continuar usando la app normalmente

**Si NO funciona con videos nuevos:**
1. ❌ Revisar logs de la app
2. ❌ Verificar estructura de datos en Firestore
3. ❌ Debuggear el código del carrusel
4. ❌ Verificar URLs de videos en Storage

---

## 📞 SOPORTE

Si tienes problemas:
1. Revisa los logs del script
2. Verifica los prerequisitos
3. Intenta el método alternativo (Firebase Console)
4. Revisa la sección de solución de problemas

---

**Fecha:** 26/11/2025
**Archivos creados:**
- `limpiar-videos-concursos.js` - Script de limpieza
- `limpiar-videos-concursos.bat` - Ejecutor automático (Windows)
- `INSTRUCCIONES_LIMPIEZA_VIDEOS.md` - Este documento

**Estado:** ✅ LISTO PARA USAR
