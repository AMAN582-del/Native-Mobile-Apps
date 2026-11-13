package com.aman.shiftmate

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.aman.shiftmate.data.ShiftDatabase
import com.aman.shiftmate.data.ShiftRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = ShiftRepository(ShiftDatabase.getInstance(application).shiftDao())

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        // Room's Flow re-emits whenever a shift is added or changed, so the list stays up to date
        viewModelScope.launch {
            repository.shifts
                .catch {
                    // Database read failed: tell the user instead of crashing
                    _uiState.update { s ->
                        s.copy(isLoading = false, errorMessage = "Could not load your shifts.")
                    }
                }
                .collect { list ->
                    _uiState.update { it.copy(shifts = list, isLoading = false, errorMessage = null) }
                }
        }
    }
}