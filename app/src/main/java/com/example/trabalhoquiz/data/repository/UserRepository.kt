package com.example.trabalhoquiz.data.repository

import com.example.trabalhoquiz.data.local.dao.UserDAO
import com.example.trabalhoquiz.data.local.model.UserEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

class UserRepository(private val userDao: UserDAO) {

    fun getAllUsers(): Flow<List<UserEntity>> =
        userDao.getAllUsers().catch { e ->
            throw Exception("Erro ao carregar usuários: ${e.message}")
        }

    suspend fun getUserById(uid: String): UserEntity? = try {
        userDao.getUserById(uid)
    } catch (e: Exception) {
        throw Exception("Erro ao buscar usuário: ${e.message}")
    }

    suspend fun insertUser(user: UserEntity) {
        try {
            userDao.insertUser(user)
        } catch (e: Exception) {
            throw Exception("Erro ao inserir usuário: ${e.message}")
        }
    }

    suspend fun updateUser(user: UserEntity) {
        try {
            userDao.updateUser(user)
        } catch (e: Exception) {
            throw Exception("Erro ao atualizar usuário: ${e.message}")
        }
    }

    suspend fun deleteUser(user: UserEntity) {
        try {
            userDao.deleteUser(user)
        } catch (e: Exception) {
            throw Exception("Erro ao deletar usuário: ${e.message}")
        }
    }

}