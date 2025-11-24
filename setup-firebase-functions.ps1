# Script para configurar Firebase Functions para Agora
# Ejecuta este script después de instalar Node.js y Firebase CLI

Write-Host "🚀 Configurando Firebase Functions para Agora..." -ForegroundColor Cyan
Write-Host ""

# Verificar Node.js
Write-Host "📦 Verificando Node.js..." -ForegroundColor Yellow
try {
    $nodeVersion = node --version
    Write-Host "✅ Node.js instalado: $nodeVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Node.js no está instalado" -ForegroundColor Red
    Write-Host "Por favor, instala Node.js desde: https://nodejs.org/" -ForegroundColor Yellow
    Write-Host "Luego ejecuta este script de nuevo." -ForegroundColor Yellow
    pause
    exit
}

# Verificar Firebase CLI
Write-Host "🔥 Verificando Firebase CLI..." -ForegroundColor Yellow
try {
    $firebaseVersion = firebase --version
    Write-Host "✅ Firebase CLI instalado: $firebaseVersion" -ForegroundColor Green
} catch {
    Write-Host "❌ Firebase CLI no está instalado" -ForegroundColor Red
    Write-Host "Instalando Firebase CLI..." -ForegroundColor Yellow
    npm install -g firebase-tools
    Write-Host "✅ Firebase CLI instalado" -ForegroundColor Green
}

Write-Host ""
Write-Host "🔐 Iniciando sesión en Firebase..." -ForegroundColor Yellow
Write-Host "Se abrirá tu navegador. Inicia sesión con tu cuenta de Google." -ForegroundColor Cyan
firebase login

Write-Host ""
Write-Host "📋 Proyectos de Firebase disponibles:" -ForegroundColor Yellow
firebase projects:list

Write-Host ""
Write-Host "🎯 Inicializando Firebase Functions..." -ForegroundColor Yellow
Write-Host "IMPORTANTE: Cuando te pregunte:" -ForegroundColor Cyan
Write-Host "  1. Selecciona tu proyecto existente" -ForegroundColor White
Write-Host "  2. Lenguaje: JavaScript" -ForegroundColor White
Write-Host "  3. ESLint: No" -ForegroundColor White
Write-Host "  4. Instalar dependencias: Sí" -ForegroundColor White
Write-Host ""
pause

firebase init functions

Write-Host ""
Write-Host "📦 Instalando dependencia de Agora..." -ForegroundColor Yellow
cd functions
npm install agora-access-token
cd ..

Write-Host ""
Write-Host "📄 Copiando código de la función..." -ForegroundColor Yellow
if (Test-Path "functions_index.js") {
    Copy-Item "functions_index.js" "functions\index.js" -Force
    Write-Host "✅ Código copiado a functions\index.js" -ForegroundColor Green
} else {
    Write-Host "⚠️  No se encontró functions_index.js" -ForegroundColor Yellow
    Write-Host "Por favor, copia manualmente el contenido de functions_index.js a functions\index.js" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "🚀 ¿Desplegar funciones a Firebase ahora? (S/N)" -ForegroundColor Yellow
$deploy = Read-Host

if ($deploy -eq "S" -or $deploy -eq "s") {
    Write-Host "📤 Desplegando funciones..." -ForegroundColor Yellow
    firebase deploy --only functions
    
    Write-Host ""
    Write-Host "🎉 ¡Funciones desplegadas exitosamente!" -ForegroundColor Green
    Write-Host ""
    Write-Host "✅ Verifica en Firebase Console:" -ForegroundColor Cyan
    Write-Host "   https://console.firebase.google.com/" -ForegroundColor White
    Write-Host ""
    Write-Host "⏭️  Siguiente paso:" -ForegroundColor Yellow
    Write-Host "   Actualiza FirebaseManager.kt (ver DESPLEGAR_CLOUD_FUNCTION.md)" -ForegroundColor White
} else {
    Write-Host ""
    Write-Host "⏸️  Deploy cancelado" -ForegroundColor Yellow
    Write-Host "Para desplegar más tarde, ejecuta:" -ForegroundColor Cyan
    Write-Host "   firebase deploy --only functions" -ForegroundColor White
}

Write-Host ""
Write-Host "✨ Configuración completada" -ForegroundColor Green
pause
