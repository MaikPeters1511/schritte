package de.schritte.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.IBinder
import androidx.core.app.NotificationCompat
import de.schritte.app.R
import de.schritte.app.SchritteApp
import androidx.glance.appwidget.updateAll
import de.schritte.app.domain.StepTracker
import de.schritte.app.widget.StepsWidget
import de.schritte.app.widget.WidgetRefresher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class StepSensorService : Service(), SensorEventListener {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val events = Channel<Long>(Channel.UNLIMITED)
    private lateinit var sensors: SensorManager

    override fun onCreate() {
        super.onCreate()
        val app = application as SchritteApp
        val recorder = StepRecorder(
            StepTracker(app.clock.zone), { app.steps.load() }, { app.steps.save(it) }, app.clock,
        )
        val refresher = WidgetRefresher(scope, 30_000, { System.currentTimeMillis() }) {
            StepsWidget().updateAll(applicationContext)
        }
        val goalNotifier = GoalNotifier(
            goal = { app.settings.settings.first().goalSteps },
            lastNotified = { app.settings.lastGoalNotificationDate() },
            markNotified = { app.settings.markGoalNotified(it) },
            notify = { steps, goal -> GoalNotification(applicationContext).show(steps, goal) },
        )
        scope.launch {
            for (raw in events) {
                val state = recorder.record(raw)
                goalNotifier.onSteps(state)
                refresher.request()
            }
        }

        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL, getString(R.string.channel_name), NotificationManager.IMPORTANCE_LOW)
        )
        val notification = NotificationCompat.Builder(this, CHANNEL)
            .setContentTitle(getString(R.string.notification_title))
            .setSmallIcon(android.R.drawable.ic_menu_directions)
            .setOngoing(true)
            .build()
        startForeground(1, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_HEALTH)

        sensors = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensors.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)?.let {
            sensors.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        events.trySend(event.values[0].toLong())
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int) = START_STICKY

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        sensors.unregisterListener(this)
        scope.cancel()
        super.onDestroy()
    }

    private companion object { const val CHANNEL = "steps" }
}
