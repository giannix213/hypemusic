# 🚀 Resumen: Subir a GitHub en 3 Pasos

## ⚡ OPCIÓN RÁPIDA (Recomendada)

### 1. Ejecutar script automático
```bash
subir-a-github.bat
```

El script te guiará paso a paso.

## 📝 OPCIÓN MANUAL

### 1. Crear repositorio en GitHub
1. Ve a https://github.com/new
2. Nombre: `hypematch-app`
3. **NO marques** "Initialize with README"
4. Click "Create repository"
5. Copia la URL que te da

### 2. Subir tu proyecto
```bash
# Inicializar Git
git init

# Agregar archivos
git add .

# Primer commit
git commit -m "Initial commit: App con Live Streaming"

# Conectar con GitHub (reemplaza con tu URL)
git remote add origin https://github.com/TU_USUARIO/hypematch-app.git

# Subir
git branch -M main
git push -u origin main
```

### 3. Agregar colaboradores
1. En GitHub: Settings → Collaborators
2. Click "Add people"
3. Ingresa el username de tu compañero

## 🔐 IMPORTANTE: Archivos Sensibles

**YA ESTÁN PROTEGIDOS:**
- ✅ `.gitignore` creado
- ✅ `AgoraConfig.example.kt` creado
- ✅ `README.md` con instrucciones

**NUNCA subas:**
- ❌ `google-services.json`
- ❌ `AgoraConfig.kt` (solo el .example)
- ❌ Claves API o contraseñas

## 👥 Para tu Compañero

Cuando tu compañero clone el repositorio:

```bash
# 1. Clonar
git clone https://github.com/TU_USUARIO/hypematch-app.git
cd hypematch-app

# 2. Configurar Agora
copy app\src\main\java\com\metu\hypematch\AgoraConfig.example.kt AgoraConfig.kt
# Editar AgoraConfig.kt con las credenciales (envíaselas por mensaje privado)

# 3. Agregar google-services.json
# (Envíale el archivo por mensaje privado)

# 4. Compilar
gradlew assembleDebug
```

## 🔄 Trabajo Diario

### Antes de trabajar:
```bash
git pull
```

### Después de hacer cambios:
```bash
git add .
git commit -m "Descripción de los cambios"
git push
```

## 📚 Archivos Creados

- ✅ `GUIA_SUBIR_A_GITHUB.md` - Guía completa
- ✅ `subir-a-github.bat` - Script automático
- ✅ `README.md` - Documentación del proyecto
- ✅ `AgoraConfig.example.kt` - Ejemplo de configuración
- ✅ `.gitignore` - Archivos a ignorar

## 🎯 Próximos Pasos

1. [ ] Ejecutar `subir-a-github.bat`
2. [ ] Agregar colaboradores en GitHub
3. [ ] Compartir credenciales por mensaje privado
4. [ ] Tu compañero clona el repo
5. [ ] Tu compañero configura archivos sensibles
6. [ ] ¡A trabajar en equipo! 🚀

---

**¿Dudas?** Lee la guía completa: [`GUIA_SUBIR_A_GITHUB.md`](GUIA_SUBIR_A_GITHUB.md)
