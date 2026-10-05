package com.example.forchoiceexam.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.forchoiceexam.data.model.QuestionType
import com.example.forchoiceexam.data.model.QuestionWithStatus
import com.example.forchoiceexam.data.repository.ExamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class PracticeMode(val titleName: String) {
    SEQUENTIAL("顺序做题"),
    RANDOM("随机抽题"),
    ERROR_BOOK("错题本测试")
}

data class PracticeUiState(
    val bankId: Long = 0,
    val mode: PracticeMode = PracticeMode.SEQUENTIAL,
    val questions: List<QuestionWithStatus> = emptyList(),
    val currentIndex: Int = 0,
    val isLoading: Boolean = true,
    val wrongQuestionIds: Set<Long> = emptySet(),
    val title: String = ""
) {
    val currentQuestion: QuestionWithStatus?
        get() = questions.getOrNull(currentIndex)
}

class PracticeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ExamRepository(application)

    private val _uiState = MutableStateFlow(PracticeUiState())
    val uiState: StateFlow<PracticeUiState> = _uiState.asStateFlow()

    fun loadQuestions(bankId: Long, mode: PracticeMode, customTitle: String = "", startIndex: Int = 0) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            val rawQuestions = when (mode) {
                PracticeMode.SEQUENTIAL -> repository.getQuestionsSequential(bankId)
                PracticeMode.RANDOM -> repository.getQuestionsRandom(bankId)
                PracticeMode.ERROR_BOOK -> repository.getWrongQuestions(bankId)
            }

            val wrongIds = repository.getWrongQuestionIdsSet(bankId)

            val statusList = rawQuestions.map { question ->
                QuestionWithStatus(
                    question = question,
                    isWrongQuestion = wrongIds.contains(question.id)
                )
            }

            val validStartIndex = if (startIndex in statusList.indices) startIndex else 0

            _uiState.value = PracticeUiState(
                bankId = bankId,
                mode = mode,
                questions = statusList,
                currentIndex = validStartIndex,
                isLoading = false,
                wrongQuestionIds = wrongIds,
                title = if (customTitle.isNotEmpty()) customTitle else mode.titleName
            )

            if (mode == PracticeMode.SEQUENTIAL) {
                repository.updateLastSequentialIndex(bankId, validStartIndex)
            }
        }
    }

    fun selectOption(optionKey: String) {
        val state = _uiState.value
        val current = state.currentQuestion ?: return
        if (current.isSubmitted) return

        val newSelected = if (current.question.type == QuestionType.MULTIPLE_CHOICE) {
            if (current.selectedOptions.contains(optionKey)) {
                current.selectedOptions - optionKey
            } else {
                current.selectedOptions + optionKey
            }
        } else {
            setOf(optionKey)
        }

        val updatedQuestion = current.copy(selectedOptions = newSelected)
        val updatedList = state.questions.toMutableList()
        updatedList[state.currentIndex] = updatedQuestion

        _uiState.value = state.copy(questions = updatedList)

        // For Single Choice / Judgment, submit immediately
        if (current.question.type != QuestionType.MULTIPLE_CHOICE && newSelected.isNotEmpty()) {
            evaluateAndSubmit(updatedQuestion, state.currentIndex)
        }
    }

    fun submitAnswer() {
        val state = _uiState.value
        val current = state.currentQuestion ?: return
        if (current.isSubmitted) return

        evaluateAndSubmit(current, state.currentIndex)
    }

    private fun evaluateAndSubmit(item: QuestionWithStatus, index: Int) {
        val state = _uiState.value
        val selectedJoined = item.selectedOptions.sorted().joinToString("")
        val correctAns = item.question.normalizedAnswer
        val isCorrect = selectedJoined == correctAns

        val submittedItem = item.copy(
            isSubmitted = true,
            isCorrect = isCorrect
        )

        val updatedList = state.questions.toMutableList()
        updatedList[index] = submittedItem

        _uiState.value = state.copy(questions = updatedList)

        viewModelScope.launch {
            repository.recordUserAnswer(
                question = item.question,
                userAnswer = selectedJoined,
                isCorrect = isCorrect
            )
            val updatedWrongIds = repository.getWrongQuestionIdsSet(state.bankId)
            _uiState.value = _uiState.value.copy(
                wrongQuestionIds = updatedWrongIds
            )
        }
    }

    fun jumpToQuestion(index: Int) {
        val state = _uiState.value
        if (index in state.questions.indices) {
            _uiState.value = state.copy(currentIndex = index)
            saveCurrentSequentialIndex(state.bankId, state.mode, index)
        }
    }

    fun nextQuestion() {
        val state = _uiState.value
        if (state.currentIndex < state.questions.lastIndex) {
            val newIndex = state.currentIndex + 1
            _uiState.value = state.copy(currentIndex = newIndex)
            saveCurrentSequentialIndex(state.bankId, state.mode, newIndex)
        }
    }

    fun previousQuestion() {
        val state = _uiState.value
        if (state.currentIndex > 0) {
            val newIndex = state.currentIndex - 1
            _uiState.value = state.copy(currentIndex = newIndex)
            saveCurrentSequentialIndex(state.bankId, state.mode, newIndex)
        }
    }

    private fun saveCurrentSequentialIndex(bankId: Long, mode: PracticeMode, index: Int) {
        if (mode == PracticeMode.SEQUENTIAL) {
            viewModelScope.launch {
                repository.updateLastSequentialIndex(bankId, index)
            }
        }
    }

    fun removeCurrentQuestionFromWrongBook() {
        val state = _uiState.value
        val current = state.currentQuestion ?: return
        viewModelScope.launch {
            repository.removeFromWrongQuestions(current.question.id)
            if (state.mode == PracticeMode.ERROR_BOOK) {
                val updatedList = state.questions.filter { it.question.id != current.question.id }
                val newIndex = if (state.currentIndex >= updatedList.size) {
                    (updatedList.size - 1).coerceAtLeast(0)
                } else {
                    state.currentIndex
                }
                val updatedWrongIds = repository.getWrongQuestionIdsSet(state.bankId)
                _uiState.value = state.copy(
                    questions = updatedList,
                    currentIndex = newIndex,
                    wrongQuestionIds = updatedWrongIds
                )
            } else {
                val updatedWrongIds = repository.getWrongQuestionIdsSet(state.bankId)
                _uiState.value = state.copy(wrongQuestionIds = updatedWrongIds)
            }
        }
    }
}
