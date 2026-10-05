package com.example.forchoiceexam.data.parser

import com.example.forchoiceexam.data.model.ColumnMapping
import com.example.forchoiceexam.data.model.Question
import com.example.forchoiceexam.data.model.QuestionType
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Sheet
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.InputStream

object ExcelParser {

    fun readPreviewRows(inputStream: InputStream, maxRows: Int = 5): List<List<String>> {
        val preview = mutableListOf<List<String>>()
        val workbook = WorkbookFactory.create(inputStream)
        val sheet = workbook.getSheetAt(0) ?: return emptyList()

        var rowCount = 0
        for (i in 0..sheet.lastRowNum) {
            val row = sheet.getRow(i) ?: continue
            if (isRowEmpty(row)) continue

            val rowData = mutableListOf<String>()
            val maxCols = (row.lastCellNum.toInt()).coerceAtLeast(7)
            for (c in 0 until maxCols) {
                rowData.add(getCellValueAsString(row.getCell(c)).trim())
            }
            preview.add(rowData)
            rowCount++
            if (rowCount >= maxRows) break
        }

        workbook.close()
        return preview
    }

    fun suggestDefaultMapping(previewRows: List<List<String>>): ColumnMapping {
        if (previewRows.isEmpty()) return ColumnMapping()

        val row0 = previewRows[0]
        val c0 = row0.getOrNull(0) ?: ""
        val c1 = row0.getOrNull(1) ?: ""
        val c2 = row0.getOrNull(2) ?: ""
        val c7 = row0.getOrNull(7) ?: ""

        // Check if row 0 looks like 9-column layout
        if (c0.contains("序号") || c1.contains("题型") || c2.contains("题干") || c7.contains("答案")) {
            return ColumnMapping(
                questionCol = 2,
                optionACol = 3,
                optionBCol = 4,
                optionCCol = 5,
                optionDCol = 6,
                answerCol = 7,
                explanationCol = 8,
                typeCol = 1,
                skipFirstRow = true
            )
        }

        // Check row 1 if available
        if (previewRows.size > 1) {
            val row1 = previewRows[1]
            val r1c0 = row1.getOrNull(0) ?: ""
            val r1c1 = row1.getOrNull(1) ?: ""
            val r1c2 = row1.getOrNull(2) ?: ""

            if (r1c1 in listOf("单选题", "多选题", "判断题", "单选", "多选", "判断") ||
                (r1c0.all { it.isDigit() } && r1c2.isNotBlank())) {
                return ColumnMapping(
                    questionCol = 2,
                    optionACol = 3,
                    optionBCol = 4,
                    optionCCol = 5,
                    optionDCol = 6,
                    answerCol = 7,
                    explanationCol = 8,
                    typeCol = 1,
                    skipFirstRow = true
                )
            }
        }

        // Default 7-column layout
        return ColumnMapping(
            questionCol = 0,
            optionACol = 1,
            optionBCol = 2,
            optionCCol = 3,
            optionDCol = 4,
            answerCol = 5,
            explanationCol = 6,
            typeCol = null,
            skipFirstRow = true
        )
    }

