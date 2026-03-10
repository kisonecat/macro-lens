package com.phood.data.local

data class MacroTotals(
    val calories: Int,
    val proteinG: Int,
    val carbsG: Int,
    val fatG: Int
) {
    companion object {
        val ZERO = MacroTotals(0, 0, 0, 0)
    }
}
