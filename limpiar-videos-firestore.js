// Script alternativo para limpiar videos usando Firebase CLI
// NO requiere serviceAccountKey.json
// Ejecutar con: node limpiar-videos-firestore.js

const { execSync } = require('child_process');

console.log('🧹 ===== LIMPIEZA DE VIDEOS DE CONCURSOS =====');
console.log('');
console.log('Este script usa Firebase CLI para eliminar los videos.');
console.log('');

// Verificar si Firebase CLI está instalado
try {
  console.log('📋 Verificando Firebase CLI...');
  const version = execSync('firebase --version', { encoding: 'utf-8' });
  console.log(`✅ Firebase CLI instalado: ${version.trim()}`);
} catch (error) {
  console.error('❌ Firebase CLI no está instalado.');
  console.error('');
  console.error('Por favor instala Firebase CLI:');
  console.error('  npm install -g firebase-tools');
  console.error('');
  console.error('Luego ejecuta:');
  console.error('  firebase login');
  console.error('');
  process.exit(1);
}

console.log('');
console.log('⚠️  ADVERTENCIA: Esta acción eliminará TODOS los videos de contest_entries');
console.log('⚠️  Presiona Ctrl+C para cancelar en los próximos 5 segundos...');
console.log('');

// Esperar 5 segundos
setTimeout(() => {
  console.log('🗑️  Procediendo con la eliminación...');
  console.log('');
  
  try {
    // Comando para eliminar la colección usando Firebase CLI
    console.log('📝 Ejecutando comando de eliminación...');
    console.log('   firebase firestore:delete contest_entries --recursive --force');
    console.log('');
    
    const result = execSync(
      'firebase firestore:delete contest_entries --recursive --force',
      { encoding: 'utf-8', stdio: 'inherit' }
    );
    
    console.log('');
    console.log('✅ ===== LIMPIEZA COMPLETADA =====');
    console.log('✅ La colección contest_entries ha sido eliminada');
    console.log('');
    console.log('📱 Próximos pasos:');
    console.log('   1. Abre la app');
    console.log('   2. Verifica que no hay videos');
    console.log('   3. Sube un video nuevo');
    console.log('   4. Verifica que funciona correctamente');
    console.log('');
    
  } catch (error) {
    console.error('');
    console.error('❌ Error durante la eliminación');
    console.error('❌ Detalles:', error.message);
    console.error('');
    console.error('💡 Posibles soluciones:');
    console.error('   1. Ejecuta: firebase login');
    console.error('   2. Verifica que estás en el proyecto correcto: firebase use');
    console.error('   3. Intenta el método manual en Firebase Console');
    console.error('');
  }
}, 5000);
