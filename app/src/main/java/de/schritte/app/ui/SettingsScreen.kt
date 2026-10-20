package de.schritte.app.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.schritte.app.data.Settings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(vm: SettingsViewModel, modifier: Modifier = Modifier) {
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("") }
    var message by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()
    val current by vm.settings.collectAsStateWithLifecycle(Settings())

    LaunchedEffect(Unit) {
        val s = vm.settings.first()
        height = s.heightCm.toString()
        weight = s.weightKg.toString()
        goal = s.goalSteps.toString()
    }

    Column(modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        val numeric = KeyboardOptions(keyboardType = KeyboardType.Number)
        OutlinedTextField(height, { height = it }, label = { Text("Größe (cm)") }, keyboardOptions = numeric)
        OutlinedTextField(weight, { weight = it }, label = { Text("Gewicht (kg)") }, keyboardOptions = numeric)
        OutlinedTextField(goal, { goal = it }, label = { Text("Tagesziel (Schritte)") }, keyboardOptions = numeric)
        Button(onClick = {
            val h = height.toIntOrNull()
            val w = weight.toIntOrNull()
            val g = goal.toIntOrNull()
            scope.launch {
                message = if (h != null && w != null && g != null && vm.save(h, w, g)) "Gespeichert"
                else "Ungültige Werte (Größe 100–250 cm, Gewicht 30–300 kg, Ziel 1.000–100.000)"
            }
        }) { Text("Speichern") }
        message?.let { Text(it) }
        Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
            Text("Dunkles Design")
            Switch(
                checked = current.darkTheme ?: isSystemInDarkTheme(),
                onCheckedChange = { scope.launch { vm.setDarkTheme(it) } },
            )
        }
    }
}
