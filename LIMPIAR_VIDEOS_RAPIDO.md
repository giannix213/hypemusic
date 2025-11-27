# 🧹 LIMPIEZA RÁPIDA DE VIDEOS - GUÍA EXPRESS

## 🎯 Objetivo
Eliminar todos los videos de concursos para empezar desde cero y verificar si el problema está en los videos o en el código.

---

## ⚡ PASOS RÁPIDOS (Windows)

### 1. Obtener Service Account Key
```
1. Ve a: https://console.firebase.google.com
2. Selecciona tu proyecto
3. ⚙️ Configuración → Cuentas de servicio
4. "Generar nueva clave privada"
5. Guarda como: functions/serviceAccountKey.json
```

### 2. Ejecutar Script
```bash
limpiar-videos-concursos.bat
```

### 3. Confirmar
- El script mostrará todos los videos
- Presiona Enter para confirmar
- Espera 3 segundos
- ✅ Videos eliminados

---

## 🔍 VERIFICAR RESULTADO

### En Firebase Console:
```
Firestore → contest_entries → (vacía)
```

### En la App:
```
Pantalla Live → "No hay videos de concursos aún"
```

---

## 🧪 PROBAR CON VIDEOS NUEVOS

### 1. Subir un video nuevo:
```
1. Abre la app
2. Swipe izquierda → Catálogo
3. Selecciona un concurso
4. Graba o sube un video
5. Confirma y sube
```

### 2. Verificar en el carrusel:
```
1. Vuelve a la pantalla Live
2. El video debe aparecer
3. Debe reproducirse correctamente
4. Gestos deben funcionar (like, comentar, etc.)
```

---

## ✅ RESULTADO ESPERADO

### Si funciona con videos nuevos:
```
✅ El código está bien
✅ El problema eran los videos antiguos
✅ Continuar usando normalmente
```

### Si NO funciona con videos nuevos:
```
❌ Hay un problema en el código
❌ Revisar logs de la app
❌ Debuggear el carrusel
```

---

## 🐛 SI HAY PROBLEMAS

### Error: "serviceAccountKey.json not found"
```bash
# Verifica que el archivo esté en:
functions/serviceAccountKey.json
```

### Error: "Node.js not found"
```bash
# Instala Node.js desde:
https://nodejs.org
```

### Error: "firebase-admin not installed"
```bash
npm install firebase-admin
```

---

## 📋 ARCHIVOS CREADOS

1. **limpiar-videos-concursos.js** - Script principal
2. **limpiar-videos-concursos.bat** - Ejecutor automático (Windows)
3. **INSTRUCCIONES_LIMPIEZA_VIDEOS.md** - Guía completa
4. **LIMPIAR_VIDEOS_RAPIDO.md** - Esta guía express

---

## 🚀 COMANDO ÚNICO

Si tienes todo configurado:
```bash
node limpiar-videos-concursos.js
```

---

## ⚠️ IMPORTANTE

- ✅ Elimina registros de Firestore
- ❌ NO elimina archivos de Storage
- ❌ NO afecta el código
- ❌ NO afecta otros datos

---

**Tiempo estimado:** 2-3 minutos
**Dificultad:** Fácil
**Reversible:** No (pero puedes subir videos nuevos)
