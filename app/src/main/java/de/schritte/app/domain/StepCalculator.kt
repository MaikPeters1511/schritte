package de.schritte.app.domain

class StepCalculator(private val heightCm: Int, private val weightKg: Int) {
    val stepLengthM: Double = heightCm * 0.415 / 100

    fun distanceM(steps: Int): Double = steps * stepLengthM

    fun kcal(steps: Int): Double = 0.57 * weightKg * distanceM(steps) / 1000
}
