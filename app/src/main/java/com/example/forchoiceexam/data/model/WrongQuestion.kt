package com.example.forchoiceexam.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "wrong_questions",
    foreignKeys = [
        ForeignKey(
            entity = Question::class,
            parentColumns = ["id"],
            childColumns = ["questionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = QuestionBank::class,
            parentColumns = ["id"],
            childColumns = ["bankId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["questionId"], unique = true),
        Index(value = ["bankId"])
    ]
)
data class WrongQuestion(
    @PrimaryKey
    val questionId: Long,
    val bankId: Long,
    val wrongCount: Int = 1,
    val lastWrongAt: Long = System.currentTimeMillis(),
    val lastUserAnswer: String = ""
)
