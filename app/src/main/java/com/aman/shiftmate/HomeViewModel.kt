
package com.aman.shiftmate

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    // Single entry point for the UI event (unidirectional data flow)
    fun onClockToggle() {
        if (_uiState.value.isClockedIn) clockOut() else clockIn()
    }

    private fun clockIn() {
        val now = System.currentTimeMillis()
        _uiState.update { it.copy(isClockedIn = true, clockInTime = now, elapsedSeconds = 0L) }
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update { state ->
                    val start = state.clockInTime
                    if (start != null) {
                        state.copy(elapsedSeconds = (System.currentTimeMillis() - start) / 1000)
                    } else state
                }
            }
        }
    }

    private fun clockOut() {
        timerJob?.cancel()
        _uiState.update { it.copy(isClockedIn = false, clockInTime = null) }
    }
}