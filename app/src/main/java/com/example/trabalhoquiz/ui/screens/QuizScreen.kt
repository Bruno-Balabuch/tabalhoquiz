package com.example.trabalhoquiz.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.trabalhoquiz.ui.quiz.QuizViewModel
import com.example.trabalhoquiz.data.local.model.QuestionEntity

@Composable
fun QuizScreen(viewModel: QuizViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedAmount by remember { mutableStateOf(5) }
    var hasStarted by rememberSaveable { mutableStateOf(false) }

    when {
        !hasStarted -> {
            QuizAmountSelector(
                selectedAmount = selectedAmount,
                onAmountChange = { selectedAmount = it },
                onStart = {
                    hasStarted = true
                    viewModel.loadQuestions(amount = selectedAmount)
                }
            )
        }

        uiState.isLoading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        uiState.error != null -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Erro: ${uiState.error}", color = MaterialTheme.colorScheme.error)
            }
        }

        uiState.isFinished -> {

            LaunchedEffect(Unit) {
                viewModel.finishQuiz()
            }

            QuizResultScreen(
                score = uiState.score,
                total = uiState.questions.size,
                onRestart = {
                    hasStarted = false
                    viewModel.restartQuiz()
                }
            )
        }

        else -> {
            val question = uiState.questions.getOrNull(uiState.currentIndex)
            if (question != null) {
                QuizQuestion(
                    question = question,
                    onAnswer = { answer -> viewModel.answerQuestion(answer) },
                    feedback = uiState.feedback,
                    timeLeft = uiState.timeLeft
                )
            }
        }
    }
}

@Composable
fun QuizAmountSelector(
    selectedAmount: Int,
    onAmountChange: (Int) -> Unit,
    onStart: () -> Unit
) {
    val amounts = listOf(5, 10, 15, 20)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Quantas perguntas você quer responder?", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(24.dp))

        amounts.forEach { amount ->
            Button(
                onClick = { onAmountChange(amount) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (amount == selectedAmount)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.secondaryContainer
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text("$amount perguntas")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onStart) {
            Text("Iniciar Quiz")
        }
    }
}

@Composable
fun QuizQuestion(
    question: QuestionEntity,
    onAnswer: (String) -> Unit,
    feedback: String?,
    timeLeft: Int
) {
    val options = remember(question) {
        (question.incorrectAnswers + question.correctAnswer).shuffled()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "⏱️ Tempo restante: $timeLeft s",
            style = MaterialTheme.typography.titleMedium,
            color = if (timeLeft <= 2) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(text = question.question, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(24.dp))

        options.forEach { option ->
            Button(
                onClick = { onAnswer(option) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Text(option)
            }
        }

        feedback?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(it, style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
fun QuizResultScreen(score: Int, total: Int, onRestart: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🏁 Fim do Quiz!", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(12.dp))
        Text("Você acertou $score de $total perguntas!", style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onRestart) {
            Text("Jogar novamente")
        }
    }
}
