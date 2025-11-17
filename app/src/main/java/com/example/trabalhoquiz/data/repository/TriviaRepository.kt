package com.example.trabalhoquiz.data.repository

import com.example.trabalhoquiz.data.local.model.QuestionEntity
import com.example.trabalhoquiz.data.remote.TriviaApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class TriviaRepository(private val api: TriviaApiService) {

    suspend fun fetchTriviaQuestions(amount: Int = 10): List<QuestionEntity> = withContext(Dispatchers.IO) {
        val response = api.getQuestions(amount)
        if (response.response_code == 0) {
            response.results.map { q ->
                QuestionEntity(
                    id = "", // API não fornece ID
                    category = q.category,
                    question = htmlDecode(q.question),
                    correctAnswer = htmlDecode(q.correct_answer),
                    incorrectAnswers = q.incorrect_answers.map { htmlDecode(it) },
                    createdByAdmin = false
                )
            }
        } else {
            emptyList()
        }
    }

    private fun htmlDecode(text: String): String {
        return android.text.Html.fromHtml(text, android.text.Html.FROM_HTML_MODE_LEGACY).toString()
    }
}