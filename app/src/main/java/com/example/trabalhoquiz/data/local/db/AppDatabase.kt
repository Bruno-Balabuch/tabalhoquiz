package com.example.trabalhoquiz.data.local.db

import androidx.room.Database
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
        const val DATABASE_NAME = "quiz_app_db"
    }

}