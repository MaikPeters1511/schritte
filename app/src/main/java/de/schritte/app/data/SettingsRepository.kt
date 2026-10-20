package de.schritte.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.LocalDate

data class Settings(val heightCm: Int = 175, val weightKg: Int = 70, val darkTheme: Boolean? = null, val goalSteps: Int = 10_000)

class SettingsRepository(private val store: DataStore<Preferences>) {
    private val height = intPreferencesKey("height_cm")
    private val weight = intPreferencesKey("weight_kg")
    private val dark = booleanPreferencesKey("dark_theme")
    private val goal = intPreferencesKey("goal_steps")
    private val goalNotified = stringPreferencesKey("goal_notified_date")

    val settings: Flow<Settings> = store.data.map {
        Settings(it[height] ?: 175, it[weight] ?: 70, it[dark], it[goal] ?: 10_000)
    }

    suspend fun update(heightCm: Int, weightKg: Int, goalSteps: Int) {
        require(heightCm in 100..250) { "Größe muss zwischen 100 und 250 cm liegen" }
        require(weightKg in 30..300) { "Gewicht muss zwischen 30 und 300 kg liegen" }
        require(goalSteps in 1_000..100_000) { "Ziel muss zwischen 1.000 und 100.000 Schritten liegen" }
        store.edit { it[height] = heightCm; it[weight] = weightKg; it[goal] = goalSteps }
    }

    suspend fun setDarkTheme(enabled: Boolean) {
        store.edit { it[dark] = enabled }
    }

    suspend fun lastGoalNotificationDate(): LocalDate? =
        store.data.first()[goalNotified]?.let(LocalDate::parse)

    suspend fun markGoalNotified(date: LocalDate) {
        store.edit { it[goalNotified] = date.toString() }
    }
}
