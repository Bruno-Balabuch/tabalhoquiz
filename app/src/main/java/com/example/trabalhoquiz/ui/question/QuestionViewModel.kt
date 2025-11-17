package com.example.trabalhoquiz.ui.question

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trabalhoquiz.data.local.model.QuestionEntity
import com.example.trabalhoquiz.data.repository.QuestionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class QuestionUiState(
    val isLoading: Boolean = false,
    val questions: List<QuestionEntity> = emptyList(),
    val error: String? = null
)

class QuestionViewModel(private val repository: QuestionRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(QuestionUiState(isLoading = true))
    val uiState: StateFlow<QuestionUiState> = _uiState.asStateFlow()

    init {
        loadQuestions()
    }

    fun loadQuestions() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val questions = repository.getAllQuestionsFromFirestore()
                _uiState.value = QuestionUiState(
                    isLoading = false,
                    questions = questions
                )
            } catch (e: Exception) {
                _uiState.value = QuestionUiState(
                    isLoading = false,
                    error = e.message ?: "Erro ao carregar perguntas"
                )
            }
        }
    }

    fun addQuestion(question: QuestionEntity) {
        viewModelScope.launch {
            try {
                repository.addQuestionToFirestore(question)
                val updatedList = _uiState.value.questions + question
                _uiState.value = _uiState.value.copy(questions = updatedList)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteQuestion(question: QuestionEntity) {
        viewModelScope.launch {
            try {
                repository.deleteQuestionFromFirestore(question.id)
                val updatedList = _uiState.value.questions.filterNot { it.id == question.id }
                _uiState.value = _uiState.value.copy(questions = updatedList)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}
