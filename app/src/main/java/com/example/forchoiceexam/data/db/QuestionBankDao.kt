package com.example.forchoiceexam.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.forchoiceexam.data.model.QuestionBank
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestionBankDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBank(bank: QuestionBank): Long

    @Query("SELECT * FROM question_banks ORDER BY importedAt DESC")
    fun getAllBanks(): Flow<List<QuestionBank>>

    @Query("SELECT * FROM question_banks WHERE id = :id")
    suspend fun getBankById(id: Long): QuestionBank?

    @Query("DELETE FROM question_banks WHERE id = :id")
    suspend fun deleteBankById(id: Long): Int

    @Query("UPDATE question_banks SET questionCount = :count WHERE id = :bankId")
    suspend fun updateQuestionCount(bankId: Long, count: Int): Int

    @Query("UPDATE question_banks SET lastSequentialIndex = :index WHERE id = :bankId")
    suspend fun updateLastSequentialIndex(bankId: Long, index: Int): Int
}
