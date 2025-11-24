package com.metu.hypematch

/**
 * Configuración de desarrollo
 * 
 * IMPORTANTE: Cambiar SKIP_EMAIL_VERIFICATION a false en producción
 */
object DevConfig {
    /**
     * Si es true, permite acceso sin verificar email (SOLO PARA DESARROLLO)
     * Cambiar a false antes de publicar la app
     */
    const val SKIP_EMAIL_VERIFICATION = false // ✅ Listo para producción
    
    /**
     * Mostrar logs detallados
     */
    const val VERBOSE_LOGS = true
}
