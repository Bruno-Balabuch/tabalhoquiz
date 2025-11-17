package com.example.trabalhoquiz.data.repository

import com.example.trabalhoquiz.data.local.dao.QuestionDAO
import com.example.trabalhoquiz.data.local.model.QuestionEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

class QuestionRepository(private val questionDao: QuestionDAO) {

    fun getAllQuestions(): Flow<List<QuestionEntity>> =
        questionDao.getAllQuestions().catch { e ->
            throw Exception("Erro ao carregar perguntas: ${e.message}")
        }

    suspend fun getQuestionById(id: String): QuestionEntity? = try {
        questionDao.getQuestionById(id)
    } catch (e: Exception) {
        throw Exception("Erro ao buscar pergunta: ${e.message}")
    }

    suspend fun insertQuestion(question: QuestionEntity) {
        try {
            questionDao.insertQuestion(question)
        } catch (e: Exception) {
            throw Exception("Erro ao inserir pergunta: ${e.message}")
        }
    }

    suspend fun updateQuestion(question: QuestionEntity) {
        try {
            questionDao.updateQuestion(question)
        } catch (e: Exception) {
            throw Exception("Erro ao atualizar pergunta: ${e.message}")
        }
    }

    suspend fun deleteQuestion(question: QuestionEntity) {
        try {
            questionDao.deleteQuestion(question)
        } catch (e: Exception) {
            throw Exception("Erro ao deletar pergunta: ${e.message}")
        }
    }

}