package de.schritte.app

import android.app.Application
import androidx.datastore.preferences.preferencesDataStore
import androidx.glance.appwidget.updateAll
import de.schritte.app.data.SettingsRepository
import de.schritte.app.data.StepDatabase
import de.schritte.app.data.StepRepository
import de.schritte.app.widget.StepsWidget
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Clock

private val android.content.Context.settingsStore by preferencesDataStore("settings")

class SchritteApp : Application() {
    val clock: Clock = Clock.systemDefaultZone()
    val steps by lazy { StepRepository(StepDatabase.create(this)) }
    val settings by lazy { SettingsRepository(settingsStore) }

    override fun onCreate() {
        super.onCreate()
        // Widgets lose their content when the process is killed; redraw them on every process start.
        CoroutineScope(Dispatchers.Default).launch { StepsWidget().updateAll(this@SchritteApp) }
    }
}
