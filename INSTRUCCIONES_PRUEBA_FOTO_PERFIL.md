# 🧪 Instrucciones de Prueba: Fotos de Perfil en Videos

## 📋 Objetivo
Verificar que las fotos de perfil y nombres de usuario se muestren correctamente en los videos de concursos.

## 🔧 Preparación

### 1. Compilar la App
```bash
# En Android Studio o desde terminal
./gradlew clean
./gradlew assembleDebug
```

### 2. Instalar en Dispositivo/Emulador
```bash
./gradlew installDebug
```

## ✅ Casos de Prueba

### Caso 1: Usuario CON Foto de Perfil

**Pasos:**
1. Asegúrate de tener una foto de perfil configurada en tu cuenta
2. Ve a la pantalla de Live (icono 🔴)
3. Swipe hacia la izquierda para abrir el catálogo
4. Selecciona un concurso
5. Graba un video
6. Sube el video

**Resultado Esperado:**
- ✅ El video aparece en el carrusel de Live
- ✅ Se muestra tu foto de perfil circular (32dp)
- ✅ Se muestra tu nombre de usuario
- ✅ La foto se carga correctamente desde Firebase
- ✅ Al hacer clic en tu foto/nombre, navega a tu perfil

**Logs a Verificar:**
```
📸 Paso 2: Obteniendo foto de perfil...
👤 Foto de perfil: ✅ Encontrada
📝 Paso 3: Creando entrada en Firestore...
✅ ===== VIDEO PUBLICADO EXITOSAMENTE =====
```

---

### Caso 2: Usuario SIN Foto de Perfil

**Pasos:**
1. Crea una cuenta nueva o elimina tu foto de perfil
2. Ve a la pantalla de Live
3. Swipe hacia la izquierda para abrir el catálogo
4. Selecciona un concurso
5. Graba un video
6. Sube el video

**Resultado Esperado:**
- ✅ El video aparece en el carrusel de Live
- ✅ Se muestra un avatar circular rosa con tu inicial
- ✅ Se muestra tu nombre de usuario
- ✅ Al hacer clic en el avatar/nombre, navega a tu perfil

**Logs a Verificar:**
```
📸 Paso 2: Obteniendo foto de perfil...
👤 Foto de perfil: ⚠️ No disponible
📝 Paso 3: Creando entrada en Firestore...
✅ ===== VIDEO PUBLICADO EXITOSAMENTE =====
```

---

### Caso 3: Ver Videos de Otros Usuarios

**Pasos:**
1. Ve a la pantalla de Live
2. Swipe verticalmente para ver diferentes videos
3. Observa la información de cada usuario

**Resultado Esperado:**
- ✅ Cada video muestra la foto de perfil correcta del autor
- ✅ Cada video muestra el nombre de usuario correcto
- ✅ Los avatars fallback se muestran para usuarios sin foto
- ✅ Las fotos se cargan suavemente sin bloquear la UI
- ✅ Al hacer clic en cualquier foto/nombre, navega al perfil correcto

---

### Caso 4: Navegación al Perfil

**Pasos:**
1. Ve a la pantalla de Live
2. Mira un video de cualquier usuario
3. Haz clic en la foto de perfil o nombre del usuario

**Resultado Esperado:**
- ✅ Navega al perfil del usuario correcto
- ✅ Se muestra la información completa del usuario
- ✅ Puedes regresar al carrusel de videos

**Logs a Verificar:**
```
👤 Navegando al perfil de: Luna Beats (abc123)
```

---

### Caso 5: Rendimiento y Caché

**Pasos:**
1. Ve a la pantalla de Live
2. Swipe entre varios videos
3. Regresa a videos que ya viste
4. Observa la velocidad de carga de las fotos

**Resultado Esperado:**
- ✅ Las fotos se cargan rápidamente la primera vez
- ✅ Las fotos se cargan instantáneamente al regresar (caché)
- ✅ No hay lag o stuttering al cambiar de video
- ✅ La UI permanece fluida

---

### Caso 6: Conexión Lenta/Sin Internet

**Pasos:**
1. Activa el modo avión o limita la velocidad de red
2. Ve a la pantalla de Live
3. Intenta ver videos

**Resultado Esperado:**
- ✅ Los videos se cargan (si están en caché)
- ✅ Las fotos de perfil muestran el avatar fallback si no cargan
- ✅ No hay crashes ni errores
- ✅ La app permanece usable

---

## 🔍 Verificación en Firebase Console

### 1. Verificar Estructura de Datos

**Ir a:** Firebase Console → Firestore Database → contest_entries

