package com.example.trabalhoquiz.data.repository

import com.example.trabalhoquiz.data.local.dao.QuestionDAO
import com.example.trabalhoquiz.data.local.dao.ScoreDAO
import com.example.trabalhoquiz.data.local.model.QuestionEntity
import com.example.trabalhoquiz.data.local.model.ScoreEntity
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.tasks.await

data class PlayerResult(
    val playerName: String = "",
    val score: Int = 0,
    val total: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

class QuestionRepository(private val questionDao: QuestionDAO) {

    private val firestore = FirebaseFirestore.getInstance()
    private val questionsCollection = firestore.collection("questions")

    // ======= 🔹 LOCAL (ROOM) =======

    fun getAllQuestionsLocal(): Flow<List<QuestionEntity>> =
        questionDao.getAllQuestions().catch { e ->
            throw Exception("Erro ao carregar perguntas locais: ${e.message}")
        }

    suspend fun getQuestionById(id: String): QuestionEntity? = try {
        questionDao.getQuestionById(id)
    } catch (e: Exception) {
        throw Exception("Erro ao buscar pergunta local: ${e.message}")
    }

    suspend fun insertQuestionLocal(question: QuestionEntity) {
        try {
            questionDao.insertQuestion(question)
        } catch (e: Exception) {
            throw Exception("Erro ao inserir pergunta local: ${e.message}")
        }
    }

    suspend fun updateQuestionLocal(question: QuestionEntity) {
        try {
            questionDao.updateQuestion(question)
        } catch (e: Exception) {
            throw Exception("Erro ao atualizar pergunta local: ${e.message}")
        }
    }

    suspend fun deleteQuestionLocal(question: QuestionEntity) {
        try {
            questionDao.deleteQuestion(question)
        } catch (e: Exception) {
            throw Exception("Erro ao deletar pergunta local: ${e.message}")
        }
    }

    // ======= 🔹 FIRESTORE (NUVEM) =======

    suspend fun addQuestionToFirestore(question: QuestionEntity) {
        try {
            val docRef = if (question.id.isEmpty()) {
                questionsCollection.document()
            } else {
                questionsCollection.document(question.id)
            }
            val newQuestion = question.copy(id = docRef.id)
            docRef.set(newQuestion).await()
            // salva localmente também
            insertQuestionLocal(newQuestion)
        } catch (e: Exception) {
            throw Exception("Erro ao adicionar pergunta no Firestore: ${e.message}")
        }
    }

    suspend fun getAllQuestionsFromFirestore(): List<QuestionEntity> {
        return try {
            val snapshot = questionsCollection.get().await()
            val questions = snapshot.documents.mapNotNull { doc ->
                val q = doc.toObject(QuestionEntity::class.java)
                q?.copy(id = doc.id)
            }
            // limpa/sincroniza local
            questionDao.clearAll()
            questions.forEach { insertQuestionLocal(it) }
            questions
        } catch (e: Exception) {
            throw Exception("Erro ao buscar perguntas do Firestore: ${e.message}")
        }
    }

    suspend fun deleteQuestionFromFirestore(id: String) {
        try {
            questionsCollection.document(id).delete().await()
            val question = questionDao.getQuestionById(id)
            if (question != null) deleteQuestionLocal(question)
        } catch (e: Exception) {
            throw Exception("Erro ao deletar pergunta do Firestore: ${e.message}")
        }
    }


}