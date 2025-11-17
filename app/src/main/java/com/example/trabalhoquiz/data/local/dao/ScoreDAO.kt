package com.example.trabalhoquiz.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.trabalhoquiz.data.local.model.ScoreEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreDAO {

    @Query("SELECT * FROM scores ORDER BY score DESC, timestamp ASC")
    fun getAllScores(): Flow<List<ScoreEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScore(score: ScoreEntity)

    @Update
    suspend fun updateScore(score: ScoreEntity)

    @Delete
    suspend fun deleteScore(score: ScoreEntity)

    @Query("DELETE FROM scores")
    suspend fun clearAll()
}
