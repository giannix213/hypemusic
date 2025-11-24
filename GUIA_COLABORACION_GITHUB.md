# 👥 Guía para Trabajar en Equipo con GitHub

## Paso 1: Agregar a tu Compañero como Colaborador

### Opción A: Repositorio Privado (Recomendado para proyectos personales)

1. Ve a tu repositorio en GitHub: `https://github.com/TU-USUARIO/HypeMatch`
2. Haz clic en **Settings** (⚙️ arriba a la derecha)
3. En el menú izquierdo, haz clic en **Collaborators**
4. Haz clic en **Add people**
5. Ingresa el **usuario de GitHub** o **email** de tu compañero
6. Haz clic en **Add [nombre] to this repository**
7. Tu compañero recibirá un email de invitación
8. Debe aceptar la invitación para tener acceso

### Opción B: Repositorio Público (Si quieres que sea open source)

1. Ve a **Settings** > **General**
2. Baja hasta **Danger Zone**
3. Haz clic en **Change visibility** > **Make public**
4. Confirma escribiendo el nombre del repositorio
5. Ahora cualquiera puede ver y clonar el proyecto
6. Para que tu compañero pueda hacer cambios, agrégalo como colaborador (pasos anteriores)

## Paso 2: Tu Compañero Clona el Repositorio

Tu compañero debe hacer esto en su computadora:

### Opción 1: Usando Git (Línea de comandos)

```powershell
# Navegar a la carpeta donde quiere el proyecto
cd C:\Users\SuNombre\Proyectos

# Clonar el repositorio
git clone https://github.com/TU-USUARIO/HypeMatch.git

# Entrar a la carpeta
cd HypeMatch

# Verificar que está conectado
git remote -v
```

### Opción 2: Usando GitHub Desktop

1. Abre GitHub Desktop
2. **File** > **Clone repository**
3. Busca **HypeMatch** en la lista
4. Selecciona la carpeta donde quiere guardarlo
5. Haz clic en **Clone**

### Opción 3: Usando Android Studio

1. Abre Android Studio
2. **File** > **New** > **Project from Version Control**
3. Pega la URL: `https://github.com/TU-USUARIO/HypeMatch.git`
4. Selecciona la carpeta de destino
5. Haz clic en **Clone**

## Paso 3: Flujo de Trabajo en Equipo

### 🔄 Flujo Básico (Recomendado para Principiantes)

#### Antes de empezar a trabajar:
```powershell
# 1. Descargar los últimos cambios
git pull origin main
```

#### Después de hacer cambios:
```powershell
# 2. Ver qué archivos cambiaron
git status

# 3. Agregar los cambios
git add .

# 4. Hacer commit con mensaje descriptivo
git commit -m "feat: Agregar pantalla de configuración"

# 5. Subir los cambios
git push origin main
```

### 🌿 Flujo con Ramas (Recomendado para Equipos)

Cada persona trabaja en su propia rama y luego hace un **Pull Request**.

#### Crear una rama para tu funcionalidad:
```powershell
# Crear y cambiar a una nueva rama
git checkout -b feature/nombre-funcionalidad

# Ejemplo:
git checkout -b feature/perfil-usuario
git checkout -b fix/corregir-login
git checkout -b docs/actualizar-readme
```

#### Trabajar en tu rama:
```powershell
# Hacer cambios en el código...

# Agregar y hacer commit
git add .
git commit -m "feat: Agregar edición de perfil"

# Subir tu rama a GitHub
git push origin feature/perfil-usuario
```

#### Crear Pull Request:
1. Ve a GitHub
2. Verás un banner amarillo: **"Compare & pull request"**
3. Haz clic en el botón
4. Escribe una descripción de los cambios
5. Haz clic en **Create pull request**
6. Tu compañero revisa el código
7. Si está bien, hace clic en **Merge pull request**

#### Actualizar tu rama main local:
```powershell
# Cambiar a main
git checkout main

# Descargar los cambios
git pull origin main

# Eliminar tu rama local (ya está en main)
git branch -d feature/perfil-usuario
```

## Paso 4: Resolver Conflictos

### ¿Qué es un conflicto?
Ocurre cuando dos personas modifican la misma línea de código.

### Cómo resolverlo:

```powershell
# 1. Intentar hacer pull
git pull origin main

# Si hay conflicto, verás algo como:
# CONFLICT (content): Merge conflict in MainActivity.kt
```

#### En el archivo verás marcas como esta:
```kotlin
<<<<<<< HEAD
// Tu código
val nombre = "Freddy"
=======
// Código de tu compañero
val nombre = "Juan"
>>>>>>> origin/main
```

#### Para resolver:
1. Abre el archivo en Android Studio
2. Decide qué código mantener
3. Elimina las marcas `<<<<<<<`, `=======`, `>>>>>>>`
4. Guarda el archivo
5. Haz commit:
```powershell
git add .
git commit -m "fix: Resolver conflicto en MainActivity"
git push origin main
```

## Paso 5: Buenas Prácticas para Trabajar en Equipo

### ✅ DO (Hacer):

1. **Siempre hacer `git pull` antes de empezar a trabajar**
   ```powershell
   git pull origin main
   ```

2. **Hacer commits frecuentes con mensajes claros**
   ```powershell
   git commit -m "feat: Agregar botón de compartir"
   git commit -m "fix: Corregir error en login"
   git commit -m "docs: Actualizar README"
   ```

