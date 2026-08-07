package com.macrolens.data.local

data class MacroTotals(
    val calories: Int,
    val proteinG: Int,
    val carbsG: Int,
    val fatG: Int,
    val fruitVegServings: Int
) {
    companion object {
        val ZERO = MacroTotals(0, 0, 0, 0, 0)
    }
}
