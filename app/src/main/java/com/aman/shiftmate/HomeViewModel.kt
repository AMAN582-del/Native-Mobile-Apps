package com.aman.shiftmate

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aman.shiftmate.data.SettingsRepository
import com.aman.shiftmate.data.ShiftDatabase
import com.aman.shiftmate.data.ShiftRepository
import com.aman.shiftmate.domain.PayCalculator
import com.aman.shiftmate.model.Shift
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ShiftRepository(ShiftDatabase.getInstance(application).shiftDao())
    private val settingsRepository = SettingsRepository(application)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var lastShifts: List<Shift> = emptyList()

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

        // Keeps the "This week" card up to date whenever shifts change
        viewModelScope.launch {
            repository.shifts
                .catch {
                    _uiState.update { it.copy(errorMessage = "Could not load this week's totals.") }
                }
                .collect { list ->
                    lastShifts = list
                    updateWeek()
                }
        }
    }

    // Single entry point for the UI event (unidirectional data flow)
    fun onClockToggle() {
        viewModelScope.launch {
            try {
                if (_uiState.value.isClockedIn) {
                    repository.clockOut()
                } else {
                    // The hourly rate comes from the user's profile
                    repository.clockIn(settingsRepository.get().hourlyRate)
                }
                _uiState.update { it.copy(errorMessage = null) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update { it.copy(errorMessage = "Could not save your shift. Please try again.") }
            }
        }
    }

    /** Called when the Clock screen is shown, so a changed tax region in Profile is picked up. */
    fun refreshWeek() = updateWeek()

    private fun updateWeek() {
        val start = weekStart()
        val inWeek = lastShifts.filter { it.endTime != null && it.startTime >= start }
        val region = settingsRepository.get().taxRegion
        val result = PayCalculator.summarise(inWeek, 52, region)
        _uiState.update {
            it.copy(weekHours = result.hours, weekGross = result.gross, weekNet = result.net)
        }
    }

    /** Monday 00:00 of the current week, in local time. */
    private fun weekStart(): Long {
        val c = Calendar.getInstance()
        c.firstDayOfWeek = Calendar.MONDAY
        c.set(Calendar.HOUR_OF_DAY, 0)
        c.set(Calendar.MINUTE, 0)
        c.set(Calendar.SECOND, 0)
        c.set(Calendar.MILLISECOND, 0)
        c.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        return c.timeInMillis
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