package com.nihoru.vuzify.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [LessonEntity::class], version = 1, exportSchema = false)
abstract class VuzifyDatabase : RoomDatabase() {

    abstract fun lessonDao(): LessonDao

    companion object {
        @Volatile
        private var INSTANCE: VuzifyDatabase? = null

        fun getDatabase(context: Context): VuzifyDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VuzifyDatabase::class.java,
                    "vuzify_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
