package com.aman.shiftmate.data

import com.aman.shiftmate.model.Shift
import kotlinx.coroutines.flow.Flow

// Repository pattern: the rest of the app talks to this, not to Room directly.
// Later this will also coordinate the remote (internet) data source.
class ShiftRepository(private val dao: ShiftDao) {

    val shifts: Flow<List<Shift>> = dao.observeAll()
    val openShift: Flow<Shift?> = dao.observeOpenShift()

    suspend fun clockIn(hourlyRate: Double) {
        // Ignore a second clock-in if a shift is already open
        if (dao.getOpenShift() == null) {
            dao.insert(Shift(startTime = System.currentTimeMillis(), hourlyRate = hourlyRate))
        }
    }

    suspend fun clockOut() {
        val open = dao.getOpenShift() ?: return
        dao.update(open.copy(endTime = System.currentTimeMillis()))
    }
}