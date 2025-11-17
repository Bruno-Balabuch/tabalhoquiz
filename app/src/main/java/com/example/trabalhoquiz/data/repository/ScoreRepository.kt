package com.example.trabalhoquiz.data.repository

import com.example.trabalhoquiz.data.local.dao.ScoreDAO
import com.example.trabalhoquiz.data.local.model.ScoreEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

class ScoreRepository(private val scoreDao: ScoreDAO) {

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

}