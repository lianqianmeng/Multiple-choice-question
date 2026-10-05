package com.example.forchoiceexam.data.model

data class QuestionWithStatus(
    val question: Question,
    val selectedOptions: Set<String> = emptySet(),
    val isSubmitted: Boolean = false,
    val isCorrect: Boolean? = null,
    val isWrongQuestion: Boolean = false
)
