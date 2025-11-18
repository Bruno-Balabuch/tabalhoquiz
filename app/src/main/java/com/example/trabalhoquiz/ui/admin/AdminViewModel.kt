package com.example.trabalhoquiz.ui.admin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trabalhoquiz.data.local.model.QuestionEntity
import com.example.trabalhoquiz.data.repository.QuestionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.UUID

data class AdminUiState(
    val questions: List<QuestionEntity> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

class AdminViewModel(
    private val repository: QuestionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState

    init {
        loadQuestions()
    }

    fun loadQuestions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                repository.getAllQuestionsFromFirestore()

                repository.getAllQuestionsLocal().collectLatest { localQuestions ->
                    _uiState.value = _uiState.value.copy(
                        questions = localQuestions,
                        isLoading = false
                    )
                }

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Erro ao carregar perguntas"
                )
            }
        }
    }

    fun createQuestion(
        category: String,
        question: String,
        correctAnswer: String,
        incorrectAnswers: List<String>,
        createdByAdmin: Boolean
    ) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null, successMessage = null)
            try {
                val newQuestion = QuestionEntity(
                    id = UUID.randomUUID().toString(),
                    category = category,
                    question = question,
                    correctAnswer = correctAnswer,
                    incorrectAnswers = incorrectAnswers,
                    createdByAdmin = createdByAdmin
                )

                repository.addQuestionToFirestore(newQuestion)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Pergunta adicionada com sucesso!"
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Erro ao adicionar pergunta"
                )
            }
        }
    }

    fun deleteQuestion(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                repository.deleteQuestionFromFirestore(id)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "Pergunta removida com sucesso!"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Erro ao deletar pergunta"
                )
            }
        }
    }

    fun updateQuestion(updatedQuestion: QuestionEntity) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                repository.updateQuestionLocal(updatedQuestion) // local
                repository.addQuestionToFirestore(updatedQuestion) // regrava no Firestore
                val updatedList = _uiState.value.questions.map {
                    if (it.id == updatedQuestion.id) updatedQuestion else it
                }
                _uiState.value = _uiState.value.copy(
                    questions = updatedList,
                    isLoading = false,
                    successMessage = "Pergunta atualizada com sucesso!"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Erro ao atualizar pergunta"
                )
            }
        }
    }
}
