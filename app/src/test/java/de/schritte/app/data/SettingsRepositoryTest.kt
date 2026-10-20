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
        r.update(180, 80)
        assertEquals(Settings(180, 80), r.settings.first())
    }

    @Test(expected = IllegalArgumentException::class)
    fun heightOutOfRange_throws(): Unit = runBlocking { repo().update(99, 70) }

    @Test(expected = IllegalArgumentException::class)
    fun weightOutOfRange_throws(): Unit = runBlocking { repo().update(175, 301) }
}