**Verificar que cada documento tenga:**
```json
{
  "userId": "string",
  "username": "string",
  "profilePictureUrl": "string",  ← ✅ DEBE EXISTIR
  "videoUrl": "string",
  "title": "string",
  "description": "string",
  "contestId": "string",
  "likes": 0,
  "views": 0,
  "timestamp": 1700000000000
}
```

### 2. Verificar URLs de Fotos

**Verificar que:**
- ✅ `profilePictureUrl` no esté vacío (si el usuario tiene foto)
- ✅ La URL apunte a Firebase Storage
- ✅ La URL sea accesible (copiar y pegar en navegador)

---

## 🐛 Problemas Comunes y Soluciones

### Problema 1: No se muestra la foto de perfil

**Posibles causas:**
- El usuario no tiene foto de perfil configurada
- La URL de la foto es incorrecta
- Problemas de permisos en Firebase Storage

**Solución:**
1. Verificar en Firebase Console que el usuario tenga `profileImageUrl`
2. Verificar que la URL sea accesible
3. Verificar logs: `👤 Foto de perfil: ✅ Encontrada` o `⚠️ No disponible`

---

### Problema 2: Se muestra "Usuario" en lugar del nombre

**Posibles causas:**
- El campo `username` está vacío en Firestore
- Error al obtener el nombre del usuario

**Solución:**
1. Verificar en Firebase Console que el documento tenga `username`
2. Verificar logs: `👤 Usuario: [nombre] ([userId])`

---

### Problema 3: La foto no se carga (queda en blanco)

**Posibles causas:**
- Dependencia de Coil no está instalada
- URL de la foto es inválida
- Problemas de red

**Solución:**
1. Verificar que `io.coil-kt:coil-compose:2.5.0` esté en build.gradle
2. Verificar que la URL comience con `https://`
3. Probar la URL en un navegador

---

### Problema 4: Crash al hacer clic en la foto

**Posibles causas:**
- `onNavigateToProfile` no está implementado
- `userId` está vacío

**Solución:**
1. Verificar que `onNavigateToProfile` esté definido en MainActivity
2. Verificar logs: `👤 Navegando al perfil de: [username] ([userId])`

---

## 📊 Métricas de Éxito

### Funcionalidad
- [ ] 100% de videos muestran información del usuario
- [ ] 100% de fotos de perfil cargan correctamente (o muestran fallback)
- [ ] 100% de clics en perfil navegan correctamente

### Rendimiento
- [ ] Fotos cargan en < 2 segundos (primera vez)
- [ ] Fotos cargan instantáneamente (desde caché)
- [ ] No hay lag al cambiar de video

### Experiencia de Usuario
- [ ] La UI se ve profesional y pulida
- [ ] Los avatars fallback son atractivos
- [ ] La navegación es intuitiva

---

## 🎯 Checklist Final

Antes de considerar la feature completa, verificar:

- [ ] Todos los casos de prueba pasan
- [ ] No hay errores en los logs
- [ ] No hay crashes
- [ ] La UI se ve bien en diferentes tamaños de pantalla
- [ ] Las fotos se cargan correctamente
- [ ] Los avatars fallback se ven bien
- [ ] La navegación al perfil funciona
- [ ] El rendimiento es bueno
- [ ] Los datos se guardan correctamente en Firestore

---

## 📝 Reporte de Pruebas

### Plantilla de Reporte

```
Fecha: [fecha]
Dispositivo: [modelo]
Android: [versión]
App Version: [versión]

CASO 1: Usuario con foto de perfil
- Estado: ✅ / ❌
- Notas: [observaciones]

CASO 2: Usuario sin foto de perfil
- Estado: ✅ / ❌
- Notas: [observaciones]

CASO 3: Ver videos de otros usuarios
- Estado: ✅ / ❌
- Notas: [observaciones]

CASO 4: Navegación al perfil
- Estado: ✅ / ❌
- Notas: [observaciones]

CASO 5: Rendimiento y caché
- Estado: ✅ / ❌
- Notas: [observaciones]

CASO 6: Conexión lenta/sin internet
- Estado: ✅ / ❌
- Notas: [observaciones]

PROBLEMAS ENCONTRADOS:
[lista de problemas]

CONCLUSIÓN:
[resumen general]
```

---

## 🚀 Siguiente Paso

Una vez que todas las pruebas pasen:

1. ✅ Marcar la feature como completa
2. 📝 Documentar cualquier problema encontrado
3. 🎉 Celebrar la mejora de la experiencia de usuario

¡Buena suerte con las pruebas! 🎊
