package com.example.trabalhoquiz.data.local.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "questions")
data class QuestionEntity(
    @PrimaryKey val id: String, // usar UUID ou id da API
    val category: String,
    val question: String,
    val correctAnswer: String,
    val incorrectAnswers: List<String>,
    val createdByAdmin: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)