package de.schritte.app.ui

import android.Manifest
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import de.schritte.app.R
import de.schritte.app.SchritteApp
import de.schritte.app.data.Settings
import de.schritte.app.service.StepSensorService

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        val app = application as SchritteApp
        setContent {
            val settings by app.settings.settings.collectAsStateWithLifecycle(Settings())
            val dark = settings.darkTheme ?: isSystemInDarkTheme()
            DisposableEffect(dark) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark },
                    navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark },
                )
                onDispose {}
            }
            MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
                val launcher = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestMultiplePermissions()
                ) { result ->
                    if (result[Manifest.permission.ACTIVITY_RECOGNITION] == true) startTracking()
                }
                LaunchedEffect(Unit) {
                    launcher.launch(
                        arrayOf(Manifest.permission.ACTIVITY_RECOGNITION, Manifest.permission.POST_NOTIFICATIONS)
                    )
                }
                Root(app)
            }
        }
    }

    private fun startTracking() {
        ContextCompat.startForegroundService(this, Intent(this, StepSensorService::class.java))
    }
}

@Composable
private fun Root(app: SchritteApp) {
    var tab by rememberSaveable { mutableIntStateOf(0) }
    val labels = listOf(R.string.tab_today, R.string.tab_stats, R.string.tab_settings)

    val today = viewModel<TodayViewModel>(factory = viewModelFactory {
        initializer {
            TodayViewModel(app.steps.observeNewest(), app.settings.settings, app.clock)
        }
    })
    val stats = viewModel<StatsViewModel>(factory = viewModelFactory {
        initializer { StatsViewModel(app.clock) { f, t -> app.steps.observeRange(f, t) } }
    })
    val settings = viewModel<SettingsViewModel>(factory = viewModelFactory {
        initializer { SettingsViewModel(app.settings.settings, { app.settings.setDarkTheme(it) }) { h, w, g -> app.settings.update(h, w, g) } }
    })

    Scaffold(bottomBar = {
        NavigationBar {
            labels.forEachIndexed { i, label ->
                NavigationBarItem(selected = tab == i, onClick = { tab = i },
                    icon = {}, label = { Text(androidx.compose.ui.res.stringResource(label)) })
            }
        }
    }) { padding ->
        val m = Modifier.padding(padding)
        when (tab) {
            0 -> TodayScreen(today, m)
            1 -> StatsScreen(stats, m)
            else -> SettingsScreen(settings, m)
        }
    }
}
