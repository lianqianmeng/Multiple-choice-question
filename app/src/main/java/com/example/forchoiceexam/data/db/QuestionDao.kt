package com.example.forchoiceexam.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.forchoiceexam.data.model.Question
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<Question>): List<Long>

    @Query("SELECT * FROM questions WHERE bankId = :bankId ORDER BY orderIndex ASC")
    suspend fun getQuestionsByBankSequential(bankId: Long): List<Question>

    @Query("SELECT * FROM questions WHERE bankId = :bankId ORDER BY RANDOM()")
    suspend fun getQuestionsByBankRandom(bankId: Long): List<Question>

    @Query("SELECT * FROM questions WHERE bankId = :bankId ORDER BY orderIndex ASC")
    fun getQuestionsByBankFlow(bankId: Long): Flow<List<Question>>

    @Query("SELECT * FROM questions WHERE id = :id")
    suspend fun getQuestionById(id: Long): Question?

    @Query("SELECT COUNT(*) FROM questions WHERE bankId = :bankId")
    suspend fun getQuestionCountByBank(bankId: Long): Int
}
