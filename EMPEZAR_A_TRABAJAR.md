# 🚀 Cómo Empezar a Trabajar - Checklist

## ✅ Paso 1: Conseguir google-services.json

**ESTADO: ❌ PENDIENTE**

### Qué hacer:
1. Contacta a tu hermana
2. Pídele el archivo `google-services.json`
3. Guárdalo en: `E:\hype\hypemusic\app\google-services.json`

### Verificar:
```cmd
dir app\google-services.json
```

---

## ✅ Paso 2: Configurar Git (Ya está listo)

**ESTADO: ✅ COMPLETADO**

Git ya está instalado y configurado.

---

## ✅ Paso 3: Sincronizar con GitHub

### Antes de trabajar cada día:

**Opción A - Usando script:**
```
Doble clic en: actualizar-proyecto.bat
```

**Opción B - Usando Android Studio:**
1. VCS → Update Project
2. O presiona: Ctrl + T

---

## ✅ Paso 4: Compilar el Proyecto

Una vez tengas `google-services.json`:

1. En Android Studio: Build → Rebuild Project
2. Espera a que termine (puede tardar varios minutos la primera vez)
3. Si todo está bien, verás: "BUILD SUCCESSFUL"

---

## ✅ Paso 5: Trabajar Normalmente

1. Edita el código que necesites
2. Prueba en el emulador o dispositivo
3. Cuando termines, guarda los cambios (ver Paso 6)

---

## ✅ Paso 6: Subir tus Cambios

### Después de trabajar:

**Opción A - Usando script:**
```
Doble clic en: guardar-cambios.bat
Escribe qué cambiaste
```

**Opción B - Usando Android Studio:**
1. VCS → Commit (Ctrl + K)
2. Escribe qué cambiaste
3. Click en "Commit and Push"

---

## 🔄 Flujo de Trabajo Diario

```
1. Actualizar proyecto (git pull)
   ↓
2. Trabajar en el código
   ↓
3. Probar que funcione
   ↓
4. Guardar cambios (git commit + push)
   ↓
5. Avisar a tu hermana
```

---

## ⚠️ Reglas Importantes

1. **SIEMPRE actualiza antes de trabajar** - Evita conflictos
2. **Comunícate con tu hermana** - Dile en qué vas a trabajar
3. **Commits frecuentes** - No esperes días para subir cambios
4. **Mensajes claros** - "Arreglé el login" es mejor que "cambios"
5. **No trabajes en el mismo archivo** - Coordínense

---

## 🆘 Si algo sale mal

### Error al compilar:
- Verifica que tengas `google-services.json`
- Build → Clean Project
- Build → Rebuild Project

### Error al hacer git pull:
- Guarda tus cambios primero con `guardar-cambios.bat`
- Luego intenta actualizar de nuevo

### Conflictos de Git:
- Contacta a tu hermana
- Resuelvan juntos qué código mantener

---

## 📞 Contacto

Si tienes dudas, pregúntale a tu hermana o busca en la documentación del proyecto.

**Archivos útiles:**
- `README.md` - Información general del proyecto
- `GUIA_COLABORACION_GITHUB.md` - Guía completa de Git
- `DESCARGAR_GOOGLE_SERVICES.md` - Cómo obtener el archivo de Firebase
