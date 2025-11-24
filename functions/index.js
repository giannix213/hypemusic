const functions = require('firebase-functions');
const { RtcTokenBuilder, RtcRole } = require('agora-access-token');

// Configuración de Agora
const APP_ID = '72117baf2c874766b556e6f83ac9c58d';
const APP_CERTIFICATE = 'f907826ae8ff4c00b7057d15b6f2e628';

/**
 * Genera un token de Agora para un canal específico
 * 
 * Parámetros esperados:
 * - channelName: Nombre del canal
 * - role: 'publisher' o 'subscriber'
 * - uid: ID del usuario (0 para cualquier usuario)
 * 
 * Retorna:
 * - token: Token de Agora válido por 1 hora
 * - expiresAt: Timestamp de expiración
 */
exports.generateAgoraToken = functions.https.onCall((data, context) => {
  try {
    // Validar parámetros
    const channelName = data.channelName;
    const roleStr = data.role || 'subscriber';
    const uid = data.uid || 0;
    
    if (!channelName) {
      throw new functions.https.HttpsError(
        'invalid-argument',
        'El nombre del canal es requerido'
      );
    }
    
    // Determinar el rol
    const role = roleStr === 'publisher' ? RtcRole.PUBLISHER : RtcRole.SUBSCRIBER;
    
    // Configurar expiración según el rol
    // Publisher (emisor): 2 horas (7200s)
    // Subscriber (espectador): 1 hora (3600s)
    const expirationTimeInSeconds = roleStr === 'publisher' ? 7200 : 3600;
    const currentTimestamp = Math.floor(Date.now() / 1000);
    const privilegeExpiredTs = currentTimestamp + expirationTimeInSeconds;
    
    // Generar token
    const token = RtcTokenBuilder.buildTokenWithUid(
      APP_ID,
      APP_CERTIFICATE,
      channelName,
      uid,
      role,
      privilegeExpiredTs
    );
    
    console.log('✅ Token generado para canal:', channelName);
    console.log('   Rol:', roleStr);
    console.log('   UID:', uid);
    console.log('   Expira en:', new Date(privilegeExpiredTs * 1000).toISOString());
    
    return {
      token: token,
      expiresAt: privilegeExpiredTs,
      channelName: channelName,
      uid: uid
    };
    
  } catch (error) {
    console.error('❌ Error generando token:', error);
    throw new functions.https.HttpsError(
      'internal',
      'Error al generar el token de Agora',
      error.message
    );
  }
});

/**
 * Genera un token de Agora para un streamer (publisher)
 * Simplificado para uso directo desde la app
 */
exports.generateStreamerToken = functions.https.onCall((data, context) => {
  // Verificar que el usuario esté autenticado
  if (!context.auth) {
    throw new functions.https.HttpsError(
      'unauthenticated',
      'El usuario debe estar autenticado'
    );
  }
  
  const channelName = data.channelName;
  const uid = context.auth.uid.hashCode(); // Convertir UID de Firebase a número
  
  return exports.generateAgoraToken({
    channelName: channelName,
    role: 'publisher',
    uid: uid
  }, context);
});

/**
 * Genera un token de Agora para un espectador (subscriber)
 */
exports.generateViewerToken = functions.https.onCall((data, context) => {
  // Verificar que el usuario esté autenticado
  if (!context.auth) {
    throw new functions.https.HttpsError(
      'unauthenticated',
      'El usuario debe estar autenticado'
    );
  }
  
  const channelName = data.channelName;
  const uid = context.auth.uid.hashCode(); // Convertir UID de Firebase a número
  
  return exports.generateAgoraToken({
    channelName: channelName,
    role: 'subscriber',
    uid: uid
  }, context);
});

// Helper para convertir string a número
String.prototype.hashCode = function() {
  let hash = 0;
  for (let i = 0; i < this.length; i++) {
    const char = this.charCodeAt(i);
    hash = ((hash << 5) - hash) + char;
    hash = hash & hash; // Convert to 32bit integer
  }
  return Math.abs(hash);
};
