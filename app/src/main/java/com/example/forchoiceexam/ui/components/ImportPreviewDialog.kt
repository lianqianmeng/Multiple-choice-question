package com.example.forchoiceexam.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forchoiceexam.data.model.ColumnMapping

@Composable
fun ImportPreviewDialog(
    previewRows: List<List<String>>,
    initialMapping: ColumnMapping,
    defaultBankName: String,
    onConfirm: (bankName: String, mapping: ColumnMapping) -> Unit,
    onDismissRequest: () -> Unit
) {
    var bankName by remember { mutableStateOf(defaultBankName) }
    var skipFirstRow by remember { mutableStateOf(initialMapping.skipFirstRow) }

    // Column max count
    val maxCols = remember(previewRows) {
        previewRows.maxOfOrNull { it.size }?.coerceAtLeast(7) ?: 9
    }

    var questionCol by remember { mutableIntStateOf(initialMapping.questionCol) }
    var optionACol by remember { mutableIntStateOf(initialMapping.optionACol) }
    var optionBCol by remember { mutableIntStateOf(initialMapping.optionBCol) }
    var optionCCol by remember { mutableIntStateOf(initialMapping.optionCCol) }
    var optionDCol by remember { mutableIntStateOf(initialMapping.optionDCol) }
    var answerCol by remember { mutableIntStateOf(initialMapping.answerCol) }
    var explanationCol by remember { mutableIntStateOf(initialMapping.explanationCol) }
    var typeCol by remember { mutableStateOf(initialMapping.typeCol) }

    val verticalScrollState = rememberScrollState()
    val tableHorizontalScrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = {
            Text("导入预览与列映射", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(verticalScrollState)
            ) {
                // Bank Name Input
                OutlinedTextField(
                    value = bankName,
                    onValueChange = { bankName = it },
                    label = { Text("题库名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Skip 1st row toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = skipFirstRow,
                        onCheckedChange = { skipFirstRow = it }
                    )
                    Text("忽略第 1 行（表头）", fontSize = 14.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Section Title
                Text(
                    text = "文件前 5 行预览（左右滑动查看所有列）",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Preview Table Container
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp)),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    Row(
                        modifier = Modifier
                            .padding(8.dp)
                            .horizontalScroll(tableHorizontalScrollState)
                    ) {
                        for (colIndex in 0 until maxCols) {
                            Column(
                                modifier = Modifier
                                    .width(130.dp)
                                    .padding(horizontal = 4.dp)
                            ) {
                                // Column Header Badge
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(vertical = 4.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "列 ${colIndex + 1}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                // Cells in this column
                                previewRows.take(5).forEachIndexed { rowIndex, rowData ->
                                    val cellText = rowData.getOrNull(colIndex) ?: ""
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(
                                                if (rowIndex == 0 && skipFirstRow)
                                                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                                            )
                                            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(4.dp))
                                            .padding(4.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Text(
                                            text = cellText.ifBlank { "-" },
                                            fontSize = 11.sp,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis,
                                            color = if (cellText.isBlank()) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Mapping Customization Section
                Text(
                    text = "自定义字段对应列：",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    ColumnSelectorRow(label = "题目/题干", selectedCol = questionCol, maxCols = maxCols) { questionCol = it }
                    ColumnSelectorRow(label = "选项 A", selectedCol = optionACol, maxCols = maxCols) { optionACol = it }
                    ColumnSelectorRow(label = "选项 B", selectedCol = optionBCol, maxCols = maxCols) { optionBCol = it }
                    ColumnSelectorRow(label = "选项 C", selectedCol = optionCCol, maxCols = maxCols) { optionCCol = it }
                    ColumnSelectorRow(label = "选项 D", selectedCol = optionDCol, maxCols = maxCols) { optionDCol = it }
                    ColumnSelectorRow(label = "参考答案", selectedCol = answerCol, maxCols = maxCols) { answerCol = it }
                    ColumnSelectorRow(
                        label = "解析",
                        selectedCol = explanationCol,
                        maxCols = maxCols,
                        allowNone = true
                    ) { explanationCol = it }
                    ColumnSelectorRow(
                        label = "题型",
                        selectedCol = typeCol ?: -1,
                        maxCols = maxCols,
                        allowNone = true,
                        noneLabel = "自动检测"
                    ) { typeCol = if (it == -1) null else it }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val mapping = ColumnMapping(
                        questionCol = questionCol,
                        optionACol = optionACol,
                        optionBCol = optionBCol,
                        optionCCol = optionCCol,
                        optionDCol = optionDCol,
                        answerCol = answerCol,
                        explanationCol = explanationCol,
                        typeCol = typeCol,
                        skipFirstRow = skipFirstRow
                    )
                    onConfirm(bankName.ifBlank { defaultBankName }, mapping)
                }
            ) {
                Text("确认导入")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismissRequest) {
                Text("取消")
            }
        }
    )
}

@Composable
private fun ColumnSelectorRow(
    label: String,
    selectedCol: Int,
    maxCols: Int,
    allowNone: Boolean = false,
    noneLabel: String = "无",
    onColSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.Medium)

        Box {
            val displayValue = when {
                selectedCol < 0 -> noneLabel
                else -> "列 ${selectedCol + 1}"
            }

            OutlinedButton(
                onClick = { expanded = true },
                shape = RoundedCornerShape(6.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 2.dp)
            ) {
                Text(text = displayValue, fontSize = 12.sp)
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                if (allowNone) {
                    DropdownMenuItem(
                        text = { Text(noneLabel, fontSize = 13.sp) },
                        onClick = {
                            onColSelected(-1)
                            expanded = false
                        }
                    )
                }
                for (c in 0 until maxCols) {
                    DropdownMenuItem(
                        text = { Text("列 ${c + 1}", fontSize = 13.sp) },
                        onClick = {
                            onColSelected(c)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}
