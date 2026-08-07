package com.macrolens.data.local

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
            COALESCE(SUM(fatG), 0) as fatG,
            COALESCE(SUM(fruitVegServings), 0) as fruitVegServings
        FROM food_entries
        WHERE date = :date AND status = 'COMPLETED'
    """)
    fun getTotalsForDate(date: LocalDate): Flow<MacroTotals>

    @Insert
    suspend fun insert(entry: FoodEntry): Long

    @Delete
    suspend fun delete(entry: FoodEntry)

    @Query("DELETE FROM food_entries WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT * FROM food_entries WHERE id = :id")
    suspend fun getById(id: Long): FoodEntry?

    @Query("SELECT * FROM food_entries WHERE status = 'PENDING' ORDER BY id ASC")
    fun observePending(): Flow<List<FoodEntry>>

    @Query("SELECT * FROM food_entries WHERE status = 'PENDING' ORDER BY id ASC LIMIT 1")
    suspend fun getFirstPending(): FoodEntry?

    @Query("""
        UPDATE food_entries
        SET status = 'COMPLETED',
            calories = :calories,
            proteinG = :proteinG,
            carbsG = :carbsG,
            fatG = :fatG,
            fruitVegServings = :fruitVegServings,
            description = :description,
            errorMessage = NULL,
            imagePath = NULL
        WHERE id = :id
    """)
    suspend fun markCompleted(
        id: Long,
        calories: Int,
        proteinG: Int,
        carbsG: Int,
        fatG: Int,
        fruitVegServings: Int,
        description: String
    )

    @Query("UPDATE food_entries SET status = 'FAILED', errorMessage = :errorMessage WHERE id = :id")
    suspend fun markFailed(id: Long, errorMessage: String)

    @Query("UPDATE food_entries SET status = 'PENDING', errorMessage = NULL WHERE id = :id")
    suspend fun markPending(id: Long)
}
