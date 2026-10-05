package com.example.forchoiceexam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.forchoiceexam.ui.screens.HomeScreen
import com.example.forchoiceexam.ui.screens.PracticeScreen
import com.example.forchoiceexam.ui.theme.ForChoiceExamTheme
import com.example.forchoiceexam.ui.viewmodel.MainViewModel
import com.example.forchoiceexam.ui.viewmodel.PracticeMode
import com.example.forchoiceexam.ui.viewmodel.PracticeViewModel

sealed interface Screen {
    object Home : Screen
    data class Practice(
        val bankId: Long,
        val mode: PracticeMode,
        val title: String,
        val startIndex: Int = 0
    ) : Screen
}

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()
    private val practiceViewModel: PracticeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ForChoiceExamTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }

                    when (val screen = currentScreen) {
                        is Screen.Home -> {
                            HomeScreen(
                                viewModel = mainViewModel,
                                onNavigateToPractice = { bankId, mode, title, startIndex ->
                                    currentScreen = Screen.Practice(bankId, mode, title, startIndex)
                                }
                            )
                        }
                        is Screen.Practice -> {
                            PracticeScreen(
                                viewModel = practiceViewModel,
                                bankId = screen.bankId,
                                mode = screen.mode,
                                title = screen.title,
                                startIndex = screen.startIndex,
                                onBackClick = {
                                    currentScreen = Screen.Home
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
