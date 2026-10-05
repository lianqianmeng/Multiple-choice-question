package com.example.forchoiceexam.data.repository

import android.content.Context
import android.net.Uri
import com.example.forchoiceexam.data.db.ExamDatabase
import com.example.forchoiceexam.data.model.ColumnMapping
import com.example.forchoiceexam.data.model.Question
import com.example.forchoiceexam.data.model.QuestionBank
import com.example.forchoiceexam.data.model.QuestionType
import com.example.forchoiceexam.data.model.WrongQuestion
import com.example.forchoiceexam.data.parser.ExcelParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ExamRepository(private val context: Context) {

    private val db = ExamDatabase.getDatabase(context)
    private val bankDao = db.questionBankDao()
    private val questionDao = db.questionDao()
    private val wrongDao = db.wrongQuestionDao()

    val allBanks: Flow<List<QuestionBank>> = bankDao.getAllBanks()

    suspend fun readExcelPreview(uri: Uri): Pair<List<List<String>>, ColumnMapping>? = withContext(Dispatchers.IO) {
        runCatching {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@runCatching null
            val previewRows = ExcelParser.readPreviewRows(inputStream, maxRows = 5)
            val suggestedMapping = ExcelParser.suggestDefaultMapping(previewRows)
            Pair(previewRows, suggestedMapping)
        }.getOrNull()
    }

    suspend fun importBankFromUriWithMapping(
        uri: Uri,
        customName: String,
        mapping: ColumnMapping
    ): Result<QuestionBank> = withContext(Dispatchers.IO) {
        runCatching {
            val fileName = customName.ifBlank { getFileNameFromUri(uri) ?: "新导入题库" }

            // 1. Create bank entry
            val bank = QuestionBank(
                name = fileName,
                importedAt = System.currentTimeMillis(),
                questionCount = 0,
                lastSequentialIndex = 0
            )
            val bankId = bankDao.insertBank(bank)

            // 2. Parse Excel
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: throw IllegalArgumentException("无法读取文件内容")

            val questions = ExcelParser.parseExcelWithMapping(inputStream, bankId, mapping)
            if (questions.isEmpty()) {
                bankDao.deleteBankById(bankId)
                throw IllegalStateException("Excel 文件中没有解析到有效的题目")
            }

            // 3. Save questions and update count
            questionDao.insertQuestions(questions)
            bankDao.updateQuestionCount(bankId, questions.size)

            bankDao.getBankById(bankId) ?: bank.copy(id = bankId, questionCount = questions.size)
        }
    }

    suspend fun deleteBank(bankId: Long) = withContext(Dispatchers.IO) {
        bankDao.deleteBankById(bankId)
    }

    suspend fun updateLastSequentialIndex(bankId: Long, index: Int) = withContext(Dispatchers.IO) {
        bankDao.updateLastSequentialIndex(bankId, index)
    }

    suspend fun getFirstQuestionIndexByType(bankId: Long, type: QuestionType): Int = withContext(Dispatchers.IO) {
        val questions = questionDao.getQuestionsByBankSequential(bankId)
        val targetIndex = questions.indexOfFirst { it.type == type }
        if (targetIndex != -1) targetIndex else 0
    }

    suspend fun getQuestionsSequential(bankId: Long): List<Question> = withContext(Dispatchers.IO) {
        questionDao.getQuestionsByBankSequential(bankId)
    }

    suspend fun getQuestionsRandom(bankId: Long): List<Question> = withContext(Dispatchers.IO) {
        questionDao.getQuestionsByBankRandom(bankId)
    }

    suspend fun getWrongQuestions(bankId: Long): List<Question> = withContext(Dispatchers.IO) {
        wrongDao.getWrongQuestionsByBank(bankId)
    }

    fun getWrongCountFlow(bankId: Long): Flow<Int> {
        return wrongDao.getWrongCountByBankFlow(bankId)
    }

    suspend fun getWrongQuestionIdsSet(bankId: Long): Set<Long> = withContext(Dispatchers.IO) {
        wrongDao.getWrongQuestionsByBank(bankId).map { it.id }.toSet()
    }

    suspend fun recordUserAnswer(question: Question, userAnswer: String, isCorrect: Boolean) = withContext(Dispatchers.IO) {
        if (!isCorrect) {
            val existing = wrongDao.getWrongQuestionByQuestionId(question.id)
            if (existing != null) {
                wrongDao.insertOrUpdateWrongQuestion(
                    existing.copy(
                        wrongCount = existing.wrongCount + 1,
                        lastWrongAt = System.currentTimeMillis(),
                        lastUserAnswer = userAnswer
                    )
                )
            } else {
                wrongDao.insertOrUpdateWrongQuestion(
                    WrongQuestion(
                        questionId = question.id,
                        bankId = question.bankId,
                        wrongCount = 1,
                        lastWrongAt = System.currentTimeMillis(),
                        lastUserAnswer = userAnswer
                    )
                )
            }
        }
    }

    suspend fun removeFromWrongQuestions(questionId: Long) = withContext(Dispatchers.IO) {
        wrongDao.removeWrongQuestion(questionId)
    }

    suspend fun clearWrongQuestions(bankId: Long) = withContext(Dispatchers.IO) {
        wrongDao.clearWrongQuestionsByBank(bankId)
    }

    fun getFileNameFromUri(uri: Uri): String? {
        var name: String? = null
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex("_display_name")
                    if (index != -1) {
                        name = cursor.getString(index)
                    }
                }
            }
        }
        if (name == null) {
            name = uri.path
            val cut = name?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                name = name.substring(cut + 1)
            }
        }
        return name?.replace(Regex("\\.(xlsx|xls)$", RegexOption.IGNORE_CASE), "")
    }
}
