package com.macrolens.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

enum class EntryStatus {
    PENDING,
    COMPLETED,
    FAILED
}

@Entity(tableName = "food_entries")
data class FoodEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: LocalDate,
    val calories: Int = 0,
    val proteinG: Int = 0,
    val carbsG: Int = 0,
    val fatG: Int = 0,
    val fruitVegServings: Int = 0,
    val description: String = "",
    val thumbnailPath: String? = null,
    val imagePath: String? = null,
    val status: EntryStatus = EntryStatus.COMPLETED,
    val errorMessage: String? = null
)
