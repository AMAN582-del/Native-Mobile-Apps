package com.aman.shiftmate

/** Everything the Clock screen shows. */
data class HomeUiState(
    val isClockedIn: Boolean = false,
    val clockInTime: Long? = null,
    val elapsedSeconds: Long = 0L,
    val weekHours: Double = 0.0,
    val weekGross: Double = 0.0,
    val weekNet: Double = 0.0,
    val errorMessage: String? = null
)