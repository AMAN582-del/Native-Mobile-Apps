package com.aman.shiftmate.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// One row = one worked shift. endTime is null while the user is still clocked in.
@Entity(tableName = "shifts")
data class Shift(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startTime: Long,            // epoch milliseconds
    val endTime: Long? = null,      // epoch milliseconds, null = shift in progress
    val breakMinutes: Int = 0,
    val hourlyRate: Double,         // rate in pounds when the shift was worked
    val note: String = ""
)