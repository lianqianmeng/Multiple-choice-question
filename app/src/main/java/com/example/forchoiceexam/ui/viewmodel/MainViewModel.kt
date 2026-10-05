package com.example.forchoiceexam.ui.viewmodel

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.forchoiceexam.data.model.ColumnMapping
import com.example.forchoiceexam.data.model.QuestionBank
import com.example.forchoiceexam.data.model.QuestionType
import com.example.forchoiceexam.data.repository.ExamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ImportUiState {
    object Idle : ImportUiState
    object Loading : ImportUiState
    data class Success(val bankName: String) : ImportUiState
    data class Error(val message: String) : ImportUiState
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ExamRepository(application)

    val allBanks: StateFlow<List<QuestionBank>> = repository.allBanks
        .toStateFlow(emptyList())

    private val _importState = MutableStateFlow<ImportUiState>(ImportUiState.Idle)
    val importState: StateFlow<ImportUiState> = _importState.asStateFlow()

    suspend fun readExcelPreview(uri: Uri): Pair<List<List<String>>, ColumnMapping>? {
        return repository.readExcelPreview(uri)
    }

    fun getFileNameFromUri(uri: Uri): String {
        return repository.getFileNameFromUri(uri) ?: "新导入题库"
    }

    fun importBankWithMapping(uri: Uri, customName: String, mapping: ColumnMapping) {
        viewModelScope.launch {
            _importState.value = ImportUiState.Loading
            val result = repository.importBankFromUriWithMapping(uri, customName, mapping)
            result.onSuccess { bank ->
                _importState.value = ImportUiState.Success(bank.name)
            }.onFailure { error ->
                _importState.value = ImportUiState.Error(error.localizedMessage ?: "导入失败")
            }
        }
    }

    fun resetImportState() {
        _importState.value = ImportUiState.Idle
    }

    fun deleteBank(bankId: Long) {
        viewModelScope.launch {
            repository.deleteBank(bankId)
        }
    }

    fun getWrongCountFlow(bankId: Long) = repository.getWrongCountFlow(bankId)

    suspend fun getFirstQuestionIndexByType(bankId: Long, type: QuestionType): Int {
        return repository.getFirstQuestionIndexByType(bankId, type)
    }

    private fun <T> kotlinx.coroutines.flow.Flow<T>.toStateFlow(initialValue: T): StateFlow<T> {
        val stateFlow = MutableStateFlow(initialValue)
        viewModelScope.launch {
            collect { stateFlow.value = it }
        }
        return stateFlow.asStateFlow()
    }
}
