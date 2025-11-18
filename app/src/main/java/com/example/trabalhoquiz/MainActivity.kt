package com.example.trabalhoquiz

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.room.Room
import com.example.trabalhoquiz.data.local.db.AppDatabase
import com.example.trabalhoquiz.data.remote.NetworkModule
import com.example.trabalhoquiz.data.repository.QuestionRepository
import com.example.trabalhoquiz.data.repository.ScoreRepository
import com.example.trabalhoquiz.data.repository.TriviaRepository
import com.example.trabalhoquiz.data.repository.UserRepository
import com.example.trabalhoquiz.ui.admin.AdminViewModel
import com.example.trabalhoquiz.ui.quiz.QuizViewModel
import com.example.trabalhoquiz.ui.score.ScoreViewModel
import com.example.trabalhoquiz.ui.screens.AdminScreen
import com.example.trabalhoquiz.ui.screens.BottomNav
import com.example.trabalhoquiz.ui.screens.LoginScreen
import com.example.trabalhoquiz.ui.screens.QuizScreen
import com.example.trabalhoquiz.ui.screens.RankingScreen
import com.example.trabalhoquiz.ui.screens.RegisterScreen
import com.example.trabalhoquiz.ui.theme.TrabalhoquizTheme
import com.example.trabalhoquiz.ui.user.UserViewModel
import com.example.trabalhoquiz.ui.user.UserViewModelFactory

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        ).build()

        val userRepository = UserRepository(db.userDao())
        val userFactory = UserViewModelFactory(userRepository)
        val userViewModel: UserViewModel by viewModels { userFactory }

        val adminViewModel = AdminViewModel(QuestionRepository(db.questionDao()))
        val quizViewModel = QuizViewModel(
            localRepository = QuestionRepository(db.questionDao()),
            triviaRepository = TriviaRepository(NetworkModule.api),
            scoreRepository = ScoreRepository(db.scoreDao()),
            userRepository = UserRepository(db.userDao())
        )

        val rankingViewModel = ScoreViewModel(
            repository = ScoreRepository(db.scoreDao())
        )

        setContent {
            TrabalhoquizTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainApp(
                        userViewModel = userViewModel,
                        adminViewModel = adminViewModel,
                        quizViewModel = quizViewModel,
                        rankingViewModel = rankingViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun MainApp(
    userViewModel: UserViewModel,
    adminViewModel: AdminViewModel,
    quizViewModel: QuizViewModel,
    rankingViewModel: ScoreViewModel
) {
    var currentScreen by rememberSaveable { mutableStateOf("login") }
    var isAdmin by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        bottomBar = {
            if (currentScreen in listOf("quiz", "admin", "ranking")) {
                BottomNav(
                    onNavigate = { dest -> currentScreen = dest },
                    onLogout = {
                        userViewModel.logout()
                        currentScreen = "login"
                    }
                )
            }
        }
    ) { padding ->

        Box(modifier = Modifier.padding(padding)) {
            when (currentScreen) {

                "login" -> LoginScreen(
                    viewModel = userViewModel,
                    onLoginSuccess = {
                        isAdmin = it
                        currentScreen = if (it) "admin" else "quiz"
                    },
                    onNavigateToRegister = { currentScreen = "register" }
                )

                "register" -> RegisterScreen(
                    viewModel = userViewModel,
                    onRegisterSuccess = {
                        isAdmin = it
                        currentScreen = if (it) "admin" else "quiz"
                    }
                )

                "quiz" -> QuizScreen(viewModel = quizViewModel)

                "admin" -> AdminScreen(viewModel = adminViewModel)

                "ranking" -> RankingScreen(viewModel = rankingViewModel)
            }
        }
    }
}
