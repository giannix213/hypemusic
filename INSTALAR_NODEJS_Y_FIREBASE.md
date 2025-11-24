# 📦 Instalar Node.js y Firebase CLI - Guía Paso a Paso

## Paso 1: Instalar Node.js

### Opción A: Descarga directa (Recomendado)

1. Ve a: https://nodejs.org/
2. Descarga la versión **LTS** (Long Term Support)
3. Ejecuta el instalador
4. Sigue el asistente (deja todas las opciones por defecto)
5. Reinicia tu terminal/PowerShell

### Opción B: Usando Chocolatey (si lo tienes instalado)

```powershell
choco install nodejs-lts
```

### Verificar instalación

Abre una **nueva** terminal PowerShell y ejecuta:

```powershell
node --version
npm --version
```

Deberías ver algo como:
```
v20.11.0
10.2.4
```

## Paso 2: Instalar Firebase CLI

Una vez que Node.js esté instalado:

```powershell
npm install -g firebase-tools
```

Esto puede tomar 1-2 minutos.

### Verificar instalación

```powershell
firebase --version
```

Deberías ver algo como:
```
13.0.0
```

## Paso 3: Iniciar sesión en Firebase

```powershell
firebase login
```

Esto abrirá tu navegador. Inicia sesión con tu cuenta de Google que usas para Firebase.

## Paso 4: Verificar que estás en el proyecto correcto

```powershell
cd C:\Users\Freddy\HypeMatch
firebase projects:list
```

Deberías ver tu proyecto de Firebase listado.

## Paso 5: Inicializar Firebase Functions

```powershell
firebase init functions
```

**Responde a las preguntas:**

1. **¿Qué proyecto quieres usar?**
   - Selecciona tu proyecto existente (HypeMatch o como lo hayas llamado)

2. **¿Qué lenguaje quieres usar?**
   - Selecciona: **JavaScript**

3. **¿Quieres usar ESLint?**
   - Puedes decir: **No** (para simplificar)

4. **¿Instalar dependencias ahora?**
   - Selecciona: **Sí**

Esto creará una carpeta `functions/` en tu proyecto.

## Paso 6: Instalar dependencia de Agora

```powershell
cd functions
npm install agora-access-token
```

## Paso 7: Copiar el código de la función

Tienes dos opciones:

### Opción A: Copiar archivo completo

```powershell
# Volver a la raíz del proyecto
cd ..

# Copiar el archivo
copy functions_index.js functions\index.js
```

### Opción B: Copiar manualmente

1. Abre `functions_index.js` (está en la raíz de tu proyecto)
2. Copia TODO el contenido
3. Abre `functions\index.js`
4. Reemplaza TODO el contenido con lo que copiaste
5. Guarda el archivo

## Paso 8: Desplegar a Firebase

```powershell
# Asegúrate de estar en la raíz del proyecto
cd C:\Users\Freddy\HypeMatch

# Desplegar
firebase deploy --only functions
```

Esto puede tomar 2-3 minutos. Verás algo como:

```
✔  functions[generateAgoraToken(us-central1)] Successful create operation.
✔  functions[generateStreamerToken(us-central1)] Successful create operation.
✔  functions[generateViewerToken(us-central1)] Successful create operation.

✔  Deploy complete!
```

## Paso 9: Verificar en Firebase Console

1. Ve a: https://console.firebase.google.com/
2. Selecciona tu proyecto
3. Ve a **Functions** en el menú lateral
4. Deberías ver 3 funciones desplegadas:
   - `generateAgoraToken`
   - `generateStreamerToken`
   - `generateViewerToken`

## 🎉 ¡Listo!

Ahora puedes continuar con el siguiente paso: actualizar `FirebaseManager.kt`

---

## 🐛 Problemas Comunes

### Error: "npm no se reconoce"

**Solución:** Node.js no está instalado o no está en el PATH.
1. Reinstala Node.js
2. Reinicia tu terminal
3. Intenta de nuevo

### Error: "firebase no se reconoce"

**Solución:** Firebase CLI no está instalado correctamente.
```powershell
npm install -g firebase-tools
```

### Error: "Permission denied" al instalar

**Solución:** Ejecuta PowerShell como Administrador:
1. Busca "PowerShell" en el menú inicio
2. Click derecho > "Ejecutar como administrador"
3. Intenta de nuevo

### Error al desplegar: "Project not found"

**Solución:** No has seleccionado el proyecto correcto.
```powershell
firebase use --add
```
Selecciona tu proyecto de la lista.

### Error: "Functions region not supported"

**Solución:** Esto es normal, Firebase elegirá la región automáticamente (us-central1).

---

## 📝 Comandos Útiles

```powershell
# Ver proyectos de Firebase
firebase projects:list

# Cambiar de proyecto
firebase use NOMBRE_DEL_PROYECTO

# Ver logs de las funciones
firebase functions:log

# Eliminar funciones
firebase functions:delete NOMBRE_FUNCION

# Ver estado del deploy
firebase deploy --only functions --debug
```

---

## ⏭️ Siguiente Paso

Una vez desplegadas las funciones, continúa con:
**Actualizar FirebaseManager.kt** (instrucciones en `DESPLEGAR_CLOUD_FUNCTION.md`)
