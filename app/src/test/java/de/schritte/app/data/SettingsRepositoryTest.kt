package de.schritte.app.data

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File

class SettingsRepositoryTest {
    @get:Rule val tmp = TemporaryFolder()

    private fun repo() = SettingsRepository(
        PreferenceDataStoreFactory.create { File(tmp.root, "s.preferences_pb") }
    )

    @Test fun defaults_are175And70() = runBlocking {
        assertEquals(Settings(175, 70), repo().settings.first())
    }

    @Test fun update_isPersisted() = runBlocking {
        val r = repo()
        r.update(180, 80, 10_000)
        assertEquals(Settings(180, 80), r.settings.first())
    }

    @Test(expected = IllegalArgumentException::class)
    fun heightOutOfRange_throws(): Unit = runBlocking { repo().update(99, 70, 10_000) }

    @Test(expected = IllegalArgumentException::class)
    fun weightOutOfRange_throws(): Unit = runBlocking { repo().update(175, 301, 10_000) }

    @Test fun darkTheme_defaultsToNull() = runBlocking {
        assertEquals(null, repo().settings.first().darkTheme)
    }

    @Test fun setDarkTheme_isPersisted() = runBlocking {
        val r = repo()
        r.setDarkTheme(true)
        assertEquals(true, r.settings.first().darkTheme)
        r.setDarkTheme(false)
        assertEquals(false, r.settings.first().darkTheme)
    }

    @Test fun updateBodyData_keepsDarkTheme() = runBlocking {
        val r = repo()
        r.setDarkTheme(true)
        r.update(180, 80, 10_000)
        assertEquals(Settings(180, 80, darkTheme = true), r.settings.first())
    }

    @Test fun goal_defaultsTo10000() = runBlocking {
        assertEquals(10_000, repo().settings.first().goalSteps)
    }

    @Test fun goal_isPersisted() = runBlocking {
        val r = repo()
        r.update(180, 80, 12_500)
        assertEquals(Settings(180, 80, goalSteps = 12_500), r.settings.first())
    }

    @Test fun goal_boundsAreAccepted() = runBlocking {
        val r = repo()
        r.update(180, 80, 1_000)
        assertEquals(1_000, r.settings.first().goalSteps)
        r.update(180, 80, 100_000)
        assertEquals(100_000, r.settings.first().goalSteps)
    }

    @Test fun goalOutOfRange_throwsAndKeepsOldValues() = runBlocking {
        val r = repo()
        r.update(180, 80, 8_000)
        for (bad in listOf(999, 100_001)) {
            try { r.update(190, 90, bad); throw AssertionError("expected IllegalArgumentException") }
            catch (_: IllegalArgumentException) {}
        }
        assertEquals(Settings(180, 80, goalSteps = 8_000), r.settings.first())
    }
}
