# 📱 Generar APK desde Android Studio

## 🎯 Método Rápido (Recomendado)

### Paso 1: Abrir el Menú Build
1. En Android Studio, ve al menú superior
2. Click en **`Build`**
3. Click en **`Build Bundle(s) / APK(s)`**
4. Click en **`Build APK(s)`**

```
Build > Build Bundle(s) / APK(s) > Build APK(s)
```

### Paso 2: Esperar la Compilación
- Verás una barra de progreso en la parte inferior
- Puede tomar 2-3 minutos
- Espera a que termine

### Paso 3: Localizar el APK
Cuando termine, verás una notificación en la esquina inferior derecha:

```
APK(s) generated successfully
[locate] [analyze]
```

**Click en `locate`** para abrir la carpeta con los APKs.

### Paso 4: Encontrar los APKs
Se abrirá la carpeta:
```
app/build/outputs/apk/release/
```

Verás 2 archivos:
- **`app-arm64-v8a-release.apk`** (~50 MB) ← Usa este
- **`app-armeabi-v7a-release.apk`** (~45 MB)

---

## 🎯 Método Alternativo (Con Firma)

Si necesitas firmar el APK para publicarlo:

### Paso 1: Generate Signed Bundle / APK
1. **`Build`** > **`Generate Signed Bundle / APK...`**
2. Selecciona **`APK`**
3. Click **`Next`**

### Paso 2: Crear o Seleccionar Keystore

**Si NO tienes keystore (primera vez):**
1. Click en **`Create new...`**
2. Llena los datos:
   ```
   Key store path: C:\Users\[TuUsuario]\keystore.jks
   Password: [tu_password]
   Alias: key0
   Password: [tu_password]
   Validity: 25 (años)
   First and Last Name: Tu Nombre
   ```
3. Click **`OK`**

**Si YA tienes keystore:**
1. Click en **`Choose existing...`**
2. Selecciona tu archivo `.jks`
3. Ingresa la contraseña

### Paso 3: Configurar Build
1. Selecciona **`release`**
2. Marca las casillas:
   - ☑ **V1 (Jar Signature)**
   - ☑ **V2 (Full APK Signature)**
3. Click **`Next`**

### Paso 4: Generar
1. Selecciona destino (o deja por defecto)
2. Click **`Finish`**
3. Espera a que termine

---

## 📁 Ubicación de los APKs

### Método Rápido:
```
app/build/outputs/apk/release/
```

### Método con Firma:
```
app/release/
```
o la carpeta que seleccionaste

---

## 🚀 Pasos Visuales Detallados

### 1️⃣ Menú Build
```
┌─────────────────────────────────┐
│ File  Edit  View  Navigate      │
│ Code  Refactor  Build  Run      │ ← Click aquí
│                                  │
│   ┌──────────────────────────┐  │
│   │ Make Project          F9 │  │
│   │ Make Module 'app'        │  │
│   │ Clean Project            │  │
│   │ Rebuild Project          │  │
│   │ ─────────────────────    │  │
│   │ Build Bundle(s)/APK(s) ► │  │ ← Click aquí
│   │   ├─ Build Bundle(s)     │  │
│   │   └─ Build APK(s)        │  │ ← Click aquí
│   └──────────────────────────┘  │
└─────────────────────────────────┘
```

### 2️⃣ Progreso de Build
```
┌─────────────────────────────────┐
│                                  │
│  Building...                     │
│  ████████████░░░░░░░░░  60%     │
│                                  │
│  > Task :app:compileReleaseKotlin│
└─────────────────────────────────┘
```

### 3️⃣ Notificación de Éxito
```
┌─────────────────────────────────┐
│ ✓ APK(s) generated successfully │
│                                  │
│   [locate]  [analyze]            │ ← Click en locate
└─────────────────────────────────┘
```

### 4️⃣ Carpeta con APKs
```
📁 app/build/outputs/apk/release/
  ├─ 📄 app-arm64-v8a-release.apk (50 MB)
  └─ 📄 app-armeabi-v7a-release.apk (45 MB)
```

---

## 📱 Instalar el APK en Otro Dispositivo

### Método 1: Cable USB
1. Conecta el dispositivo por USB
2. Habilita "Depuración USB" en el dispositivo
3. En Android Studio:
   - Click derecho en el APK
   - **`Reveal in Explorer`**
4. Arrastra el APK al dispositivo

### Método 2: Compartir
1. Copia el APK a tu teléfono:
   - **Google Drive**
   - **WhatsApp** (envíalo a ti mismo)
   - **Email**
   - **Bluetooth**

2. En el dispositivo:
   - Abre el archivo APK
   - Permite "Instalar desde fuentes desconocidas"
   - Toca **`Instalar`**

### Método 3: ADB (Línea de Comandos)
```bash
adb install app/build/outputs/apk/release/app-arm64-v8a-release.apk
```

---

## ⚡ Atajos de Teclado

| Acción | Atajo |
|--------|-------|
| Build APK | No hay atajo directo |
| Make Project | `Ctrl + F9` |
| Rebuild Project | `Ctrl + Shift + F9` |

---

## 🐛 Solución de Problemas

### Error: "Keystore not found"
**Solución:** Crea un nuevo keystore o usa el de debug

### Error: "Build failed"
**Solución:** 
1. `Build` > `Clean Project`
2. `Build` > `Rebuild Project`
3. Intenta de nuevo

### No aparece la notificación
**Solución:** 
1. Ve manualmente a: `app/build/outputs/apk/release/`
2. O busca en el panel "Build" en la parte inferior

### APK muy grande
**Solución:** Ya está optimizado con las configuraciones que agregué

---

## ✅ Checklist

- [ ] Abrir Android Studio
- [ ] `Build` > `Build Bundle(s) / APK(s)` > `Build APK(s)`
- [ ] Esperar a que termine (2-3 min)
- [ ] Click en `locate` en la notificación
- [ ] Copiar `app-arm64-v8a-release.apk`
- [ ] Instalar en otro dispositivo
- [ ] Probar el Live

---

## 🎯 Resumen Rápido

```
1. Build > Build Bundle(s) / APK(s) > Build APK(s)
2. Esperar 2-3 minutos
3. Click en "locate"
4. Copiar app-arm64-v8a-release.apk
5. Instalar en dispositivo
6. ¡Probar el Live!
```

---

**Tiempo total:** 5 minutos
**Tamaño del APK:** ~50 MB
**Listo para probar:** ✅
