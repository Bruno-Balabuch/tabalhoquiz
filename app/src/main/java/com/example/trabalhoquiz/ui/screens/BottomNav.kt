package com.example.trabalhoquiz.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

@Composable
fun BottomNav(
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    var selected by rememberSaveable { mutableStateOf("quiz") }

    NavigationBar {

        NavigationBarItem(
            selected = selected == "quiz",
            onClick = {
                selected = "quiz"
                onNavigate("quiz")
            },
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Quiz") }
        )

        NavigationBarItem(
            selected = selected == "ranking",
            onClick = {
                selected = "ranking"
                onNavigate("ranking")
            },
            icon = { Icon(Icons.Default.Star, contentDescription = "Ranking") },
            label = { Text("Ranking") }
        )

        NavigationBarItem(
            selected = false,
            onClick = { onLogout() },
            icon = {
                Icon(Icons.Default.ExitToApp, contentDescription = "Logout")
            },
            label = { Text("Sair") }
        )
    }
}
