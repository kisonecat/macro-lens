package com.phood.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface DailyLogDao {

    @Query("SELECT * FROM food_entries WHERE date = :date ORDER BY id DESC")
    fun getEntriesForDate(date: LocalDate): Flow<List<FoodEntry>>

    @Query("""
        SELECT
            COALESCE(SUM(calories), 0) as calories,
            COALESCE(SUM(proteinG), 0) as proteinG,
            COALESCE(SUM(carbsG), 0) as carbsG,
            COALESCE(SUM(fatG), 0) as fatG
        FROM food_entries
        WHERE date = :date
    """)
    fun getTotalsForDate(date: LocalDate): Flow<MacroTotals>

    @Insert
    suspend fun insert(entry: FoodEntry): Long

    @Delete
    suspend fun delete(entry: FoodEntry)

    @Query("DELETE FROM food_entries WHERE id = :id")
    suspend fun deleteById(id: Long)
}
