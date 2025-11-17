package com.example.trabalhoquiz.data.repository

import com.example.trabalhoquiz.data.local.dao.ScoreDAO
import com.example.trabalhoquiz.data.local.model.ScoreEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.tasks.await

class ScoreRepository(private val scoreDao: ScoreDAO) {

    private val firestore = FirebaseFirestore.getInstance()
    private val scoresCollection = firestore.collection("scores") // coleção correta

    // ============================
    // LOCAL (ROOM)
    // ============================

    fun getAllScores(): Flow<List<ScoreEntity>> =
        scoreDao.getAllScores().catch { e ->
            throw Exception("Erro ao carregar pontuações: ${e.message}")
        }

    suspend fun insertScore(score: ScoreEntity) {
        try {
            scoreDao.insertScore(score)
        } catch (e: Exception) {
            throw Exception("Erro ao inserir pontuação: ${e.message}")
        }
    }

    suspend fun updateScore(score: ScoreEntity) {
        try {
            scoreDao.updateScore(score)
        } catch (e: Exception) {
            throw Exception("Erro ao atualizar pontuação: ${e.message}")
        }
    }

    suspend fun deleteScore(score: ScoreEntity) {
        try {
            scoreDao.deleteScore(score)
        } catch (e: Exception) {
            throw Exception("Erro ao deletar pontuação: ${e.message}")
        }
    }

    // ============================
    // FIRESTORE
    // ============================

    suspend fun saveResultToFirestore(playerName: String, score: Int) {
        try {
            val doc = scoresCollection.document()

            val data = mapOf(
                "playerName" to playerName,
                "score" to score,
                "timestamp" to System.currentTimeMillis()
            )

            // salvar no Firestore
            doc.set(data).await()

            // salvar no Room (ID autogerado aqui)
            scoreDao.insertScore(
                ScoreEntity(
                    playerName = playerName,
                    score = score
                )
            )

        } catch (e: Exception) {
            throw Exception("Erro ao salvar resultado no Firestore: ${e.message}")
        }
    }

    suspend fun syncScoresFromFirestore() {
        try {
            val snapshot = scoresCollection.get().await()

            val list = snapshot.documents.mapNotNull { doc ->
                doc.toObject(ScoreEntity::class.java)
            }

            scoreDao.clearAll()
            list.forEach { scoreDao.insertScore(it) }

        } catch (e: Exception) {
            throw Exception("Erro ao sincronizar ranking: ${e.message}")
        }
    }

    suspend fun getRanking(): List<ScoreEntity> {
        return try {
            val snapshot = scoresCollection
                .orderBy("score", Query.Direction.DESCENDING)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get()
                .await()

            snapshot.documents.mapNotNull { it.toObject(ScoreEntity::class.java) }

        } catch (e: Exception) {
            throw Exception("Erro ao carregar ranking: ${e.message}")
        }
    }
}
