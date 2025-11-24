# 🔧 Migración de Videos en Firestore

## 🐛 Problema Actual

Los videos que ya están en Firestore fueron creados con datos incompletos:
- ❌ `username` vacío o "Usuario"
- ❌ `title` genérico
- ❌ `description` vacío
- ❌ `contestId` puede estar vacío

## 📊 Solución Temporal Implementada

### Fallbacks en la UI:
```kotlin
// Si username está vacío
username.isNotEmpty() ? "@${username}" : "@Usuario"

// Si title está vacío  
title.isNotEmpty() ? title : "Video de concurso"

// Si contestId está vacío
contestId.isNotEmpty() ? contestId : "Concurso"
```

Esto permite que los videos se muestren aunque tengan datos incompletos.

---

## 🔍 Cómo Identificar el Problema

### Revisar Logcat:
```
📋 Lista de videos:
  1. Video ID: abc123
     👤 Username: '' ⚠️ VACÍO
     📝 Título: 'Video de Mejor Cover...' ✅
     💬 Descripción: '' ⚠️ VACÍO
     🏆 Concurso: 'Mejor Cover...' ✅
```

### Revisar Firebase Console:
```
Firestore → contest_entries → [documento]

Campos que pueden estar vacíos:
- username: "" o "Usuario"
- description: ""
- title: "Video de [concurso]"
```

---

## 🛠️ Solución Permanente

### Opción 1: Actualizar Documentos Manualmente

**En Firebase Console:**
1. Ir a Firestore Database
2. Abrir colección `contest_entries`
3. Para cada documento:
   - Editar campo `username` → Agregar nombre real
   - Editar campo `description` → Agregar descripción
   - Guardar cambios

### Opción 2: Script de Migración (Recomendado)

**Crear función en FirebaseManager:**
```kotlin
suspend fun migrateOldContestEntries() {
    try {
        val snapshot = firestore.collection("contest_entries")
            .get()
            .await()
        
        var updated = 0
        snapshot.documents.forEach { doc ->
            val username = doc.getString("username") ?: ""
            val userId = doc.getString("userId") ?: ""
            
            // Si username está vacío, intentar obtenerlo del userId
            if (username.isEmpty() || username == "Usuario") {
                val userDoc = firestore.collection("users")
                    .document(userId)
                    .get()
                    .await()
                
                val realUsername = userDoc.getString("username") ?: "Usuario"
                
                doc.reference.update("username", realUsername).await()
                updated++
            }
        }
        
        Log.d("Migration", "✅ $updated documentos actualizados")
    } catch (e: Exception) {
        Log.e("Migration", "❌ Error: ${e.message}")
    }
}
```

### Opción 3: Eliminar y Recrear

**Si los videos viejos no son importantes:**
1. Eliminar todos los documentos de `contest_entries`
2. Subir nuevos videos con el código corregido
3. Los nuevos tendrán todos los datos correctos

---

## ✅ Verificación

### Después de la migración:

**1. Revisar Logcat:**
```
📋 Lista de videos:
  1. Video ID: abc123
     👤 Username: 'LunaBeats' ✅
     📝 Título: 'Mi cover de Bohemian...' ✅
     💬 Descripción: 'Mi versión del...' ✅
     🏆 Concurso: 'Mejor Cover...' ✅
```

**2. Revisar en la app:**
- Username visible: `@LunaBeats`
- Título visible: `Mi cover de Bohemian Rhapsody`
- Descripción visible: `Mi versión del clásico 🎸`
- Badge visible: `Mejor Cover de la Semana`

---

## 🎯 Recomendación

**Para videos existentes:**
- Usar los fallbacks implementados (ya funcionan)
- Opcionalmente, migrar datos manualmente en Firebase Console

**Para videos nuevos:**
- El código corregido ya guarda todos los datos correctamente
- Cada nuevo video tendrá username, title, description, contestId

---

## 📝 Notas

1. **Los fallbacks ya están implementados** - Los videos se muestran aunque falten datos
2. **Nuevos videos funcionarán correctamente** - El código de subida está corregido
3. **Videos viejos pueden actualizarse** - Pero no es crítico, los fallbacks funcionan

---

**Estado:** ✅ Fallbacks implementados
**Próximo paso:** Subir un nuevo video para verificar que tenga todos los datos
