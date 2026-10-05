package com.example.forchoiceexam.data.model

data class ColumnMapping(
    val questionCol: Int = 0,
    val optionACol: Int = 1,
    val optionBCol: Int = 2,
    val optionCCol: Int = 3,
    val optionDCol: Int = 4,
    val answerCol: Int = 5,
    val explanationCol: Int = 6,
    val typeCol: Int? = null,
    val skipFirstRow: Boolean = true
)
