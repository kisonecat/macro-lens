package com.macrolens.data.local

import androidx.room.TypeConverter
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun fromLocalDate(date: LocalDate): String = date.toString()

    @TypeConverter
    fun toLocalDate(dateString: String): LocalDate = LocalDate.parse(dateString)

    @TypeConverter
    fun fromEntryStatus(status: EntryStatus): String = status.name

    @TypeConverter
    fun toEntryStatus(value: String): EntryStatus = EntryStatus.valueOf(value)
}
