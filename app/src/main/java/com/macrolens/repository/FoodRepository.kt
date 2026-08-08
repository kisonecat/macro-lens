package com.macrolens.repository

import com.macrolens.data.local.DailyLogDao
import com.macrolens.data.local.FoodEntry
import com.macrolens.data.local.MacroTotals
import com.macrolens.data.remote.FoodEstimate
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.time.LocalDate

class FoodRepository(private val dao: DailyLogDao) {

    fun getEntriesForDate(date: LocalDate): Flow<List<FoodEntry>> =
        dao.getEntriesForDate(date)

    fun getTotalsForDate(date: LocalDate): Flow<MacroTotals> =
        dao.getTotalsForDate(date)

    suspend fun addEntry(entry: FoodEntry): Long =
        dao.insert(entry)

    suspend fun deleteEntry(entry: FoodEntry) {
        entry.imagePath?.let { runCatching { File(it).delete() } }
        entry.thumbnailPath?.let { runCatching { File(it).delete() } }
        dao.delete(entry)
    }

    fun observePending(): Flow<List<FoodEntry>> = dao.observePending()

    suspend fun getFirstPending(): FoodEntry? = dao.getFirstPending()

    suspend fun getById(id: Long): FoodEntry? = dao.getById(id)

    suspend fun markCompleted(id: Long, estimate: FoodEstimate) {
        // Free the full-size image once analysis is done; the thumbnail stays.
        getById(id)?.imagePath?.let { runCatching { File(it).delete() } }
        dao.markCompleted(
            id = id,
            calories = estimate.calories,
            proteinG = estimate.proteinG,
            carbsG = estimate.carbsG,
            fatG = estimate.fatG,
            fruitVegServings = estimate.fruitVegServings,
            description = estimate.description
        )
    }

    suspend fun markFailed(id: Long, errorMessage: String) =
        dao.markFailed(id, errorMessage)

    suspend fun markPending(id: Long) = dao.markPending(id)

    suspend fun updateEntry(id: Long, estimate: FoodEstimate) {
        dao.updateEntry(
            id = id,
            calories = estimate.calories,
            proteinG = estimate.proteinG,
            carbsG = estimate.carbsG,
            fatG = estimate.fatG,
            fruitVegServings = estimate.fruitVegServings,
            description = estimate.description
        )
    }
}