    fun parseExcelWithMapping(
        inputStream: InputStream,
        bankId: Long,
        mapping: ColumnMapping
    ): List<Question> {
        val questions = mutableListOf<Question>()
        val workbook = WorkbookFactory.create(inputStream)
        val sheet = workbook.getSheetAt(0) ?: return emptyList()

        var orderIndex = 0
        for (i in 0..sheet.lastRowNum) {
            val row = sheet.getRow(i) ?: continue

            // Skip row 0 if header row is enabled
            if (i == 0 && mapping.skipFirstRow) continue

            if (isRowEmpty(row)) continue

            val rawTypeStr = if (mapping.typeCol != null && mapping.typeCol >= 0) {
                getCellValueAsString(row.getCell(mapping.typeCol)).trim()
            } else ""

            val questionText = getCellValueAsString(row.getCell(mapping.questionCol)).trim()
            val optionA = getCellValueAsString(row.getCell(mapping.optionACol)).trim()
            val optionB = getCellValueAsString(row.getCell(mapping.optionBCol)).trim()
            val optionC = getCellValueAsString(row.getCell(mapping.optionCCol)).trim()
            val optionD = getCellValueAsString(row.getCell(mapping.optionDCol)).trim()
            val rawAnswer = getCellValueAsString(row.getCell(mapping.answerCol)).trim()
            val explanation = if (mapping.explanationCol >= 0) {
                getCellValueAsString(row.getCell(mapping.explanationCol)).trim()
            } else ""

            if (questionText.isBlank()) continue

            // Determine question type and options
            val isJudgment = rawTypeStr.contains("判断") || (optionC.isBlank() && optionD.isBlank())
            val finalOptionA: String
            val finalOptionB: String
            val normalizedAns: String
            val type: QuestionType

            if (isJudgment) {
                type = QuestionType.JUDGMENT
                finalOptionA = optionA.ifBlank { "正确" }
                finalOptionB = optionB.ifBlank { "错误" }
                normalizedAns = normalizeJudgmentAnswer(rawAnswer)
            } else {
                finalOptionA = optionA
                finalOptionB = optionB
                normalizedAns = normalizeChoiceAnswer(rawAnswer)
                type = when {
                    rawTypeStr.contains("多选") -> QuestionType.MULTIPLE_CHOICE
                    rawTypeStr.contains("单选") -> QuestionType.SINGLE_CHOICE
                    normalizedAns.length > 1 -> QuestionType.MULTIPLE_CHOICE
                    else -> QuestionType.SINGLE_CHOICE
                }
            }

            questions.add(
                Question(
                    bankId = bankId,
                    questionText = questionText,
                    optionA = finalOptionA,
                    optionB = finalOptionB,
                    optionC = optionC,
                    optionD = optionD,
                    rawAnswer = rawAnswer,
                    normalizedAnswer = normalizedAns,
                    explanation = explanation,
                    type = type,
                    orderIndex = orderIndex++
                )
            )
        }

        workbook.close()
        return questions
    }

    private fun isRowEmpty(row: Row): Boolean {
        for (c in 0..8) {
            val cell = row.getCell(c)
            if (cell != null && getCellValueAsString(cell).isNotBlank()) {
                return false
            }
        }
        return true
    }

    private fun normalizeJudgmentAnswer(raw: String): String {
        val upper = raw.trim().uppercase()
        return when {
            upper in listOf("√", "对", "正确", "T", "TRUE", "A", "1") -> "A"
            upper in listOf("×", "错", "错误", "F", "FALSE", "B", "0") -> "B"
            upper.contains("正") || upper.contains("对") || upper.contains("√") -> "A"
            upper.contains("错") || upper.contains("×") -> "B"
            else -> "A"
        }
    }

    private fun normalizeChoiceAnswer(raw: String): String {
        val clean = raw.uppercase().filter { it in 'A'..'D' }
        if (clean.isBlank()) return raw.uppercase()
        return clean.toCharArray().distinct().sorted().joinToString("")
    }

    private fun getCellValueAsString(cell: Cell?): String {
        if (cell == null) return ""
        return when (cell.cellType) {
            CellType.STRING -> cell.stringCellValue
            CellType.NUMERIC -> {
                val num = cell.numericCellValue
                if (num == num.toLong().toDouble()) {
                    num.toLong().toString()
                } else {
                    num.toString()
                }
            }
            CellType.BOOLEAN -> cell.booleanCellValue.toString()
            CellType.FORMULA -> {
                try {
                    cell.stringCellValue
                } catch (e: Exception) {
                    try {
                        cell.numericCellValue.toString()
                    } catch (e2: Exception) {
                        ""
                    }
                }
            }
            else -> ""
        }
    }
}
