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
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                syncUserWithFirebase(uid)
            }
            .addOnFailureListener { e ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Erro ao fazer login: ${e.message}"
                )
            }
    }

    fun registerUser(name: String, email: String, password: String, isAdmin: Boolean = false) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        auth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener { result ->
                val uid = result.user?.uid ?: return@addOnSuccessListener
                val user = UserEntity(uid, name, email, isAdmin)

                // Salvar no Firestore
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
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Usuário não encontrado no Firestore."
                    )
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
        auth.signOut()
        _uiState.value = UserUiState(isLoggedIn = false)
    }
    
    fun checkSession() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            syncUserWithFirebase(currentUser.uid)
        }
    }

}