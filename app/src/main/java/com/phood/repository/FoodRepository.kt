package com.phood.repository

import com.phood.data.local.DailyLogDao
import com.phood.data.local.FoodEntry
import com.phood.data.local.MacroTotals
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class FoodRepository(private val dao: DailyLogDao) {

    fun getEntriesForDate(date: LocalDate): Flow<List<FoodEntry>> =
        dao.getEntriesForDate(date)

    fun getTotalsForDate(date: LocalDate): Flow<MacroTotals> =
        dao.getTotalsForDate(date)

    suspend fun addEntry(entry: FoodEntry): Long =
        dao.insert(entry)

    suspend fun deleteEntry(entry: FoodEntry) =
        dao.delete(entry)

    suspend fun deleteEntryById(id: Long) =
        dao.deleteById(id)
}
