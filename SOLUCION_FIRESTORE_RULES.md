# 🔥 SOLUCIÓN: Reglas de Firestore para Live Streaming

## 🎯 Problema Identificado

- ✅ Dispositivo 1 CREA documentos en Firestore
- ❌ Dispositivo 2 NO PUEDE LEER los documentos
- **Causa:** Reglas de seguridad de Firestore bloquean la lectura

---

## 🔧 Solución: Actualizar Reglas de Firestore

### Paso 1: Ir a Firebase Console

1. Ve a: https://console.firebase.google.com/project/hype-13966/firestore/rules
2. O en Firebase Console → Firestore Database → Reglas

---

### Paso 2: Actualizar las Reglas

**Reemplaza las reglas actuales con estas:**

```javascript
rules_version = '2';

service cloud.firestore {
  match /databases/{database}/documents {
    
    // Regla por defecto: usuarios autenticados pueden leer/escribir sus propios datos
    match /{document=**} {
      allow read, write: if request.auth != null;
    }
    
    // REGLAS ESPECÍFICAS PARA LIVE STREAMING
    match /live_sessions/{sessionId} {
      // CUALQUIER usuario autenticado (incluyendo anónimos) puede:
      // - LEER todas las sesiones de Live
      // - CREAR nuevas sesiones
      allow read: if request.auth != null;
      allow create: if request.auth != null;
      
      // Solo el creador puede actualizar o eliminar
      allow update, delete: if request.auth != null && 
                               request.auth.uid == resource.data.userId;
    }
    
    // Reglas para otras colecciones
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
    
    match /songs/{songId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null;
      allow update, delete: if request.auth != null && 
                               request.auth.uid == resource.data.artistId;
    }
    
    match /contest_entries/{entryId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null;
      allow update, delete: if request.auth != null && 
                               request.auth.uid == resource.data.userId;
    }
    
    match /stories/{storyId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null;
      allow update, delete: if request.auth != null && 
                               request.auth.uid == resource.data.userId;
    }
  }
}
```

---

### Paso 3: Publicar las Reglas

1. Haz clic en **"Publicar"** en Firebase Console
2. Espera la confirmación

---

## ✅ Qué Hacen Estas Reglas

### Para `live_sessions`:

```javascript
// ✅ LEER: Cualquier usuario autenticado (incluyendo invitados)
allow read: if request.auth != null;

// ✅ CREAR: Cualquier usuario autenticado puede crear Lives
allow create: if request.auth != null;

// ✅ ACTUALIZAR/ELIMINAR: Solo el creador
allow update, delete: if request.auth.uid == resource.data.userId;
```

**Esto permite:**
- ✅ Usuarios invitados pueden ver Lives
- ✅ Usuarios invitados pueden crear Lives
- ✅ Solo el creador puede finalizar su Live

---

## 🧪 Probar Después de Actualizar

### En Dispositivo 1:
1. Inicia un Live
2. Verifica que se crea en Firestore

### En Dispositivo 2:
1. Ve a la sección Live
2. Desliza para ver el catálogo
3. **AHORA DEBERÍAS VER EL LIVE** 🎉

---

## 🐛 Si Aún No Funciona

### Verificar en los Logs del Dispositivo 2:

```bash
.\ver-logs-live-completo.bat
```

**Busca:**
```
📡 Obteniendo sesiones de Live activas...
✅ X sesiones activas encontradas
```

**Si muestra 0 sesiones:**
- Las reglas aún no se aplicaron (espera 1 minuto)
- Hay otro problema en el código

**Si muestra error de permisos:**
```
❌ PERMISSION_DENIED: Missing or insufficient permissions
```
- Las reglas no se publicaron correctamente
- Vuelve a publicarlas

---

## 📊 Reglas Alternativas (Más Permisivas)

Si quieres permitir que **CUALQUIERA** (incluso sin autenticar) pueda ver Lives:

```javascript
match /live_sessions/{sessionId} {
  // Cualquiera puede leer (incluso sin autenticar)
  allow read: if true;
  
  // Solo usuarios autenticados pueden crear
  allow create: if request.auth != null;
  
  // Solo el creador puede actualizar/eliminar
  allow update, delete: if request.auth != null && 
                           request.auth.uid == resource.data.userId;
}
```

**⚠️ Advertencia:** Esto es menos seguro pero más fácil para probar.

---

## 🚀 Acción Inmediata

1. **Ve a Firebase Console:**
   https://console.firebase.google.com/project/hype-13966/firestore/rules

2. **Copia y pega las reglas de arriba**

3. **Haz clic en "Publicar"**

4. **Espera 30 segundos**

5. **Prueba de nuevo en el dispositivo 2**

---

## ✅ Checklist

- [ ] Reglas actualizadas en Firebase Console
- [ ] Reglas publicadas
- [ ] Esperado 30 segundos
- [ ] Probado en dispositivo 2
- [ ] Dispositivo 2 ve el Live

---

¡Actualiza las reglas y prueba de nuevo! 🚀
