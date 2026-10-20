package de.schritte.app.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import de.schritte.app.domain.DayState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate

@RunWith(RobolectricTestRunner::class)
class StepRepositoryTest {
    private lateinit var db: StepDatabase
    private lateinit var repo: StepRepository
    private val d1 = LocalDate.of(2026, 10, 19)
    private val d2 = LocalDate.of(2026, 10, 20)

    @Before fun setUp() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(ctx, StepDatabase::class.java).allowMainThreadQueries().build()
        repo = StepRepository(db)
    }

    @After fun tearDown() = db.close()

    @Test fun emptyDb_loadsNull() = runBlocking { assertNull(repo.load()) }

    @Test fun saveThenLoad_roundTrips() = runBlocking {
        repo.save(DayState(d1, 120, 5000))
        assertEquals(DayState(d1, 120, 5000), repo.load())
    }

    @Test fun saveSameDate_overwrites() = runBlocking {
        repo.save(DayState(d1, 120, 5000))
        repo.save(DayState(d1, 150, 5030))
        assertEquals(DayState(d1, 150, 5030), repo.load())
    }

    @Test fun load_returnsNewestDay() = runBlocking {
        repo.save(DayState(d2, 7, 20))
        repo.save(DayState(d1, 99, 10))
        assertEquals(DayState(d2, 7, 20), repo.load())
    }

    @Test fun observeRange_returnsOnlyRange() = runBlocking {
        repo.save(DayState(d1, 10, 1))
        repo.save(DayState(d2, 20, 2))
        assertEquals(mapOf(d2 to 20), repo.observeRange(d2, d2).first())
    }

    @Test fun observeNewest_emitsLatestSavedDay() = runBlocking {
        repo.save(DayState(d1, 10, 1))
        repo.save(DayState(d2, 20, 2))
        assertEquals(DayState(d2, 20, 2), repo.observeNewest().first())
    }
}
