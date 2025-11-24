# 🚀 Guía: Subir Proyecto a GitHub para Trabajo en Equipo

## 📋 OPCIÓN 1: Primera vez (Proyecto nuevo en GitHub)

### Paso 1: Crear repositorio en GitHub
1. Ve a https://github.com
2. Click en el botón "+" → "New repository"
3. Nombre: `hypematch-app` (o el que prefieras)
4. Descripción: "App de Live Streaming con Agora y Firebase"
5. **NO marques** "Initialize with README" (ya tienes archivos)
6. Click "Create repository"

### Paso 2: Configurar Git localmente (si es primera vez)
```bash
# Configurar tu nombre y email
git config --global user.name "Tu Nombre"
git config --global user.email "tu@email.com"
```

### Paso 3: Inicializar Git en tu proyecto
```bash
# Ir a la carpeta del proyecto
cd C:\ruta\a\tu\proyecto

# Inicializar Git
git init

# Agregar todos los archivos
git add .

# Hacer el primer commit
git commit -m "Initial commit: App con Live Streaming funcional"
```

### Paso 4: Conectar con GitHub y subir
```bash
# Conectar con tu repositorio (reemplaza con tu URL)
git remote add origin https://github.com/TU_USUARIO/hypematch-app.git

# Cambiar a rama main
git branch -M main

# Subir todo
git push -u origin main
```

## 📋 OPCIÓN 2: Ya tienes un repositorio

### Si ya existe el repositorio:
```bash
# Agregar cambios
git add .

# Hacer commit
git commit -m "Fix: Corregido problema de Live Streaming - LiveViewModel eliminado"

# Subir cambios
git push
```

## 🔐 IMPORTANTE: Archivos Sensibles

### Paso 1: Crear .gitignore (si no existe)
```bash
# Crear archivo .gitignore
echo. > .gitignore
```

### Paso 2: Agregar archivos a ignorar
Edita `.gitignore` y agrega:

```
# Android
*.iml
.gradle
/local.properties
/.idea/
.DS_Store
/build
/captures
.externalNativeBuild
.cxx
*.apk
*.ap_
*.dex

# Firebase (IMPORTANTE)
google-services.json
firebase-adminsdk-*.json

# Agora (IMPORTANTE)
**/AgoraConfig.kt

# Claves y secretos
*.keystore
*.jks
key.properties
secrets.properties

# Logs
*.log

# Gradle
.gradle/
build/
```

### Paso 3: Proteger archivos sensibles ANTES de subir

**CRÍTICO:** Antes de hacer el primer push, asegúrate de:

1. **Crear archivo de ejemplo para AgoraConfig:**
```bash
# Crear AgoraConfig.example.kt
```

Contenido de `AgoraConfig.example.kt`:
```kotlin
package com.metu.hypematch

object AgoraConfig {
    const val APP_ID = "TU_AGORA_APP_ID_AQUI"
    const val APP_CERTIFICATE = "TU_AGORA_CERTIFICATE_AQUI"
}
```

2. **Crear archivo de ejemplo para google-services:**
```bash
# Crear google-services.example.json
```

Contenido de `google-services.example.json`:
```json
{
  "project_info": {
    "project_id": "TU_PROJECT_ID",
    "firebase_url": "https://TU_PROJECT.firebaseio.com"
  },
  "client": [
    {
      "client_info": {
        "mobilesdk_app_id": "TU_APP_ID",
        "android_client_info": {
          "package_name": "com.metu.hypematch"
        }
      }
    }
  ]
}
```

## 👥 TRABAJO EN EQUIPO

### Para el dueño del repositorio:

#### 1. Agregar colaboradores
1. Ve a tu repositorio en GitHub
2. Settings → Collaborators
3. Click "Add people"
4. Ingresa el username o email de tu compañero
5. Envía la invitación

#### 2. Crear archivo README.md
```bash
# Crear README
echo. > README.md
```

Contenido sugerido:
```markdown
# HypeMatch - Live Streaming App

App de streaming en vivo con Agora y Firebase.

## 🚀 Setup para Desarrolladores

### 1. Clonar el repositorio
\`\`\`bash
git clone https://github.com/TU_USUARIO/hypematch-app.git
cd hypematch-app
\`\`\`

### 2. Configurar Firebase
1. Descarga `google-services.json` de Firebase Console
2. Colócalo en `app/google-services.json`

### 3. Configurar Agora
1. Copia `app/src/main/java/com/metu/hypematch/AgoraConfig.example.kt`
2. Renómbralo a `AgoraConfig.kt`
3. Reemplaza con tus credenciales de Agora

### 4. Compilar
\`\`\`bash
gradlew assembleDebug
\`\`\`

## 📚 Documentación
- [Inicio Rápido Live](INICIO_RAPIDO_LIVE.md)
- [Índice de Documentación](INDICE_SOLUCION_LIVE.md)
```

