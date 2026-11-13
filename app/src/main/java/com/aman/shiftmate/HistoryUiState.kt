package com.aman.shiftmate

import com.aman.shiftmate.model.Shift

// Everything the History screen needs to display
data class HistoryUiState(
    val shifts: List<Shift> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)