package com.example.trabalhoquiz.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.trabalhoquiz.data.local.dao.QuestionDAO
import com.example.trabalhoquiz.data.local.dao.ScoreDAO
import com.example.trabalhoquiz.data.local.dao.UserDAO
import com.example.trabalhoquiz.data.local.model.Converters
import com.example.trabalhoquiz.data.local.model.QuestionEntity
import com.example.trabalhoquiz.data.local.model.ScoreEntity
import com.example.trabalhoquiz.data.local.model.UserEntity

@Database(
    entities = [QuestionEntity::class, ScoreEntity::class, UserEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun questionDao(): QuestionDAO
    abstract fun scoreDao(): ScoreDAO
    abstract fun userDao(): UserDAO

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        const val DATABASE_NAME = "quiz_app_db"
    }
}
