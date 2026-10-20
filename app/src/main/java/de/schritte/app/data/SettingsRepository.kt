package de.schritte.app.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class Settings(val heightCm: Int = 175, val weightKg: Int = 70)

class SettingsRepository(private val store: DataStore<Preferences>) {
    private val height = intPreferencesKey("height_cm")
    private val weight = intPreferencesKey("weight_kg")

    val settings: Flow<Settings> = store.data.map {
        Settings(it[height] ?: 175, it[weight] ?: 70)
    }

    suspend fun update(heightCm: Int, weightKg: Int) {
        require(heightCm in 100..250) { "Größe muss zwischen 100 und 250 cm liegen" }
        require(weightKg in 30..300) { "Gewicht muss zwischen 30 und 300 kg liegen" }
        store.edit { it[height] = heightCm; it[weight] = weightKg }
    }
}
