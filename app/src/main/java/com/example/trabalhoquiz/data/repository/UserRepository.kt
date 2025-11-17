package com.example.trabalhoquiz.data.repository

import com.example.trabalhoquiz.data.local.dao.UserDAO
import com.example.trabalhoquiz.data.local.model.UserEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.tasks.await

class UserRepository(private val userDao: UserDAO) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

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

    suspend fun login(email: String, password: String): Result<UserEntity?> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user

            if (firebaseUser == null) {
                return Result.failure(Exception("Usuário não encontrado no FirebaseAuth"))
            }

            val uid = firebaseUser.uid

            var user = userDao.getUserById(uid)

            if (user == null) {
                // 🔹 Se não estiver no banco local, busca no Firestore
                val doc = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .get()
                    .await()

                if (doc.exists()) {
                    user = UserEntity(
                        uid = uid,
                        name = doc.getString("name") ?: "",
                        email = doc.getString("email") ?: email,
                        isAdmin = doc.getBoolean("isAdmin") ?: false
                    )

                    insertUser(user)
                } else {
                    throw Exception("Usuário não encontrado no Firestore.")
                }
            }

            Result.success(user)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun register(email: String, password: String, name: String, isAdmin: Boolean): Result<FirebaseUser?> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user

            firebaseUser?.let {
                val newUser = UserEntity(
                    uid = it.uid,
                    name = name,
                    email = email,
                    isAdmin = isAdmin
                )
                insertUser(newUser)
            }

            Result.success(firebaseUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun logout() {
        auth.signOut()
    }

    fun getCurrentUser(): FirebaseUser? = auth.currentUser

    suspend fun getLoggedUserName(): String {
        val currentUser = auth.currentUser ?: return "Desconhecido"
        val uid = currentUser.uid

        val localUser = userDao.getUserById(uid)
        if (localUser != null) {
            return localUser.name
        }

        return try {
            val doc = com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users")
                .document(uid)
                .get()
                .await()

            doc.getString("name") ?: "Desconhecido"

        } catch (e: Exception) {
            "Desconhecido"
        }
    }
}