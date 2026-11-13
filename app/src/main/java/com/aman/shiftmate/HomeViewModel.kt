package com.aman.shiftmate

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aman.shiftmate.data.ShiftDatabase
import com.aman.shiftmate.data.ShiftRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Placeholder rate until a settings screen lets the user enter their own
private const val DEFAULT_HOURLY_RATE = 12.0

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ShiftRepository(ShiftDatabase.getInstance(application).shiftDao())

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        // The database is the source of truth: an open shift in Room means "clocked in".
        // This also restores the clocked-in state if the app was closed mid-shift.
        viewModelScope.launch {
            repository.openShift.collect { open ->
                if (open != null) {
                    _uiState.update { it.copy(isClockedIn = true, clockInTime = open.startTime) }
                    startTimer(open.startTime)
                } else {
                    timerJob?.cancel()
                    _uiState.update {
                        it.copy(isClockedIn = false, clockInTime = null, elapsedSeconds = 0L)
                    }
                }
            }
        }
    }

    // Single entry point for the UI event (unidirectional data flow)
    fun onClockToggle() {
        viewModelScope.launch {
            try {
                if (_uiState.value.isClockedIn) repository.clockOut()
                else repository.clockIn(DEFAULT_HOURLY_RATE)
                _uiState.update { it.copy(errorMessage = null) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update { it.copy(errorMessage = "Could not save your shift. Please try again.") }
            }
        }
    }

    private fun startTimer(start: Long) {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                _uiState.update { it.copy(elapsedSeconds = (System.currentTimeMillis() - start) / 1000) }
                delay(1000)
            }
        }
    }
}