package com.example.trabalhoquiz.ui.user

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trabalhoquiz.data.local.model.UserEntity
import com.example.trabalhoquiz.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class UserUiState(
    val isLoading: Boolean = false,
    val currentUser: UserEntity? = null,
    val isAdmin: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false
)

class UserViewModel(private val repository: UserRepository) : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)

                val result = auth.signInWithEmailAndPassword(email, password).await()
                val uid = result.user?.uid ?: throw Exception("Usuário sem UID")

                val userDoc = firestore.collection("users").document(uid).get().await()
                val isAdmin = userDoc.getBoolean("admin") ?: false

                val user = UserEntity(
                    uid = uid,
                    name = userDoc.getString("name") ?: "",
                    email = userDoc.getString("email") ?: "",
                    isAdmin = isAdmin
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isLoggedIn = true,
                    currentUser = user,
                    isAdmin = isAdmin,
                    error = null
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Erro ao fazer login: ${e.message}"
                )
            }
        }
    }

    fun register(name: String, email: String, password: String, isAdmin: Boolean = false) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                val user = UserEntity(uid, name, email, isAdmin)

                firestore.collection("users").document(uid)
                    .set(user)
                    .addOnSuccessListener {
                        saveUserLocally(user)
                    }
                    .addOnFailureListener { e ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Erro ao salvar no Firestore: ${e.message}"
                        )
                    }
            }
            .addOnFailureListener { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Erro ao registrar usuário: ${e.message}"
                )
            }
    }

    private fun syncUserWithFirebase(uid: String) {
        firestore.collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val user = UserEntity(
                        uid = uid,
                        name = doc.getString("name") ?: "",
                        email = doc.getString("email") ?: "",
                        isAdmin = doc.getBoolean("isAdmin") ?: false
                    )
                    saveUserLocally(user)
                } else {
                    val firebaseUser = auth.currentUser
                    val newUser = UserEntity(
                        uid = uid,
                        name = firebaseUser?.displayName ?: "Usuário",
                        email = firebaseUser?.email ?: "",
                        isAdmin = false
                    )

                    firestore.collection("users").document(uid)
                        .set(newUser)
                        .addOnSuccessListener {
                            saveUserLocally(newUser)
                        }
                        .addOnFailureListener { e ->
                            _uiState.value = _uiState.value.copy(
                                isLoading = false,
                                error = "Erro ao criar usuário no Firestore: ${e.message}"
                            )
                        }
                }
            }
            .addOnFailureListener { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Erro ao buscar dados do usuário: ${e.message}"
                )
            }
    }

    private fun saveUserLocally(user: UserEntity) {
        viewModelScope.launch {
            try {
                repository.insertUser(user)
                _uiState.value = UserUiState(
                    isLoading = false,
                    currentUser = user,
                    isAdmin = user.isAdmin,
                    isLoggedIn = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Erro ao salvar localmente: ${e.message}"
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            val currentUser = auth.currentUser
            auth.signOut()

            currentUser?.uid?.let { uid ->
                val user = repository.getUserById(uid)
                if (user != null) {
                    repository.deleteUser(user)
                }
            }

            _uiState.value = UserUiState(isLoggedIn = false)
        }
    }


    fun checkSession() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            syncUserWithFirebase(currentUser.uid)
        }
    }
}