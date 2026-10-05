package com.example.forchoiceexam.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.forchoiceexam.data.model.Question
import com.example.forchoiceexam.data.model.WrongQuestion
import kotlinx.coroutines.flow.Flow

@Dao
interface WrongQuestionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateWrongQuestion(wrongQuestion: WrongQuestion): Long

    @Query("SELECT * FROM wrong_questions WHERE questionId = :questionId")
    suspend fun getWrongQuestionByQuestionId(questionId: Long): WrongQuestion?

    @Query("""
        SELECT q.* FROM questions q 
        INNER JOIN wrong_questions w ON q.id = w.questionId 
        WHERE q.bankId = :bankId 
        ORDER BY w.lastWrongAt DESC
    """)
    suspend fun getWrongQuestionsByBank(bankId: Long): List<Question>

    @Query("""
        SELECT q.* FROM questions q 
        INNER JOIN wrong_questions w ON q.id = w.questionId 
        ORDER BY w.lastWrongAt DESC
    """)
    fun getAllWrongQuestionsFlow(): Flow<List<Question>>

    @Query("SELECT COUNT(*) FROM wrong_questions WHERE bankId = :bankId")
    fun getWrongCountByBankFlow(bankId: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM wrong_questions WHERE bankId = :bankId")
    suspend fun getWrongCountByBank(bankId: Long): Int

    @Query("DELETE FROM wrong_questions WHERE questionId = :questionId")
    suspend fun removeWrongQuestion(questionId: Long): Int

    @Query("DELETE FROM wrong_questions WHERE bankId = :bankId")
    suspend fun clearWrongQuestionsByBank(bankId: Long): Int
}
