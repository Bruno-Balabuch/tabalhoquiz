package com.example.trabalhoquiz.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "scores")
data class ScoreEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val playerName: String = "",
    val score: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)
