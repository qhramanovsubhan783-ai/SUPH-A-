package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Navy950
import com.example.viewmodel.ChatViewModel

enum class AppScreen {
    CHAT,
    SETTINGS
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Navy950
                ) {
                    SuphAiApp()
                }
            }
        }
    }
}

@Composable
fun SuphAiApp(
    chatViewModel: ChatViewModel = viewModel()
) {
    var currentScreen by remember { mutableStateOf(AppScreen.CHAT) }
    val currentSettings by chatViewModel.settings.collectAsState()

    when (currentScreen) {
        AppScreen.CHAT -> {
            ChatScreen(
                viewModel = chatViewModel,
                onNavigateToSettings = { currentScreen = AppScreen.SETTINGS }
            )
        }
        AppScreen.SETTINGS -> {
            SettingsScreen(
                currentSettings = currentSettings,
                onSaveSettings = { updatedSettings ->
                    chatViewModel.updateSettings(updatedSettings)
                },
                onClearAllHistory = {
                    chatViewModel.clearAllHistory()
                },
                onBack = { currentScreen = AppScreen.CHAT }
            )
        }
    }
}
