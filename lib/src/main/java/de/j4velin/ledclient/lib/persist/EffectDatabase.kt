package de.j4velin.ledclient.lib.persist

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [PersistableEffect::class], version = 1, exportSchema = false)
internal abstract class EffectDatabase : RoomDatabase() {
    abstract fun getDao(): EffectDao

    companion object {
        @Volatile
        private var INSTANCE: EffectDatabase? = null

        internal fun getDatabase(context: Context): EffectDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    EffectDatabase::class.java,
                    "effect_database"
                ).build()
                INSTANCE = instance
                return instance
            }
        }
    }
}