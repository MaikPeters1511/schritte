package de.schritte.app.data

import de.schritte.app.domain.DayState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

class StepRepository(db: StepDatabase) {
    private val dao = db.dayDao()

    suspend fun load(): DayState? = dao.newest()?.toState()

    suspend fun save(state: DayState) =
        dao.upsert(DayEntity(state.date.toString(), state.steps, state.lastRaw))

    fun observeNewest(): Flow<DayState?> = dao.observeNewest().map { it?.toState() }

    fun observeRange(from: LocalDate, to: LocalDate): Flow<Map<LocalDate, Int>> =
        dao.observeRange(from.toString(), to.toString())
            .map { rows -> rows.associate { LocalDate.parse(it.date) to it.steps } }

    private fun DayEntity.toState() = DayState(LocalDate.parse(date), steps, lastRaw)
}
