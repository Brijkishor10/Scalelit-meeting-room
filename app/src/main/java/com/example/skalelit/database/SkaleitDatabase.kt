package com.example.skalelit.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [UserEntity::class, BookingEntity::class, FavoriteEntity::class], version = 3)
abstract class SkaleitDatabase : RoomDatabase() {
    abstract fun dao(): SkaleitDao

    companion object {
        @Volatile
        private var INSTANCE: SkaleitDatabase? = null

        fun getDatabase(context: Context): SkaleitDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SkaleitDatabase::class.java,
                    "skaleit_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}