### Para los colaboradores:

#### 1. Clonar el repositorio
```bash
# Clonar
git clone https://github.com/USUARIO_DUEÑO/hypematch-app.git

# Entrar a la carpeta
cd hypematch-app
```

#### 2. Configurar archivos sensibles
```bash
# Copiar archivos de ejemplo
copy app\src\main\java\com\metu\hypematch\AgoraConfig.example.kt AgoraConfig.kt

# Editar con tus credenciales
# (Pedir al dueño las credenciales por mensaje privado)
```

#### 3. Trabajar en el proyecto
```bash
# Antes de empezar, actualizar
git pull

# Hacer cambios...

# Agregar cambios
git add .

# Commit
git commit -m "Descripción de los cambios"

# Subir
git push
```

## 🔄 FLUJO DE TRABAJO DIARIO

### Antes de empezar a trabajar:
```bash
# Actualizar tu copia local
git pull
```

### Después de hacer cambios:
```bash
# Ver qué cambió
git status

# Agregar archivos específicos
git add archivo1.kt archivo2.kt

# O agregar todos
git add .

# Hacer commit con mensaje descriptivo
git commit -m "Fix: Corregido problema de X"

# Subir cambios
git push
```

### Si hay conflictos:
```bash
# Git te avisará del conflicto
# Abre los archivos en conflicto y resuélvelos
# Busca las marcas: <<<<<<< HEAD

# Después de resolver:
git add .
git commit -m "Merge: Resueltos conflictos"
git push
```

## 🌿 TRABAJO CON RAMAS (Recomendado)

### Crear rama para nueva funcionalidad:
```bash
# Crear y cambiar a nueva rama
git checkout -b feature/nombre-funcionalidad

# Hacer cambios...

# Commit
git add .
git commit -m "Add: Nueva funcionalidad X"

# Subir rama
git push -u origin feature/nombre-funcionalidad
```

### Crear Pull Request:
1. Ve a GitHub
2. Verás un botón "Compare & pull request"
3. Describe los cambios
4. Pide revisión a tu compañero
5. Después de aprobación, haz merge

### Volver a rama principal:
```bash
# Cambiar a main
git checkout main

# Actualizar
git pull

# Eliminar rama local (opcional)
git branch -d feature/nombre-funcionalidad
```

## 📝 MENSAJES DE COMMIT RECOMENDADOS

```bash
# Nuevas funcionalidades
git commit -m "Add: Implementado chat en vivo"

# Correcciones
git commit -m "Fix: Corregido crash al iniciar Live"

# Mejoras
git commit -m "Improve: Optimizado rendimiento de video"

# Documentación
git commit -m "Docs: Actualizada guía de instalación"

# Refactorización
git commit -m "Refactor: Simplificado LiveLauncherScreen"

# Estilo/formato
git commit -m "Style: Formateado código según estándar"
```

## 🚨 COMANDOS DE EMERGENCIA

### Deshacer último commit (sin perder cambios):
```bash
git reset --soft HEAD~1
```

### Deshacer cambios en un archivo:
```bash
git checkout -- archivo.kt
```

### Ver historial:
```bash
git log --oneline
```

### Ver diferencias:
```bash
git diff
```

### Guardar cambios temporalmente:
```bash
# Guardar cambios sin commit
git stash

# Recuperar cambios guardados
git stash pop
```

## 🔐 SEGURIDAD: CHECKLIST

Antes de hacer el primer push, verifica:

- [ ] `.gitignore` está configurado
- [ ] `google-services.json` está en `.gitignore`
- [ ] `AgoraConfig.kt` está en `.gitignore`
- [ ] Archivos `.example` están creados
- [ ] No hay claves API en el código
- [ ] No hay contraseñas en el código
- [ ] README.md tiene instrucciones de setup

## 📞 COMPARTIR CREDENCIALES CON EL EQUIPO

**NUNCA subas credenciales a GitHub**

Opciones seguras:
1. **Mensaje privado** (Discord, WhatsApp, etc.)
2. **Gestor de contraseñas** (1Password, LastPass)
3. **Variables de entorno** (para CI/CD)
4. **Firebase App Distribution** (para APKs)

## 🎯 RESUMEN RÁPIDO

```bash
# Primera vez
git init
git add .
git commit -m "Initial commit"
git remote add origin https://github.com/USUARIO/REPO.git
git push -u origin main

# Día a día
git pull                    # Actualizar
# ... hacer cambios ...
git add .                   # Agregar cambios
git commit -m "Mensaje"     # Guardar cambios
git push                    # Subir cambios
```

## 📚 RECURSOS

- [Git Cheat Sheet](https://education.github.com/git-cheat-sheet-education.pdf)
- [GitHub Docs](https://docs.github.com)
- [Git Tutorial](https://www.atlassian.com/git/tutorials)
