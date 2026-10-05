package com.example.forchoiceexam

import com.example.forchoiceexam.data.model.Question
import com.example.forchoiceexam.data.model.QuestionType
import com.example.forchoiceexam.data.model.QuestionWithStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PracticeViewModelTest {

    @Test
    fun testQuestionWithStatus_SingleChoiceEvaluation() {
        val question = Question(
            id = 1L,
            bankId = 10L,
            questionText = "测试单选题",
            optionA = "选项A",
            optionB = "选项B",
            optionC = "选项C",
            optionD = "选项D",
            rawAnswer = "B",
            normalizedAnswer = "B",
            type = QuestionType.SINGLE_CHOICE,
            orderIndex = 0
        )

        val status = QuestionWithStatus(question = question, selectedOptions = setOf("B"))
        val selectedAnswer = status.selectedOptions.sorted().joinToString("")
        val isCorrect = selectedAnswer == question.normalizedAnswer

        assertTrue(isCorrect)

        val statusWrong = QuestionWithStatus(question = question, selectedOptions = setOf("A"))
        val selectedAnswerWrong = statusWrong.selectedOptions.sorted().joinToString("")
        val isWrongCorrect = selectedAnswerWrong == question.normalizedAnswer

        assertFalse(isWrongCorrect)
    }

    @Test
    fun testQuestionWithStatus_MultipleChoiceEvaluation() {
        val question = Question(
            id = 2L,
            bankId = 10L,
            questionText = "测试多选题",
            optionA = "选项A",
            optionB = "选项B",
            optionC = "选项C",
            optionD = "选项D",
            rawAnswer = "A, C",
            normalizedAnswer = "AC",
            type = QuestionType.MULTIPLE_CHOICE,
            orderIndex = 1
        )

        // Selected 'C' and 'A' (unordered set)
        val status = QuestionWithStatus(question = question, selectedOptions = setOf("C", "A"))
        val selectedAnswer = status.selectedOptions.sorted().joinToString("")

        assertEquals("AC", selectedAnswer)
        assertEquals(question.normalizedAnswer, selectedAnswer)
    }
}
