package com.metu.hypematch.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * 🚀 FASE 3: Room Database Principal
 * 
 * Base de datos local para caché de datos
 * 
 * Beneficios:
 * - Carga instantánea (< 50ms)
 * - Funciona offline
 * - Reduce llamadas a Firebase
 * - Mejor experiencia de usuario
 */
@Database(
    entities = [UserProfileEntity::class, SongEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun songDao(): SongDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                android.util.Log.d("AppDatabase", "🗄️ Creando base de datos...")
                
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "hypematch_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                
                INSTANCE = instance
                
                android.util.Log.d("AppDatabase", "✅ Base de datos creada")
                instance
            }
        }
        
        /**
         * Limpiar instancia (útil para testing)
         */
        fun clearInstance() {
            INSTANCE = null
        }
    }
}
