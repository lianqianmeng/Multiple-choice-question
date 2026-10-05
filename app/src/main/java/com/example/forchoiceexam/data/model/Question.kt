package com.example.forchoiceexam.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "questions",
    foreignKeys = [
        ForeignKey(
            entity = QuestionBank::class,
            parentColumns = ["id"],
            childColumns = ["bankId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["bankId"])]
)
data class Question(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val bankId: Long,
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String = "",
    val optionD: String = "",
    val rawAnswer: String,
    val normalizedAnswer: String,
    val explanation: String = "",
    val type: QuestionType,
    val orderIndex: Int
)
