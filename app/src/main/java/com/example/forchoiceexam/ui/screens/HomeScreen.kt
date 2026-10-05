package com.example.forchoiceexam.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forchoiceexam.data.model.ColumnMapping
import com.example.forchoiceexam.data.model.QuestionBank
import com.example.forchoiceexam.data.model.QuestionType
import com.example.forchoiceexam.ui.components.ImportPreviewDialog
import com.example.forchoiceexam.ui.viewmodel.ImportUiState
import com.example.forchoiceexam.ui.viewmodel.MainViewModel
import com.example.forchoiceexam.ui.viewmodel.PracticeMode
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToPractice: (bankId: Long, mode: PracticeMode, title: String, startIndex: Int) -> Unit
) {
    val banks by viewModel.allBanks.collectAsState()
    val importState by viewModel.importState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    var selectedUriForImport by remember { mutableStateOf<Uri?>(null) }
    var previewData by remember { mutableStateOf<Pair<List<List<String>>, ColumnMapping>?>(null) }
    var defaultBankName by remember { mutableStateOf("") }
    var isReadingPreview by remember { mutableStateOf(false) }

    var bankToDelete by remember { mutableStateOf<QuestionBank?>(null) }
    var resumeBankDialog by remember { mutableStateOf<QuestionBank?>(null) }

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedUriForImport = uri
            isReadingPreview = true
            coroutineScope.launch {
                val preview = viewModel.readExcelPreview(uri)
                val name = viewModel.getFileNameFromUri(uri)
                if (preview != null) {
                    previewData = preview
                    defaultBankName = name
                } else {
                    snackbarHostState.showSnackbar("无法读取文件预览数据")
                }
                isReadingPreview = false
            }
        }
    }

    LaunchedEffect(importState) {
        when (val state = importState) {
            is ImportUiState.Success -> {
                snackbarHostState.showSnackbar("成功导入题库: ${state.bankName}")
                viewModel.resetImportState()
            }
            is ImportUiState.Error -> {
                snackbarHostState.showSnackbar("导入失败: ${state.message}")
                viewModel.resetImportState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "选择题测试 APP",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            // Import Header Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "导入 Excel 题库",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "支持 .xlsx / .xls 格式，提供前 5 行数据预览与列自定义映射",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Button(
                        onClick = { filePickerLauncher.launch("*/*") },
                        enabled = importState !is ImportUiState.Loading && !isReadingPreview,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        if (importState is ImportUiState.Loading || isReadingPreview) {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .padding(end = 6.dp)
                                    .height(18.dp)
                                    .width(18.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Text("选择文件")
                    }
                }
            }

            Text(
                text = "我的题库 (${banks.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (banks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "暂无题库",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "请点击上方【选择文件】导入 Excel 题库",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(banks, key = { it.id }) { bank ->
                        val wrongCountFlow = remember(bank.id) { viewModel.getWrongCountFlow(bank.id) }
                        val wrongCount by wrongCountFlow.collectAsState(initial = 0)

                        BankCard(
                            bank = bank,
                            wrongCount = wrongCount,
                            onSequentialClick = {
                                if (bank.lastSequentialIndex > 0) {
                                    resumeBankDialog = bank
                                } else {
                                    onNavigateToPractice(bank.id, PracticeMode.SEQUENTIAL, bank.name, 0)
                                }
                            },
                            onRandomClick = {
                                onNavigateToPractice(bank.id, PracticeMode.RANDOM, bank.name, 0)
                            },
                            onErrorBookClick = {
                                onNavigateToPractice(bank.id, PracticeMode.ERROR_BOOK, "${bank.name} - 错题本", 0)
                            },
                            onStartByTypeClick = { type ->
                                coroutineScope.launch {
                                    val index = viewModel.getFirstQuestionIndexByType(bank.id, type)
                                    onNavigateToPractice(bank.id, PracticeMode.SEQUENTIAL, "${bank.name} - ${type.displayName}", index)
                                }
                            },
                            onStartByNumberClick = { targetNumber ->
                                val index = (targetNumber - 1).coerceIn(0, (bank.questionCount - 1).coerceAtLeast(0))
                                onNavigateToPractice(bank.id, PracticeMode.SEQUENTIAL, bank.name, index)
                            },
                            onDeleteClick = {
                                bankToDelete = bank
                            }
                        )
                    }
                }
            }
        }
    }

    // Resume Sequential Practice Dialog
    resumeBankDialog?.let { bank ->
        AlertDialog(
            onDismissRequest = { resumeBankDialog = null },
            title = { Text("顺序做题提示") },
            text = { Text("从第一题开始或从已顺序做题的下一题（第 ${bank.lastSequentialIndex + 1} 题）？") },
            confirmButton = {
                Button(
                    onClick = {
                        val index = bank.lastSequentialIndex
                        resumeBankDialog = null
                        onNavigateToPractice(bank.id, PracticeMode.SEQUENTIAL, bank.name, index)
                    }
                ) {
                    Text("最新一题")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        resumeBankDialog = null
                        onNavigateToPractice(bank.id, PracticeMode.SEQUENTIAL, bank.name, 0)
                    }
                ) {
                    Text("从头开始")
                }
            }
        )
    }

    // Excel Preview & Mapping Customization Dialog
    previewData?.let { (rows, mapping) ->
        val uri = selectedUriForImport
        if (uri != null) {
            ImportPreviewDialog(
                previewRows = rows,
                initialMapping = mapping,
                defaultBankName = defaultBankName,
                onConfirm = { bankName, finalMapping ->
                    viewModel.importBankWithMapping(uri, bankName, finalMapping)
                    previewData = null
                    selectedUriForImport = null
                },
                onDismissRequest = {
                    previewData = null
                    selectedUriForImport = null
                }
            )
        }
    }

    // Delete Bank Confirmation Dialog
    bankToDelete?.let { bank ->
        AlertDialog(
            onDismissRequest = { bankToDelete = null },
            title = { Text("删除题库") },
            text = { Text("确定要删除题库【${bank.name}】及其所有错题记录吗？") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteBank(bank.id)
                        bankToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { bankToDelete = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun BankCard(
    bank: QuestionBank,
    wrongCount: Int,
    onSequentialClick: () -> Unit,
    onRandomClick: () -> Unit,
    onErrorBookClick: () -> Unit,
    onStartByTypeClick: (QuestionType) -> Unit,
    onStartByNumberClick: (Int) -> Unit,
    onDeleteClick: () -> Unit
) {
    var customNumberText by remember { mutableStateOf("") }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = bank.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    modifier = Modifier.weight(1f)
                )

                TextButton(onClick = onDeleteClick) {
                    Text("删除", color = MaterialTheme.colorScheme.error, fontSize = 13.sp)
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "共 ${bank.questionCount} 题",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    color = if (wrongCount > 0) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "错题 $wrongCount 题",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (wrongCount > 0) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Main Action Mode Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onSequentialClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Text("顺序做题", fontSize = 13.sp)
                }

                FilledTonalButton(
                    onClick = onRandomClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Text("随机抽题", fontSize = 13.sp)
                }

                OutlinedButton(
                    onClick = onErrorBookClick,
                    enabled = wrongCount > 0,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    Text("错题本", fontSize = 13.sp)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // New Row 1: Start By Question Type
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "按题型：",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedButton(
                    onClick = { onStartByTypeClick(QuestionType.SINGLE_CHOICE) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    Text("单选题", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = { onStartByTypeClick(QuestionType.MULTIPLE_CHOICE) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    Text("多选题", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = { onStartByTypeClick(QuestionType.JUDGMENT) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp, vertical = 4.dp)
                ) {
                    Text("判断题", fontSize = 12.sp)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // New Row 2: Custom Number Start Input & Button
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "从 ",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                OutlinedTextField(
                    value = customNumberText,
                    onValueChange = { input ->
                        if (input.isEmpty() || input.all { it.isDigit() }) {
                            customNumberText = input
                        }
                    },
                    placeholder = { Text("1~${bank.questionCount}", fontSize = 12.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .width(110.dp)
                        .height(50.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
                )

                Text(
                    text = " 题 ",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.width(4.dp))

                val inputNum = customNumberText.toIntOrNull()
                val isValidNumber = inputNum != null && inputNum in 1..bank.questionCount

                Button(
                    onClick = {
                        if (inputNum != null) {
                            onStartByNumberClick(inputNum)
                        }
                    },
                    enabled = isValidNumber,
                    shape = RoundedCornerShape(6.dp),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("开始做题", fontSize = 13.sp)
                }
            }
        }
    }
}
