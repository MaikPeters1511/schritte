package de.schritte.app

import android.app.Application
import androidx.datastore.preferences.preferencesDataStore
import de.schritte.app.data.SettingsRepository
import de.schritte.app.data.StepDatabase
import de.schritte.app.data.StepRepository
import java.time.Clock

private val android.content.Context.settingsStore by preferencesDataStore("settings")

class SchritteApp : Application() {
    val clock: Clock = Clock.systemDefaultZone()
    val steps by lazy { StepRepository(StepDatabase.create(this)) }
    val settings by lazy { SettingsRepository(settingsStore) }
}
