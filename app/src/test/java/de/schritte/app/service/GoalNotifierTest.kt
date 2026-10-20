package de.schritte.app.service

import de.schritte.app.domain.DayState
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class GoalNotifierTest {
    private val day = LocalDate.of(2026, 10, 20)
    private var goal = 10_000
    private var last: LocalDate? = null
    private val sent = mutableListOf<Pair<Int, Int>>()

    private val notifier = GoalNotifier(
        goal = { goal },
        lastNotified = { last },
        markNotified = { last = it },
        notify = { steps, g -> sent += steps to g },
    )

    private fun state(steps: Int, date: LocalDate = day) = DayState(date, steps, 0)

    @Test fun belowGoal_sendsNothing() = runBlocking {
        notifier.onSteps(state(9_999))
        assertEquals(emptyList<Pair<Int, Int>>(), sent)
    }

    @Test fun atGoal_sendsOnceWithStepsAndGoal() = runBlocking {
        notifier.onSteps(state(10_000))
        assertEquals(listOf(10_000 to 10_000), sent)
    }

    @Test fun furtherStepsSameDay_sendNothingMore() = runBlocking {
        notifier.onSteps(state(10_000))
        notifier.onSteps(state(10_400))
        notifier.onSteps(state(15_000))
        assertEquals(1, sent.size)
    }

    @Test fun nextDay_sendsAgain() = runBlocking {
        notifier.onSteps(state(10_000))
        notifier.onSteps(state(10_500, day.plusDays(1)))
        assertEquals(2, sent.size)
    }

    @Test fun goalLoweredLater_notifiesWhenReached() = runBlocking {
        notifier.onSteps(state(6_000))
        goal = 5_000
        notifier.onSteps(state(6_100))
        assertEquals(listOf(6_100 to 5_000), sent)
    }

    @Test fun goalRaisedAfterNotification_doesNotNotifyAgainSameDay() = runBlocking {
        notifier.onSteps(state(10_000))
        goal = 12_000
        notifier.onSteps(state(12_500))
        assertEquals(1, sent.size)
    }

    @Test fun dayIsMarkedBeforeNotifying() = runBlocking {
        var markedWhenSent: LocalDate? = null
        val n = GoalNotifier({ goal }, { last }, { last = it }) { _, _ -> markedWhenSent = last }
        n.onSteps(state(10_000))
        assertEquals(day, markedWhenSent)
    }
}
