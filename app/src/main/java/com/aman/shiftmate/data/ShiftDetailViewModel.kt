
package com.aman.shiftmate

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.aman.shiftmate.data.ShiftDatabase
import com.aman.shiftmate.data.ShiftRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ShiftDetailViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : AndroidViewModel(application) {

    private val repository = ShiftRepository(ShiftDatabase.getInstance(application).shiftDao())

    // The shift id is passed in the navigation route ("detail/{shiftId}")
    private val shiftId: Long = savedStateHandle.get<Long>("shiftId") ?: -1L

    private val _uiState = MutableStateFlow(ShiftDetailUiState())
    val uiState: StateFlow<ShiftDetailUiState> = _uiState.asStateFlow()

    init {
        loadShift()
    }

    private fun loadShift() {
        viewModelScope.launch {
            try {
                val shift = repository.getShift(shiftId)
                _uiState.update {
                    if (shift == null) {
                        it.copy(isLoading = false, errorMessage = "This shift no longer exists.")
                    } else {
                        it.copy(shift = shift, isLoading = false)
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update { it.copy(isLoading = false, errorMessage = "Could not load this shift.") }
            }
        }
    }

    fun onDeleteClicked() {
        val shift = _uiState.value.shift ?: return
        viewModelScope.launch {
            try {
                repository.deleteShift(shift)
                _uiState.update { it.copy(deleted = true) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update { it.copy(errorMessage = "Could not delete this shift.") }
            }
        }
    }
}