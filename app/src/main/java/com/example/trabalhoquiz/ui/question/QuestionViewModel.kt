package com.example.trabalhoquiz.ui.question

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trabalhoquiz.data.local.model.QuestionEntity
import com.example.trabalhoquiz.data.repository.QuestionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

data class QuestionUiState(
    val isLoading: Boolean = false,
    val questions: List<QuestionEntity> = emptyList(),
    val error: String? = null
)

class QuestionViewModel(private val repository: QuestionRepository) : ViewModel()  {

    private val _uiState = MutableStateFlow(QuestionUiState(isLoading = true))
    val uiState: StateFlow<QuestionUiState> = _uiState.asStateFlow()

    init {
        loadQuestions()
    }

    fun loadQuestions() {
        viewModelScope.launch {
            repository.getAllQuestions()
                .onStart { _uiState.value = _uiState.value.copy(isLoading = true) }
                .catch { e ->
                    _uiState.value = QuestionUiState(
                        isLoading = false,
                        error = e.message
                    )
                }
                .collect { list ->
                    _uiState.value = QuestionUiState(
                        isLoading = false,
                        questions = list
                    )
                }
        }
    }

    fun addQuestion(question: QuestionEntity) {
        viewModelScope.launch {
            try {
                repository.insertQuestion(question)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteQuestion(question: QuestionEntity) {
        viewModelScope.launch {
            try {
                repository.deleteQuestion(question)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

}