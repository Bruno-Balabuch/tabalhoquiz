package com.example.trabalhoquiz.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.trabalhoquiz.data.repository.QuestionRepository
import com.example.trabalhoquiz.data.repository.ScoreRepository
import com.example.trabalhoquiz.ui.question.QuestionViewModel
import com.example.trabalhoquiz.ui.score.ScoreViewModel

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(private val questionRepository: QuestionRepository, private val scoreRepository: ScoreRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(QuestionViewModel::class.java) ->
                QuestionViewModel(questionRepository) as T

            modelClass.isAssignableFrom(ScoreViewModel::class.java) ->
                ScoreViewModel(scoreRepository) as T

            else -> throw IllegalArgumentException("ViewModel desconhecido: ${modelClass.name}")
        }
    }

}