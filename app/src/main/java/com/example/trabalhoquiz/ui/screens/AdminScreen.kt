package com.example.trabalhoquiz.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.trabalhoquiz.data.local.model.QuestionEntity
import com.example.trabalhoquiz.ui.admin.AdminViewModel

@Composable
fun AdminScreen(viewModel: AdminViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    var category by remember { mutableStateOf("") }
    var question by remember { mutableStateOf("") }
    var correctAnswer by remember { mutableStateOf("") }
    var incorrect1 by remember { mutableStateOf("") }
    var incorrect2 by remember { mutableStateOf("") }
    var incorrect3 by remember { mutableStateOf("") }
    var editingQuestion by remember { mutableStateOf<QuestionEntity?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadQuestions()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 100.dp)
    ) {
        item {
            Text("Painel do Administrador", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Gerencie e crie novas perguntas para o quiz.")
            Spacer(modifier = Modifier.height(24.dp))

            OutlinedTextField(
                value = category,
                onValueChange = { category = it },
                label = { Text("Categoria") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = question,
                onValueChange = { question = it },
                label = { Text("Pergunta") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = correctAnswer,
                onValueChange = { correctAnswer = it },
                label = { Text("Resposta Correta") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = incorrect1,
                onValueChange = { incorrect1 = it },
                label = { Text("Resposta Incorreta 1") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = incorrect2,
                onValueChange = { incorrect2 = it },
                label = { Text("Resposta Incorreta 2") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = incorrect3,
                onValueChange = { incorrect3 = it },
                label = { Text("Resposta Incorreta 3") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (category.isNotBlank() && question.isNotBlank() && correctAnswer.isNotBlank()) {
                        val incorrectAnswers = listOfNotNull(
                            incorrect1.takeIf { it.isNotBlank() },
                            incorrect2.takeIf { it.isNotBlank() },
                            incorrect3.takeIf { it.isNotBlank() }
                        )

                        if (editingQuestion == null) {
                            viewModel.createQuestion(
                                category = category,
                                question = question,
                                correctAnswer = correctAnswer,
                                incorrectAnswers = incorrectAnswers,
                                createdByAdmin = true
                            )
                        } else {
                            val updated = editingQuestion!!.copy(
                                category = category,
                                question = question,
                                correctAnswer = correctAnswer,
                                incorrectAnswers = incorrectAnswers
                            )
                            viewModel.updateQuestion(updated)
                            editingQuestion = null
                        }

                        category = ""
                        question = ""
                        correctAnswer = ""
                        incorrect1 = ""
                        incorrect2 = ""
                        incorrect3 = ""
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (editingQuestion == null) "Adicionar Pergunta" else "Salvar Alterações")
            }

            Spacer(modifier = Modifier.height(32.dp))
            Text("📋 Perguntas Cadastradas:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
        }

        // 🔹 Lista de perguntas
        if (uiState.isLoading) {
            item {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            }
        } else if (uiState.error != null) {
            item {
                Text(
                    "Erro: ${uiState.error}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            items(uiState.questions) { questionItem ->
                QuestionCard(
                    question = questionItem,
                    onDelete = { viewModel.deleteQuestion(questionItem.id) },
                    onEdit = { q ->
                        editingQuestion = q
                        category = q.category
                        question = q.question
                        correctAnswer = q.correctAnswer
                        incorrect1 = q.incorrectAnswers.getOrNull(0) ?: ""
                        incorrect2 = q.incorrectAnswers.getOrNull(1) ?: ""
                        incorrect3 = q.incorrectAnswers.getOrNull(2) ?: ""
                    }
                )
            }
        }
    }
}

@Composable
fun QuestionCard(
    question: QuestionEntity,
    onDelete: () -> Unit,
    onEdit: (QuestionEntity) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Categoria: ${question.category}", style = MaterialTheme.typography.labelMedium)
            Text(question.question, style = MaterialTheme.typography.bodyLarge)
            Text("Resposta correta: ${question.correctAnswer}", color = MaterialTheme.colorScheme.primary)

            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Button(
                    onClick = { onEdit(question) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("Editar", color = MaterialTheme.colorScheme.onSecondary)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = onDelete,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Excluir", color = MaterialTheme.colorScheme.onError)
                }
            }
        }
    }
}