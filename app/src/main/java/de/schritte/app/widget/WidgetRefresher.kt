package de.schritte.app.widget

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/** Runs [update] right away, but at most once per [minIntervalMs]; skipped requests collapse into one trailing update. */
class WidgetRefresher(
    private val scope: CoroutineScope,
    private val minIntervalMs: Long,
    private val now: () -> Long,
    private val update: suspend () -> Unit,
) {
    private var last = Long.MIN_VALUE / 2
    private var pending: Job? = null

    @Synchronized
    fun request() {
        if (pending?.isActive == true) return
        val wait = last + minIntervalMs - now()
        if (wait <= 0) {
            last = now()
            scope.launch { update() }
        } else {
            pending = scope.launch {
                delay(wait)
                markUpdated()
                update()
            }
        }
    }

    @Synchronized
    private fun markUpdated() { last = now() }
}
