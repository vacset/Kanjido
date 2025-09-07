package me.seta.vacset.qrwari.data.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        EventEntity::class,
        ItemEntity::class,
        ParticipantEntity::class,
        EventParticipantCrossRef::class
    ],
    version = 2, // Incremented version to 2
    exportSchema = false // Recommended to set to true for production apps for schema migration history
)
@TypeConverters(StorageTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun eventDao(): EventDao // We'll define EventDao next
    // abstract fun participantDao(): ParticipantDao // For future direct participant operations

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "qrwari_database"
                )
                // Wipes and rebuilds instead of migrating if no Migration object.
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
