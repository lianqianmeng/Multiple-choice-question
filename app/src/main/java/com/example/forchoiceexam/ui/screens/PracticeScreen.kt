package com.example.forchoiceexam.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forchoiceexam.data.model.QuestionType
import com.example.forchoiceexam.ui.components.QuestionCard
import com.example.forchoiceexam.ui.components.QuestionSheetDialog
import com.example.forchoiceexam.ui.viewmodel.PracticeMode
import com.example.forchoiceexam.ui.viewmodel.PracticeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PracticeScreen(
    viewModel: PracticeViewModel,
    bankId: Long,
    mode: PracticeMode,
    title: String,
    startIndex: Int = 0,
    onBackClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showSheetDialog by remember { mutableStateOf(false) }

    LaunchedEffect(bankId, mode, startIndex) {
        viewModel.loadQuestions(bankId, mode, title, startIndex)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = uiState.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (uiState.questions.isNotEmpty()) {
                            Text(
                                text = "第 ${uiState.currentIndex + 1} / ${uiState.questions.size} 题",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    TextButton(onClick = onBackClick) {
                        Text("返回", fontSize = 16.sp)
                    }
                },
                actions = {
                    if (uiState.questions.isNotEmpty()) {
                        TextButton(onClick = { showSheetDialog = true }) {
                            Text("答题卡", fontSize = 15.sp)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        },
        bottomBar = {
            if (uiState.questions.isNotEmpty()) {
                Surface(
                    tonalElevation = 6.dp,
                    shadowElevation = 8.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { viewModel.previousQuestion() },
                            enabled = uiState.currentIndex > 0,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("上一题")
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        if (uiState.mode == PracticeMode.ERROR_BOOK) {
                            TextButton(
                                onClick = { viewModel.removeCurrentQuestionFromWrongBook() },
                                enabled = uiState.currentQuestion != null
                            ) {
                                Text("标记已掌握", color = MaterialTheme.colorScheme.primary)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        val current = uiState.currentQuestion
                        if (current != null && current.question.type == QuestionType.MULTIPLE_CHOICE && !current.isSubmitted) {
                            Button(
                                onClick = { viewModel.submitAnswer() },
                                enabled = current.selectedOptions.isNotEmpty(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("提交答案")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        Button(
                            onClick = { viewModel.nextQuestion() },
                            enabled = uiState.currentIndex < uiState.questions.lastIndex,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("下一题")
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else if (uiState.questions.isEmpty()) {
                Column(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (uiState.mode == PracticeMode.ERROR_BOOK) "太棒了，当前没有错题！" else "题库中暂无题目",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = onBackClick) {
                        Text("返回首页")
                    }
                }
            } else {
                val currentQuestion = uiState.currentQuestion
                if (currentQuestion != null) {
                    QuestionCard(
                        questionStatus = currentQuestion,
                        onOptionSelected = { option ->
                            viewModel.selectOption(option)
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }

    if (showSheetDialog && uiState.questions.isNotEmpty()) {
        QuestionSheetDialog(
            questions = uiState.questions,
            currentIndex = uiState.currentIndex,
            onSelectQuestion = { index ->
                viewModel.jumpToQuestion(index)
            },
            onDismissRequest = {
                showSheetDialog = false
            }
        )
    }
}
