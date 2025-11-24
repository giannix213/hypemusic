# ✅ Checklist - Solucionar Error de Live

Sigue estos pasos en orden. Marca cada uno cuando lo completes.

## 📋 Preparación

- [ ] **Abrir terminal** en la carpeta del proyecto
- [ ] **Tener Android Studio abierto**
- [ ] **Tener internet funcionando**

## 🔧 Paso 1: Verificar Firebase CLI

```bash
firebase --version
```

- [ ] ✅ Firebase CLI está instalado (muestra versión)
- [ ] ❌ Si no está instalado, ejecutar: `npm install -g firebase-tools`

## 🔑 Paso 2: Login en Firebase

```bash
firebase login
```

- [ ] ✅ Ya estoy logueado
- [ ] ❌ Si no, seguir las instrucciones en el navegador

## 📡 Paso 3: Verificar Cloud Functions

```bash
firebase functions:list
```

**¿Qué ves?**

- [ ] ✅ Veo 3 funciones: `generateAgoraToken`, `generateStreamerToken`, `generateViewerToken`
- [ ] ❌ No veo funciones o veo un error → Continuar al Paso 4

## 🚀 Paso 4: Desplegar Cloud Functions (si es necesario)

### 4.1 Instalar dependencias
```bash
cd functions
npm install
```

- [ ] ✅ Dependencias instaladas sin errores
- [ ] ❌ Si hay errores, verificar que Node.js esté instalado

### 4.2 Volver a la raíz del proyecto
```bash
cd ..
```

- [ ] ✅ Estoy en la raíz del proyecto

### 4.3 Desplegar funciones
```bash
firebase deploy --only functions
```

**Esto puede tomar 2-3 minutos. Espera a que termine.**

- [ ] ✅ Despliegue completado exitosamente
- [ ] ❌ Si hay errores, verificar que el proyecto de Firebase esté configurado

### 4.4 Verificar nuevamente
```bash
firebase functions:list
```

- [ ] ✅ Ahora veo las 3 funciones desplegadas

## 🏗️ Paso 5: Rebuild de la App

### Opción A: Desde Android Studio
1. Click en `Build` en el menú superior
2. Click en `Clean Project`
3. Esperar a que termine
4. Click en `Build` nuevamente
5. Click en `Rebuild Project`

- [ ] ✅ Clean completado
- [ ] ✅ Rebuild completado sin errores

### Opción B: Desde terminal
```bash
gradlew clean
gradlew build
```

- [ ] ✅ Build completado sin errores

## 📱 Paso 6: Probar el Live

1. **Ejecutar la app** en emulador o dispositivo físico
   - [ ] ✅ App ejecutándose

2. **Abrir Logcat** en Android Studio
   - [ ] ✅ Logcat abierto
   - [ ] ✅ Filtro configurado: `FirebaseManager`

3. **Ir a la pestaña Live** en la app
   - [ ] ✅ Estoy en la pestaña Live

4. **Tocar el botón para iniciar Live**
   - [ ] ✅ Veo "Preparando Live..."

5. **Revisar logs en Logcat**

**¿Qué ves en los logs?**

- [ ] ✅ Veo: `🎬 ===== INICIANDO NUEVA SESIÓN DE LIVE =====`
- [ ] ✅ Veo: `📺 Canal generado: live_...`
- [ ] ✅ Veo: `🔑 Solicitando token de Agora...`
- [ ] ✅ Veo: `✅ Token de Agora recibido: ...`
- [ ] ✅ Veo: `✅ Sesión creada en Firestore: ...`
- [ ] ✅ Veo: `✅ ===== SESIÓN DE LIVE LISTA =====`

6. **Verificar la pantalla de la app**

- [ ] ✅ La cámara se activó
- [ ] ✅ Veo mi imagen en la pantalla
- [ ] ✅ Veo el botón rojo para finalizar
- [ ] ✅ Puedo transmitir en vivo

## 🎉 Resultado Final

- [ ] ✅ **TODO FUNCIONA** - El Live se inicia correctamente
- [ ] ❌ **AÚN HAY PROBLEMAS** - Ver sección de Troubleshooting

## 🆘 Troubleshooting

### Si ves error: "Cloud Function not found"
- [ ] Volver al Paso 4 y desplegar las funciones
- [ ] Verificar que `firebase functions:list` muestre las funciones

### Si ves error: "Permission denied"
- [ ] Verificar que el usuario esté logueado en la app
- [ ] Revisar reglas de Firestore en Firebase Console

### Si ves error: "Network error"
- [ ] Verificar conexión a internet
- [ ] Intentar de nuevo

### Si la cámara no se activa
- [ ] Verificar permisos de cámara en la app
- [ ] Revisar logs de Logcat para más detalles

## 📞 Si Nada Funciona

Si después de completar todos los pasos el problema persiste:

1. **Captura de pantalla** del error en la app
2. **Copia los logs** de Logcat (filtra por `FirebaseManager`)
3. **Ejecuta** `firebase functions:list` y copia el resultado
4. **Comparte** toda esta información para diagnóstico

## 📚 Documentación Adicional

- `DIAGNOSTICO_RAPIDO_LIVE.md` - Guía de diagnóstico detallada
- `COMANDOS_SOLUCIONAR_LIVE.md` - Lista de comandos
- `SOLUCION_ERROR_LIVE.md` - Explicación técnica completa
- `RESUMEN_SOLUCION_LIVE.md` - Resumen de la solución

## 🎯 Scripts Automáticos

Si prefieres usar scripts automáticos:

```bash
# Verificar y desplegar todo automáticamente
verificar-y-desplegar-functions.bat

# Solo verificar el estado
verificar-functions.bat
```

---

**Tiempo estimado:** 10-15 minutos
**Dificultad:** Baja
**Éxito esperado:** 95%

¡Buena suerte! 🚀
