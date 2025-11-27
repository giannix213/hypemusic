# 🧹 LIMPIEZA MANUAL DE VIDEOS - SIN SCRIPTS

## 🎯 Método más simple: Firebase Console

Ya que no tienes el `serviceAccountKey.json`, usa este método manual que es más rápido:

---

## ✅ PASOS (2 minutos)

### 1. Abrir Firebase Console
```
1. Ve a: https://console.firebase.google.com
2. Selecciona tu proyecto
3. Click en "Firestore Database" en el menú lateral
```

### 2. Encontrar la colección
```
4. En la lista de colecciones, busca: contest_entries
5. Click en ella para abrirla
```

### 3. Eliminar la colección
```
6. Click en los 3 puntos verticales (⋮) al lado de "contest_entries"
7. Selecciona "Delete collection"
8. Confirma escribiendo el nombre de la colección
9. Click en "Delete"
```

**O eliminar documentos uno por uno:**
```
6. Abre la colección contest_entries
7. Para cada documento:
   - Click en el documento
   - Click en los 3 puntos (⋮)
   - "Delete document"
   - Confirmar
```

---

## 🔍 VERIFICAR

### En Firebase Console:
```
✅ La colección contest_entries no aparece
   O
✅ Aparece pero dice "(0 documentos)"
```

### En la App:
```
1. Abre la app
2. Ve a la pantalla Live
3. Debe mostrar: "No hay videos de concursos aún"
```

---

## 🧪 PROBAR CON VIDEO NUEVO

### 1. Subir video:
```
1. Swipe izquierda → Catálogo
2. Selecciona un concurso
3. Graba o sube un video
4. Confirma y sube
```

### 2. Verificar:
```
1. Vuelve a la pantalla Live
2. El video debe aparecer
3. Debe reproducirse correctamente
4. Gestos deben funcionar
```

---

## ✅ RESULTADO ESPERADO

### Si funciona:
```
✅ Video se reproduce sin problemas
✅ No hay pantallas negras
✅ Transiciones suaves
✅ Gestos funcionan

CONCLUSIÓN: El problema eran los videos antiguos
```

### Si NO funciona:
```
❌ Video no se reproduce
❌ Pantalla negra
❌ Errores

CONCLUSIÓN: Hay un bug en el código
ACCIÓN: Revisar logs de la app
```

---

## 📊 CAPTURAS DE PANTALLA

### Antes de eliminar:
```
Firestore Database
  └── contest_entries (5 documentos)
      ├── abc123
      ├── def456
      ├── ghi789
      ├── jkl012
      └── mno345
```

### Después de eliminar:
```
Firestore Database
  └── contest_entries (0 documentos)
```

---

## ⏱️ TIEMPO TOTAL

```
Abrir Firebase Console     → 30 segundos
Encontrar colección        → 10 segundos
Eliminar colección         → 20 segundos
Verificar en app           → 1 minuto
Subir video de prueba      → 2 minutos
                           ─────────────
TOTAL:                       4 minutos
```

---

## 🎉 ¡LISTO!

Este método es más simple y no requiere:
- ❌ serviceAccountKey.json
- ❌ Node.js scripts
- ❌ Firebase CLI
- ❌ Comandos de terminal

Solo necesitas:
- ✅ Acceso a Firebase Console
- ✅ 4 minutos de tu tiempo

---

**¿Listo?**

👉 Ve a: https://console.firebase.google.com

**¡Buena suerte! 🚀**
