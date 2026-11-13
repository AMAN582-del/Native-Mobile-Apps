package com.aman.shiftmate

import com.aman.shiftmate.model.Shift

// Everything the Shift detail screen needs to display
data class ShiftDetailUiState(
    val shift: Shift? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val deleted: Boolean = false
)