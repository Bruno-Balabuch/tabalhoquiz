package com.example.trabalhoquiz.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.trabalhoquiz.data.local.model.QuestionEntity
import com.example.trabalhoquiz.data.repository.QuestionRepository
import com.example.trabalhoquiz.data.repository.ScoreRepository
import com.example.trabalhoquiz.data.repository.TriviaRepository
import com.example.trabalhoquiz.data.repository.UserRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

data class QuizUiState(
    val questions: List<QuestionEntity> = emptyList(),
    val currentIndex: Int = 0,
    val score: Int = 0,
    val isFinished: Boolean = false,
    val feedback: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val timeLeft: Int = 5
)

class QuizViewModel(
    private val localRepository: QuestionRepository,
    private val triviaRepository: TriviaRepository,
    private val scoreRepository: ScoreRepository,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState

    private var cachedTrivia: List<QuestionEntity>? = null
    private var timerJob: Job? = null


    fun loadQuestions(amount: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {

                val localQuestions = localRepository.getAllQuestionsFromFirestore().toMutableList()

                val finalList = mutableListOf<QuestionEntity>()
                finalList.addAll(localQuestions)

                if (finalList.size < amount) {
                    val needed = amount - finalList.size

                    val apiQuestions =
                        triviaRepository.fetchTriviaQuestions(needed)

                    finalList.addAll(apiQuestions)
                }

                while (finalList.size < amount) {
                    finalList.addAll(finalList)
                }

                val selectedQuestions = finalList.shuffled().take(amount)

                _uiState.value = QuizUiState(
                    questions = selectedQuestions,
                    isLoading = false,
                    timeLeft = 5
                )

                startTimer()

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Erro ao carregar perguntas"
                )
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            var time = 5
            while (time > 0) {
                delay(1000)
                time--
                _uiState.value = _uiState.value.copy(timeLeft = time)
            }
            handleTimeUp()
        }
    }

    private fun handleTimeUp() {
        val state = _uiState.value
        val nextIndex = state.currentIndex + 1
        if (nextIndex < state.questions.size) {
            _uiState.value = state.copy(
                currentIndex = nextIndex,
                feedback = "⏰ Tempo esgotado!",
                timeLeft = 5
            )
            startTimer()
        } else {
            _uiState.value = state.copy(isFinished = true, feedback = null)
        }
    }

    fun answerQuestion(selectedAnswer: String) {
        timerJob?.cancel()
        val state = _uiState.value
        val current = state.questions.getOrNull(state.currentIndex) ?: return

        val isCorrect = selectedAnswer == current.correctAnswer
        val newScore = if (isCorrect) state.score + 1 else state.score
        val feedback = if (isCorrect) "✅ Acertou!" else "❌ Errou!"

        viewModelScope.launch {
            _uiState.value = state.copy(feedback = feedback, score = newScore)
            delay(1000)

            val nextIndex = state.currentIndex + 1
            if (nextIndex < state.questions.size) {
                _uiState.value = _uiState.value.copy(
                    currentIndex = nextIndex,
                    feedback = null,
                    timeLeft = 5
                )
                startTimer()
            } else {
                _uiState.value = _uiState.value.copy(
                    isFinished = true,
                    feedback = null,
                    score = newScore
                )
            }
        }
    }

    fun finishQuiz() {
        val state = _uiState.value
        viewModelScope.launch {

            val playerName = userRepository.getLoggedUserName()

            scoreRepository.saveResultToFirestore(
                playerName = playerName,
                score = state.score
            )
        }
    }

    fun restartQuiz() {
        timerJob?.cancel()
        _uiState.value = QuizUiState()
    }
}
