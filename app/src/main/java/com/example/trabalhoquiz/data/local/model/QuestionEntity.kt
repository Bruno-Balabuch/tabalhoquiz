package com.example.trabalhoquiz.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey
    var id: String = "",
    var category: String = "",
    var question: String = "",
    var correctAnswer: String = "",
    var incorrectAnswers: List<String> = emptyList(),
    var createdByAdmin: Boolean = false,
    var createdAt: Long = System.currentTimeMillis()
)
