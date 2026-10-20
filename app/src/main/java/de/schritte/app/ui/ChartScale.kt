package de.schritte.app.ui

object ChartScale {
    private val steps = listOf(10, 15, 20, 25, 30, 40, 50, 60, 80, 100)

    fun niceMax(max: Int): Int {
        if (max <= 0) return 1_000
        var magnitude = 1
        while (magnitude * 10 <= max) magnitude *= 10
        // max in [magnitude, 10 * magnitude); steps are tenths of the mantissa
        val tenths = steps.first { it * magnitude >= max * 10 }
        return tenths * magnitude / 10
    }

    fun yTicks(niceMax: Int): List<Int> = listOf(0, niceMax / 2, niceMax)

    fun labelIndices(count: Int): List<Int> = when {
        count <= 0 -> emptyList()
        count <= 12 -> (0 until count).toList()
        else -> (0 until count).filter { (count - 1 - it) % 5 == 0 }
    }
}
