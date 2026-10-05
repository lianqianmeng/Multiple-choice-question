package com.example.forchoiceexam.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "question_banks")
data class QuestionBank(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val importedAt: Long = System.currentTimeMillis(),
    val questionCount: Int = 0,
    val lastSequentialIndex: Int = 0
)
