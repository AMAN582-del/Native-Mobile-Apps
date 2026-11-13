
package com.aman.shiftmate

// Everything the Home screen needs to display, in one immutable object
data class HomeUiState(
    val isClockedIn: Boolean = false,
    val clockInTime: Long? = null,
    val elapsedSeconds: Long = 0L
)