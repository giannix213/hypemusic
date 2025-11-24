# 🔧 Solución Completa - Historias No Se Muestran

## 🚨 Problema Principal

**Firestore necesita un índice compuesto** para la query de `getUserStories()`.

La query usa:
```kotlin
.whereEqualTo("artistId", userId)
.whereGreaterThan("expiresAt", now)
```

Esto requiere un índice compuesto en Firestore.

## ✅ Solución Inmediata

### Opción 1: Crear el Índice (Recomendado)

1. **Ejecuta la app** e intenta cargar historias
2. **Verifica Logcat** - Deberías ver un error como:
   ```
   FAILED_PRECONDITION: The query requires an index
   ```
3. **Copia el link** que aparece en el error
4. **Ábrelo en el navegador** - Te llevará a Firebase Console
5. **Click en "Crear índice"**
6. **Espera 2-5 minutos** a que se cree
7. **Reinicia la app**

### Opción 2: Simplificar la Query (Temporal)

Cambia `getUserStories()` en FirebaseManager.kt:

```kotlin
suspend fun getUserStories(userId: String): List<ArtistStory> {
    return try {
        val now = System.currentTimeMillis()
        android.util.Log.d("HISTORIAS_FIREBASE", "🔍 Buscando historias para: $userId")
        
        // Query simplificado - solo filtra por userId
        val snapshot = firestore.collection("stories")
            .whereEqualTo("artistId", userId)
            .get()
            .await()
        
        android.util.Log.d("HISTORIAS_FIREBASE", "📦 Documentos: ${snapshot.documents.size}")
        
        // Filtrar manualmente las expiradas
        val stories = snapshot.documents.mapNotNull { doc ->
            try {
                val expiresAt = doc.getLong("expiresAt") ?: 0L
                
                // Saltar si expiró
                if (expiresAt <= now) {
                    android.util.Log.d("HISTORIAS_FIREBASE", "⏭️ Historia expirada: ${doc.id}")
                    return@mapNotNull null
                }
                
                ArtistStory(
                    id = doc.id,
                    artistId = doc.getString("artistId") ?: "",
                    artistName = doc.getString("artistName") ?: "",
                    artistImageUrl = doc.getString("artistImageUrl") ?: "",
                    mediaUrl = doc.getString("mediaUrl") ?: "",
                    mediaType = doc.getString("mediaType") ?: "image",
                    caption = doc.getString("caption") ?: "",
                    timestamp = doc.getLong("timestamp") ?: 0L,
                    expiresAt = expiresAt,
                    views = (doc.getLong("views") ?: 0).toInt(),
                    isViewed = false,
                    isHighlighted = doc.getBoolean("isHighlighted") ?: false
                )
            } catch (e: Exception) {
                android.util.Log.e("HISTORIAS_FIREBASE", "Error: ${e.message}")
                null
            }
        }.sortedByDescending { it.timestamp }
        
        android.util.Log.d("HISTORIAS_FIREBASE", "✅ ${stories.size} historias válidas")
        stories
    } catch (e: Exception) {
        android.util.Log.e("HISTORIAS_FIREBASE", "❌ Error: ${e.message}", e)
        emptyList()
    }
}
```

## 🔍 Verificar en Firebase Console

### 1. Firestore Database
```
stories/
  └── {docId}/
      ├── artistId: "tu_user_id"
      ├── artistName: "Tu Nombre"
      ├── mediaUrl: "https://..."
      ├── mediaType: "image"
      ├── timestamp: 1700000000000
      ├── expiresAt: 1700086400000  ← Debe ser > ahora
      └── views: 0
```

### 2. Storage
```
stories/
  └── {userId}/
      └── {uuid}.jpg  ← Tu imagen
```

### 3. Índices
Ve a: **Firestore Database > Índices**

Debe existir:
- Colección: `stories`
- Campos: `artistId` (Ascending), `expiresAt` (Ascending)

## 🐛 Debug Paso a Paso

### 1. Verifica que se suba el archivo
```
Logcat: "ProfileScreen"
Busca: "📤 Paso 1: Subiendo archivo"
Debe mostrar: "✅ Archivo subido exitosamente"
```

### 2. Verifica que se guarde la metadata
```
Logcat: "UPLOAD_STORY_METADATA"
Busca: "💾 Guardando en Firestore"
Debe mostrar: "✅ METADATA GUARDADA EXITOSAMENTE"
```

### 3. Verifica que se obtengan las historias
```
Logcat: "HISTORIAS_FIREBASE"
Busca: "🔍 Buscando historias"
Debe mostrar: "📦 Documentos encontrados: X"
```

### 4. Si no encuentra documentos
- Ve a Firebase Console > Firestore
- Verifica que exista la colección `stories`
- Verifica que el `artistId` coincida con tu userId
- Verifica que `expiresAt` sea mayor que el timestamp actual

## 🎯 Checklist Completo

- [ ] Archivo se sube a Storage en `stories/{userId}/`
- [ ] Documento se crea en Firestore colección `stories`
- [ ] Documento tiene todos los campos requeridos
- [ ] `expiresAt` es mayor que timestamp actual
- [ ] `artistId` coincide con tu userId
- [ ] Índice compuesto está creado (o query simplificado)
- [ ] Logs muestran "Documentos encontrados: X" con X > 0
- [ ] App no se queda cargando indefinidamente

## 🚀 Solución Rápida

Si quieres que funcione YA:

1. **Usa la Opción 2** (query simplificado) arriba
2. **Reinicia la app**
3. **Sube una nueva historia**
4. **Espera 2 segundos**
5. **Toca el badge** para recargar

Esto debería funcionar inmediatamente sin esperar a que se cree el índice.

## 📝 Notas

- Las historias expiran en 24 horas automáticamente
- El query simplificado es menos eficiente pero funciona sin índice
- Una vez creado el índice, puedes volver al query original
- El índice tarda 2-5 minutos en crearse

---

**Próximo paso**: Implementa la Opción 2 (query simplificado) para que funcione inmediatamente.
