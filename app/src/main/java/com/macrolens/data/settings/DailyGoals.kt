package com.macrolens.data.settings

data class DailyGoals(
    val calories: Int = 0,
    val proteinG: Int = 0,
    val carbsG: Int = 0,
    val fatG: Int = 0,
    val fruitVegServings: Int = 0
) {
    fun hasAny(): Boolean =
        calories > 0 || proteinG > 0 || carbsG > 0 || fatG > 0 || fruitVegServings > 0

    companion object {
        val NONE = DailyGoals()
    }
}
