package com.example.forchoiceexam.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.forchoiceexam.data.model.QuestionType
import com.example.forchoiceexam.data.model.QuestionWithStatus

@Composable
fun QuestionCard(
    questionStatus: QuestionWithStatus,
    onOptionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val question = questionStatus.question
    val scrollState = rememberScrollState()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .verticalScroll(scrollState)
        ) {
            // Type Badge & Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                val badgeBg = when (question.type) {
                    QuestionType.SINGLE_CHOICE -> MaterialTheme.colorScheme.primaryContainer
                    QuestionType.MULTIPLE_CHOICE -> MaterialTheme.colorScheme.secondaryContainer
                    QuestionType.JUDGMENT -> MaterialTheme.colorScheme.tertiaryContainer
                }
                val badgeText = when (question.type) {
                    QuestionType.SINGLE_CHOICE -> MaterialTheme.colorScheme.onPrimaryContainer
                    QuestionType.MULTIPLE_CHOICE -> MaterialTheme.colorScheme.onSecondaryContainer
                    QuestionType.JUDGMENT -> MaterialTheme.colorScheme.onTertiaryContainer
                }

                Surface(
                    color = badgeBg,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = question.type.displayName,
                        style = MaterialTheme.typography.labelMedium,
                        color = badgeText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                if (questionStatus.isSubmitted) {
                    val isCorrect = questionStatus.isCorrect == true
                    Surface(
                        color = if (isCorrect) Color(0xFFE8F5E9) else Color(0xFFFFEBEE),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isCorrect) "✓" else "✕",
                                color = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isCorrect) "回答正确" else "回答错误",
                                color = if (isCorrect) Color(0xFF2E7D32) else Color(0xFFC62828),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Question Text
            Text(
                text = question.questionText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                lineHeight = 26.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Options List
            val options = mutableListOf<Pair<String, String>>()
            if (question.optionA.isNotBlank()) options.add("A" to question.optionA)
            if (question.optionB.isNotBlank()) options.add("B" to question.optionB)
            if (question.optionC.isNotBlank()) options.add("C" to question.optionC)
            if (question.optionD.isNotBlank()) options.add("D" to question.optionD)

            options.forEach { (key, optionText) ->
                val isSelected = questionStatus.selectedOptions.contains(key)
                val isCorrectOption = question.normalizedAnswer.contains(key)

                val borderColor: Color
                val containerColor: Color
                val textColor: Color

                if (questionStatus.isSubmitted) {
                    if (isCorrectOption) {
                        // Green highlight for correct answer
                        borderColor = Color(0xFF2E7D32)
                        containerColor = Color(0xFFE8F5E9)
                        textColor = Color(0xFF1B5E20)
                    } else if (isSelected) {
                        // Red highlight for wrong selection
                        borderColor = Color(0xFFC62828)
                        containerColor = Color(0xFFFFEBEE)
                        textColor = Color(0xFFB71C1C)
                    } else {
                        borderColor = MaterialTheme.colorScheme.outlineVariant
                        containerColor = MaterialTheme.colorScheme.surface
                        textColor = MaterialTheme.colorScheme.onSurfaceVariant
                    }
                } else {
                    if (isSelected) {
                        borderColor = MaterialTheme.colorScheme.primary
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        textColor = MaterialTheme.colorScheme.primary
                    } else {
                        borderColor = MaterialTheme.colorScheme.outlineVariant
                        containerColor = MaterialTheme.colorScheme.surface
                        textColor = MaterialTheme.colorScheme.onSurface
                    }
                }

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(enabled = !questionStatus.isSubmitted) {
                            onOptionSelected(key)
                        },
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, borderColor),
                    color = containerColor
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Option Letter Tag
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(borderColor.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = key,
                                fontWeight = FontWeight.Bold,
                                color = textColor,
                                fontSize = 15.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Option Text
                        Text(
                            text = optionText,
                            style = MaterialTheme.typography.bodyLarge,
                            color = textColor,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Explanation Section
            AnimatedVisibility(visible = questionStatus.isSubmitted) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(16.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "正确答案: ",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 15.sp
                        )
                        Text(
                            text = question.normalizedAnswer,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF2E7D32),
                            fontSize = 16.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "解析:",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (question.explanation.isNotBlank()) question.explanation else "暂无详细解析",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