3. **Usar ramas para funcionalidades nuevas**
   ```powershell
   git checkout -b feature/nueva-funcionalidad
   ```

4. **Comunicarse con tu compañero**
   - "Voy a trabajar en la pantalla de perfil"
   - "Ya subí los cambios del login"
   - "Hay un conflicto en MainActivity, ¿lo revisamos juntos?"

5. **Revisar el código del compañero antes de hacer merge**

### ❌ DON'T (No hacer):

1. **No trabajar ambos en el mismo archivo al mismo tiempo**
   - Coordínense para evitar conflictos

2. **No hacer `git push --force`**
   - Puede borrar el trabajo de tu compañero

3. **No subir archivos grandes o innecesarios**
   - APKs, archivos de compilación, etc.

4. **No subir información sensible**
   - Contraseñas, API keys, tokens

5. **No hacer commits con mensajes vagos**
   - ❌ "cambios"
   - ❌ "fix"
   - ✅ "feat: Agregar verificación de email"

## Paso 6: Comandos Útiles para Trabajo en Equipo

### Ver quién hizo qué cambio:
```powershell
# Ver historial de commits
git log --oneline --graph --all

# Ver quién modificó cada línea de un archivo
git blame MainActivity.kt
```

### Ver diferencias:
```powershell
# Ver cambios no guardados
git diff

# Ver cambios entre ramas
git diff main feature/nueva-funcionalidad
```

### Deshacer cambios:
```powershell
# Deshacer cambios en un archivo (antes de commit)
git checkout -- MainActivity.kt

# Deshacer el último commit (mantiene los cambios)
git reset --soft HEAD~1

# Deshacer el último commit (borra los cambios)
git reset --hard HEAD~1
```

### Guardar cambios temporalmente:
```powershell
# Guardar cambios sin hacer commit
git stash

# Ver lista de stashes
git stash list

# Recuperar los cambios
git stash pop
```

## Paso 7: Estructura de Ramas Recomendada

```
main (producción)
  ├── develop (desarrollo)
  │   ├── feature/login
  │   ├── feature/perfil
  │   └── feature/estados
  └── hotfix/bug-critico
```

### Crear estructura:
```powershell
# Crear rama develop
git checkout -b develop
git push origin develop

# Crear feature desde develop
git checkout develop
git checkout -b feature/login
```

## Paso 8: Configurar Protección de Ramas

Para evitar que alguien suba código directamente a `main`:

1. Ve a **Settings** > **Branches**
2. Haz clic en **Add rule**
3. En **Branch name pattern** escribe: `main`
4. Marca:
   - ✅ **Require a pull request before merging**
   - ✅ **Require approvals** (al menos 1)
5. Haz clic en **Create**

Ahora todos los cambios a `main` deben pasar por Pull Request.

## Paso 9: Usar Issues para Organizar Tareas

1. Ve a la pestaña **Issues** en GitHub
2. Haz clic en **New issue**
3. Crea tareas como:
   - "Implementar pantalla de configuración"
   - "Corregir bug en reproducción de música"
   - "Agregar tests unitarios"
4. Asigna issues a cada persona
5. Usa labels: `bug`, `enhancement`, `documentation`

## Paso 10: Ejemplo de Flujo Completo

### Tú (Freddy):
```powershell
# Día 1 - Mañana
git pull origin main
git checkout -b feature/estados-artistas
# ... trabajas en el código ...
git add .
git commit -m "feat: Agregar modelo de estados"
git push origin feature/estados-artistas
# Crear Pull Request en GitHub

# Día 1 - Tarde (después de que tu compañero aprobó el PR)
git checkout main
git pull origin main
```

### Tu Compañero (Juan):
```powershell
# Día 1 - Mañana
git pull origin main
git checkout -b feature/perfil-usuario
# ... trabaja en el código ...
git add .
git commit -m "feat: Agregar edición de perfil"
git push origin feature/perfil-usuario
# Crear Pull Request en GitHub

# Día 1 - Tarde
# Revisa el PR de Freddy en GitHub
# Hace clic en "Approve" y "Merge"
git checkout main
git pull origin main
```

## Resumen de Comandos Esenciales

```powershell
# Antes de trabajar
git pull origin main

# Crear rama
git checkout -b feature/nombre

# Guardar cambios
git add .
git commit -m "descripción"
git push origin feature/nombre

# Actualizar main
git checkout main
git pull origin main

# Ver estado
git status
git log --oneline
```

## Recursos Adicionales

- **GitHub Flow:** https://guides.github.com/introduction/flow/
- **Git Branching:** https://learngitbranching.js.org
- **Conventional Commits:** https://www.conventionalcommits.org

## Solución de Problemas Comunes

### "Permission denied"
- Tu compañero no aceptó la invitación
- Verifica en Settings > Collaborators

### "Merge conflict"
- Coordínense para no trabajar en el mismo archivo
- Si ocurre, resuélvanlo juntos

### "Your branch is behind"
```powershell
git pull origin main
```

### "Failed to push"
```powershell
git pull origin main --rebase
git push
```

---

¡Listo! Con esta guía tú y tu compañero pueden trabajar juntos sin problemas. 🚀

**Tip final:** Comuníquense constantemente y hagan `git pull` frecuentemente para evitar conflictos.
