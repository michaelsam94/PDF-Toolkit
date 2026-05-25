package com.michael.pdftoolkit.data.local

import android.content.Context
import androidx.annotation.VisibleForTesting
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [RecentDocumentEntity::class, SavedSignatureEntity::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun recentDocumentDao(): RecentDocumentDao
    abstract fun savedSignatureDao(): SavedSignatureDao

    companion object {
        @VisibleForTesting
        internal var inMemoryForTests: AppDatabase? = null

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            inMemoryForTests?.let { return it }
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pdf_toolkit_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        @VisibleForTesting
        fun resetForTests() {
            inMemoryForTests?.close()
            inMemoryForTests = null
            INSTANCE?.close()
            INSTANCE = null
        }
    }
}
