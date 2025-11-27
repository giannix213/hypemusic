// Script para limpiar todos los videos de concursos de Firestore
// Ejecutar con: node limpiar-videos-concursos.js

const admin = require('firebase-admin');
const serviceAccount = require('./functions/serviceAccountKey.json');

// Inicializar Firebase Admin
admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

async function limpiarVideosConCursos() {
  console.log('🧹 ===== INICIANDO LIMPIEZA DE VIDEOS DE CONCURSOS =====');
  console.log('');
  
  try {
    // 1. Obtener todos los documentos de contest_entries
    console.log('📋 Paso 1: Obteniendo lista de videos...');
    const snapshot = await db.collection('contest_entries').get();
    
    if (snapshot.empty) {
      console.log('✅ No hay videos para eliminar. La colección ya está vacía.');
      return;
    }
    
    console.log(`📊 Total de videos encontrados: ${snapshot.size}`);
    console.log('');
    
    // 2. Mostrar información de los videos antes de eliminar
    console.log('📝 Videos que serán eliminados:');
    console.log('─'.repeat(80));
    
    snapshot.forEach((doc, index) => {
      const data = doc.data();
      console.log(`${index + 1}. ID: ${doc.id}`);
      console.log(`   👤 Usuario: ${data.username || 'N/A'} (${data.userId || 'N/A'})`);
      console.log(`   📝 Título: ${data.title || 'N/A'}`);
      console.log(`   🏆 Concurso: ${data.contestId || 'N/A'}`);
      console.log(`   🎬 Video URL: ${data.videoUrl ? data.videoUrl.substring(0, 50) + '...' : 'N/A'}`);
      console.log(`   ❤️ Likes: ${data.likes || 0} | 👁️ Views: ${data.views || 0}`);
      console.log(`   📅 Fecha: ${data.timestamp ? new Date(data.timestamp).toLocaleString() : 'N/A'}`);
      console.log('');
    });
    
    console.log('─'.repeat(80));
    console.log('');
    
    // 3. Confirmar eliminación
    console.log('⚠️  ADVERTENCIA: Esta acción eliminará TODOS los videos de concursos.');
    console.log('⚠️  Los archivos de video en Storage NO serán eliminados (solo los registros de Firestore).');
    console.log('');
    console.log('🔄 Procediendo con la eliminación en 3 segundos...');
    
    await new Promise(resolve => setTimeout(resolve, 3000));
    
    // 4. Eliminar todos los documentos
    console.log('');
    console.log('🗑️  Eliminando videos...');
    
    const batch = db.batch();
    let deleteCount = 0;
    
    snapshot.forEach((doc) => {
      batch.delete(doc.ref);
      deleteCount++;
    });
    
    await batch.commit();
    
    console.log('');
    console.log('✅ ===== LIMPIEZA COMPLETADA =====');
    console.log(`✅ Videos eliminados: ${deleteCount}`);
    console.log('✅ La colección contest_entries está ahora vacía');
    console.log('');
    console.log('📱 Ahora puedes:');
    console.log('   1. Abrir la app');
    console.log('   2. Ir al catálogo de concursos');
    console.log('   3. Grabar o subir nuevos videos');
    console.log('   4. Verificar que el carrusel funciona correctamente');
    console.log('');
    console.log('💡 Nota: Los archivos de video en Firebase Storage siguen ahí.');
    console.log('   Si quieres eliminarlos también, usa la consola de Firebase.');
    
  } catch (error) {
    console.error('');
    console.error('❌ ===== ERROR EN LA LIMPIEZA =====');
    console.error('❌ Mensaje:', error.message);
    console.error('❌ Detalles:', error);
    console.error('');
    console.error('💡 Posibles causas:');
    console.error('   - serviceAccountKey.json no encontrado');
    console.error('   - Permisos insuficientes');
    console.error('   - Conexión a Firebase fallida');
  } finally {
    process.exit();
  }
}

// Ejecutar limpieza
limpiarVideosConCursos();
