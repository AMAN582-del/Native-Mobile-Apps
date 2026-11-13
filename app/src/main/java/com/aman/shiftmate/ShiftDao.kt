package com.aman.shiftmate.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.aman.shiftmate.model.Shift
import kotlinx.coroutines.flow.Flow

@Dao
interface ShiftDao {
    @Insert
    suspend fun insert(shift: Shift): Long

    @Update
    suspend fun update(shift: Shift)

    @Delete
    suspend fun delete(shift: Shift)

    // Flow re-emits automatically whenever the table changes
    @Query("SELECT * FROM shifts ORDER BY startTime DESC")
    fun observeAll(): Flow<List<Shift>>

    @Query("SELECT * FROM shifts WHERE endTime IS NULL LIMIT 1")
    fun observeOpenShift(): Flow<Shift?>

    @Query("SELECT * FROM shifts WHERE endTime IS NULL LIMIT 1")
    suspend fun getOpenShift(): Shift?

    @Query("SELECT * FROM shifts WHERE id = :id")
    suspend fun getById(id: Long): Shift?
}