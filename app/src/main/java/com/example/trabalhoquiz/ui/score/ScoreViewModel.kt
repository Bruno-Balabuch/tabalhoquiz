package com.example.trabalhoquiz.ui.score

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trabalhoquiz.data.local.model.ScoreEntity
import com.example.trabalhoquiz.data.repository.ScoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

data class ScoreUiState(
    val isLoading: Boolean = false,
    val scores: List<ScoreEntity> = emptyList(),
    val error: String? = null
)

class ScoreViewModel(private val repository: ScoreRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ScoreUiState(isLoading = true))
    val uiState: StateFlow<ScoreUiState> = _uiState.asStateFlow()

    init {
        loadScores()
    }

    fun loadScores() {
        viewModelScope.launch {
            repository.getAllScores()
                .onStart { _uiState.value = _uiState.value.copy(isLoading = true) }
                .catch { e ->
                    _uiState.value = ScoreUiState(isLoading = false, error = e.message)
                }
                .collect { list ->
                    _uiState.value = ScoreUiState(isLoading = false, scores = list)
                }
        }
    }

    fun addScore(score: ScoreEntity) {
        viewModelScope.launch {
            try {
                repository.insertScore(score)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}