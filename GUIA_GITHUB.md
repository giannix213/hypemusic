# 📦 Guía para Subir tu Proyecto a GitHub

## Paso 1: Instalar Git

### Opción A: Descargar Git para Windows
1. Ve a: https://git-scm.com/download/win
2. Descarga el instalador (64-bit)
3. Ejecuta el instalador
4. Usa las opciones por defecto (solo presiona "Next")
5. Reinicia PowerShell o CMD después de instalar

### Opción B: Instalar con Winget (si tienes Windows 10/11)
```powershell
winget install --id Git.Git -e --source winget
```

### Opción C: Instalar con Chocolatey (si lo tienes)
```powershell
choco install git
```

## Paso 2: Configurar Git (Primera vez)

Después de instalar, abre PowerShell y ejecuta:

```powershell
# Configurar tu nombre
git config --global user.name "Tu Nombre"

# Configurar tu email (usa el mismo de GitHub)
git config --global user.email "tu-email@ejemplo.com"

# Verificar configuración
git config --list
```

## Paso 3: Crear Repositorio en GitHub

1. Ve a: https://github.com
2. Inicia sesión (o crea una cuenta si no tienes)
3. Haz clic en el botón **"+"** (arriba a la derecha) > **"New repository"**
4. Configura:
   - **Repository name:** `HypeMatch` (o el nombre que prefieras)
   - **Description:** "App de música estilo Tinder para descubrir artistas"
   - **Visibility:** 
     - ✅ **Private** (si quieres que solo tú lo veas)
     - ⬜ **Public** (si quieres que sea público)
   - ⬜ NO marques "Add a README file" (ya tienes uno)
   - ⬜ NO agregues .gitignore (ya tienes uno)
5. Haz clic en **"Create repository"**

## Paso 4: Inicializar Git en tu Proyecto

Abre PowerShell en la carpeta de tu proyecto (`C:\Users\Freddy\HypeMatch`) y ejecuta:

```powershell
# Inicializar repositorio Git
git init

# Agregar todos los archivos
git add .

# Hacer el primer commit
git commit -m "Initial commit - HypeMatch app con verificación de email, likes y estados"

# Cambiar la rama principal a 'main' (GitHub usa 'main' por defecto)
git branch -M main

# Conectar con GitHub (reemplaza TU-USUARIO con tu usuario de GitHub)
git remote add origin https://github.com/TU-USUARIO/HypeMatch.git

# Subir el código a GitHub
git push -u origin main
```

### Si te pide autenticación:
GitHub ya no acepta contraseñas. Necesitas usar un **Personal Access Token**:

1. Ve a: https://github.com/settings/tokens
2. Haz clic en **"Generate new token"** > **"Generate new token (classic)"**
3. Configura:
   - **Note:** "HypeMatch App"
   - **Expiration:** 90 days (o lo que prefieras)
   - **Scopes:** Marca ✅ **repo** (todos los permisos de repositorio)
4. Haz clic en **"Generate token"**
5. **COPIA EL TOKEN** (solo se muestra una vez)
6. Cuando Git te pida contraseña, pega el token

## Paso 5: Verificar que se Subió

1. Ve a tu repositorio en GitHub: `https://github.com/TU-USUARIO/HypeMatch`
2. Deberías ver todos tus archivos

## Comandos Git Útiles para el Futuro

### Guardar cambios:
```powershell
# Ver qué archivos cambiaron
git status

# Agregar archivos modificados
git add .

# Hacer commit con mensaje
git commit -m "Descripción de los cambios"

# Subir a GitHub
git push
```

### Descargar cambios (si trabajas desde otro dispositivo):
```powershell
git pull
```

### Ver historial:
```powershell
git log --oneline
```

### Crear una rama nueva:
```powershell
git checkout -b nombre-de-rama
```

## Opción 2: Usar GitHub Desktop (Más Fácil)

Si prefieres una interfaz gráfica:

1. Descarga GitHub Desktop: https://desktop.github.com
2. Instala y abre la aplicación
3. Inicia sesión con tu cuenta de GitHub
4. Haz clic en **"Add"** > **"Add existing repository"**
5. Selecciona la carpeta `C:\Users\Freddy\HypeMatch`
6. Haz clic en **"Publish repository"**
7. Configura el nombre y privacidad
8. ¡Listo!

## Opción 3: Usar Android Studio (Integrado)

Android Studio tiene Git integrado:

1. En Android Studio, ve a **VCS** > **Enable Version Control Integration**
2. Selecciona **Git**
3. Ve a **VCS** > **Share Project on GitHub**
4. Inicia sesión con tu cuenta de GitHub
5. Configura el nombre del repositorio
6. Haz clic en **Share**

## Archivos que NO se Subirán (ya están en .gitignore)

Tu `.gitignore` ya está configurado para excluir:
- ✅ Archivos de compilación (`build/`, `*.apk`)
- ✅ Archivos de configuración local (`.idea/`, `local.properties`)
- ✅ Archivos temporales
- ✅ Claves y secretos

## Archivos Sensibles - ⚠️ IMPORTANTE

**NUNCA subas a GitHub:**
- ❌ `google-services.json` (configuración de Firebase)
- ❌ Claves API
- ❌ Contraseñas
- ❌ Tokens de acceso

Si ya los subiste por error:
```powershell
# Eliminar del historial (cuidado, esto reescribe el historial)
git filter-branch --force --index-filter \
  "git rm --cached --ignore-unmatch google-services.json" \
  --prune-empty --tag-name-filter cat -- --all

# Forzar push
git push origin --force --all
```

## Estructura Recomendada de Commits

Usa mensajes descriptivos:

```powershell
# ✅ Buenos mensajes
git commit -m "feat: Agregar verificación de email"
git commit -m "fix: Corregir error en MyMusicScreen"
git commit -m "docs: Actualizar README con nuevas funcionalidades"

# ❌ Malos mensajes
git commit -m "cambios"
git commit -m "fix"
git commit -m "asdf"
```

## Colaboración

Si quieres que otros contribuyan:

1. Ve a tu repositorio en GitHub
2. **Settings** > **Collaborators**
3. Agrega colaboradores por su usuario de GitHub

## Resumen de Comandos Rápidos

```powershell
# Primera vez
git init
git add .
git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/TU-USUARIO/HypeMatch.git
git push -u origin main

# Día a día
git add .
git commit -m "Descripción de cambios"
git push

# Ver estado
git status
git log --oneline
```

## Recursos Adicionales

- **Documentación oficial de Git:** https://git-scm.com/doc
- **GitHub Guides:** https://guides.github.com
- **Git Cheat Sheet:** https://education.github.com/git-cheat-sheet-education.pdf
- **Curso interactivo:** https://learngitbranching.js.org

## Solución de Problemas Comunes

### Error: "fatal: not a git repository"
```powershell
git init
```

### Error: "failed to push some refs"
```powershell
git pull origin main --rebase
git push
```

### Error: "Permission denied"
- Verifica que estés usando el token correcto
- Asegúrate de que el repositorio existe en GitHub

### Olvidé hacer commit antes de cambiar de rama
```powershell
git stash
git checkout otra-rama
git stash pop
```

---

¿Necesitas ayuda con algún paso específico? ¡Avísame!
