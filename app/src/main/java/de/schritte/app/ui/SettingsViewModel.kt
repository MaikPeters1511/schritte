package de.schritte.app.ui

import androidx.lifecycle.ViewModel
import de.schritte.app.data.Settings
import kotlinx.coroutines.flow.Flow

class SettingsViewModel(
    val settings: Flow<Settings>,
    private val update: suspend (heightCm: Int, weightKg: Int) -> Unit,
) : ViewModel() {
    suspend fun save(heightCm: Int, weightKg: Int): Boolean =
        try { update(heightCm, weightKg); true } catch (e: IllegalArgumentException) { false }
}
