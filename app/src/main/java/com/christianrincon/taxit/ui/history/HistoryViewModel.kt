package com.christianrincon.taxit.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.christianrincon.taxit.data.TaxRepository
import com.christianrincon.taxit.data.db.TaxCalculationEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val taxRepository: TaxRepository
) : ViewModel() {

    val calculationHistory: StateFlow<List<TaxCalculationEntity>> =
        taxRepository.getAllCalculations()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = emptyList()
            )

    fun deleteCalculation(id: Long) {
        viewModelScope.launch {
            taxRepository.deleteCalculation(id)
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            taxRepository.deleteAllCalculations()
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}
