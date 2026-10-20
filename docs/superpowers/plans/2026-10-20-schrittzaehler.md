# Schrittzähler Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Android-App (Kotlin), die Schritte über den Hardware-Schrittzähler korrekt zählt, lokal speichert und Woche/Monat/Distanz/Kalorien anzeigt.

**Architecture:** Reine Kotlin-Domäne (`StepTracker`, `StepCalculator`, `StatsAggregator`) ohne Android-Abhängigkeit, per JUnit entwickelt. Room/DataStore als Persistenz, Foreground-Service als dünner Sensor-Adapter, Compose-UI mit ViewModels.

**Tech Stack:** Kotlin, Gradle Kotlin DSL, minSdk 29, Jetpack Compose, Room, DataStore, Coroutines/Flow, JUnit4, Robolectric, Turbine-freie Flow-Tests (`first()`).

**Spec:** `docs/superpowers/specs/2026-10-20-schrittzaehler-design.md`

## Global Constraints

- Package: `de.schritte.app`; UI-Sprache Deutsch.
- minSdk 29, JDK 17, Android-SDK unter `~/Library/Android/sdk`.
- Schrittlänge = Größe × 0,415; Kalorien = 0,57 kcal × kg × km.
- Kein Tagesziel, kein Cloud-Sync, keine Konten, keine Widgets, kein Health Connect.
- Berechtigungen: `ACTIVITY_RECOGNITION`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_HEALTH`, `POST_NOTIFICATIONS`.
- TDD: kein Produktionscode ohne vorher fehlschlagenden Test (außer Service/Sensor-Hülle, manuell getestet).

## File Structure

```
settings.gradle.kts, build.gradle.kts, gradle/libs.versions.toml
app/build.gradle.kts, app/src/main/AndroidManifest.xml
app/src/main/java/de/schritte/app/
  domain/StepTracker.kt        DayState + Tageszähl-Logik
  domain/StepCalculator.kt     Distanz/Kalorien
  domain/StatsAggregator.kt    Wochen-/Monatsstatistik
  data/StepDatabase.kt         Room: Entity, Dao, Database
  data/StepRepository.kt       Tracker-Zustand + Tageswerte
  data/SettingsRepository.kt   DataStore Größe/Gewicht
  service/StepSensorService.kt Foreground-Service
  service/BootReceiver.kt
  ui/TodayViewModel.kt, StatsViewModel.kt, SettingsViewModel.kt
  ui/*Screen.kt, MainActivity.kt
app/src/test/java/de/schritte/app/... (spiegelt main)
```

---

### Task 1: Projekt-Gerüst mit lauffähigem Testlauf

**Files:** Create: `settings.gradle.kts`, `build.gradle.kts`, `gradle/libs.versions.toml`, `app/build.gradle.kts`, `app/src/main/AndroidManifest.xml`, `local.properties`, `.gitignore`; Test: `app/src/test/java/de/schritte/app/SmokeTest.kt`

**Produces:** Gradle-Wrapper; `./gradlew :app:testDebugUnitTest` läuft.

- [ ] **Step 1:** Nutzer um Erlaubnis bitten, eine Gradle-Distribution (8.x) herunterzuladen; damit `gradle wrapper` ausführen.
- [ ] **Step 2:** Projektdateien anlegen (AGP 8.x, Kotlin 2.x, Compose-Plugin, KSP für Room). `local.properties`: `sdk.dir=/Users/maik/Library/Android/sdk`.
- [ ] **Step 3:** Fehlschlagenden Smoke-Test schreiben: `assertEquals(2, 1 + 0)`. Run: `./gradlew :app:testDebugUnitTest` → FAIL (Test ausgeführt, rot).
- [ ] **Step 4:** Test auf `1 + 1` korrigieren → PASS. Danach Smoke-Test löschen.

### Task 2: StepTracker (Kernlogik)

**Files:** Create: `domain/StepTracker.kt`; Test: `domain/StepTrackerTest.kt`

**Produces:**
```kotlin
data class DayState(val date: LocalDate, val steps: Int, val lastRaw: Long)
class StepTracker(private val zone: ZoneId) {
    fun onSensor(prev: DayState?, raw: Long, now: Instant): DayState
}
```

Jeder Fall ist ein eigener Zyklus (Test → rot → minimaler Code → grün):

- [ ] **Step 1: Test 1 – erster Start** → rot (Klasse fehlt)
```kotlin
private val zone = ZoneId.of("Europe/Berlin")
private fun at(d: String) = LocalDateTime.parse(d).atZone(zone).toInstant()
private val tracker = StepTracker(zone)

@Test fun firstEvent_startsAtZeroWithRawAsBase() {
    val s = tracker.onSensor(null, 5000, at("2026-10-20T08:00:00"))
    assertEquals(DayState(LocalDate.of(2026, 10, 20), 0, 5000), s)
}
```
- [ ] **Step 2:** Minimal implementieren (nur diesen Fall), grün.
- [ ] **Step 3: Test 2 – Zuwachs** (`prev=DayState(d,10,5000)`, raw 5040 → steps 50, lastRaw 5040) → rot → `steps + (raw - lastRaw)` → grün.
- [ ] **Step 4: Test 3 – Tageswechsel** (prev am 20.10., Event am 21.10. 00:05, raw 5100 → `DayState(21.10., 0, 5100)`) → rot → Datum vergleichen → grün.
- [ ] **Step 5: Test 4 – Reboot** (`prev=DayState(d,100,90000)`, raw 30 → steps 130, lastRaw 30) → rot → `if (raw < lastRaw) steps + raw` → grün.
- [ ] **Step 6: Test 5 – Reboot über Mitternacht** (neuer Tag, raw < lastRaw → `steps 0`, lastRaw raw) und **Test 6 – gleicher Rohwert** (steps unverändert) → grün; Refactor.

Endgültige Implementierung:
```kotlin
class StepTracker(private val zone: ZoneId) {
    fun onSensor(prev: DayState?, raw: Long, now: Instant): DayState {
        val today = now.atZone(zone).toLocalDate()
        if (prev == null || prev.date != today) return DayState(today, 0, raw)
        val delta = if (raw < prev.lastRaw) raw else raw - prev.lastRaw
        return prev.copy(steps = prev.steps + delta.toInt(), lastRaw = raw)
    }
}
```
- [ ] **Step 7:** `./gradlew :app:testDebugUnitTest --tests '*StepTrackerTest'` → alle grün; Commit.

### Task 3: StepCalculator

**Files:** Create: `domain/StepCalculator.kt`; Test: `domain/StepCalculatorTest.kt`

**Produces:**
```kotlin
class StepCalculator(private val heightCm: Int, private val weightKg: Int) {
    val stepLengthM: Double
    fun distanceM(steps: Int): Double
    fun kcal(steps: Int): Double
}
```
- [ ] **Step 1: Tests** (rot): 180 cm → `stepLengthM == 0.747` (delta 1e-9); 1000 Schritte bei 180 cm → 747 m; 10000 Schritte, 180 cm, 80 kg → Distanz 7,47 km → kcal `0.57*80*7.47 = 340.632`; 0 Schritte → 0.
- [ ] **Step 2:** Implementieren: `stepLengthM = heightCm * 0.415 / 100`, `distanceM = steps * stepLengthM`, `kcal = 0.57 * weightKg * distanceM / 1000`. Grün, Commit.

### Task 4: StatsAggregator

**Files:** Create: `domain/StatsAggregator.kt`; Test: `domain/StatsAggregatorTest.kt`

**Produces:**
```kotlin
data class DayCount(val date: LocalDate, val steps: Int)
data class Stats(val days: List<DayCount>, val total: Int, val average: Int, val best: DayCount?)
object StatsAggregator {
    fun summarize(steps: Map<LocalDate, Int>, from: LocalDate, to: LocalDate): Stats
}
```
- [ ] **Step 1: Tests** (rot, je ein Zyklus): Lücken werden 0 (3 Tage, nur Tag 1 und 3 vorhanden → `days.size == 3`, mittlerer Tag 0); Summe; Durchschnitt über alle Tage im Zeitraum inkl. Lücken (gerundet, ganzzahlig); bester Tag (bei Gleichstand der frühere); leerer Zeitraum mit nur 0 → `best` ist erster Tag mit 0 oder `null`, festgelegt: `null` wenn Summe 0; Daten außerhalb von from..to werden ignoriert.
- [ ] **Step 2:** Implementieren, grün, Commit.

### Task 5: Room-Persistenz und StepRepository

**Files:** Create: `data/StepDatabase.kt`, `data/StepRepository.kt`; Test: `app/src/test/.../data/StepRepositoryTest.kt` (Robolectric, In-Memory-DB)

**Consumes:** `DayState`, `DayCount` aus Task 2/4.
**Produces:**
```kotlin
class StepRepository(db: StepDatabase) {
    suspend fun load(): DayState?                         // jüngster gespeicherter Tag inkl. lastRaw
    suspend fun save(state: DayState)                     // upsert pro Datum
    fun observe(date: LocalDate): Flow<DayState?>
    fun observeRange(from: LocalDate, to: LocalDate): Flow<Map<LocalDate, Int>>
}
```
Entity `DayEntity(date: String PK ISO, steps: Int, lastRaw: Long)`.
- [ ] **Step 1: Tests** (rot): save→load gibt gleichen State; zweites save desselben Datums überschreibt; load liefert jüngstes Datum; observeRange liefert nur Zeitraum; leere DB → `load() == null`.
- [ ] **Step 2:** Entity/Dao/Database/Repository implementieren, grün, Commit.

### Task 6: SettingsRepository

**Files:** Create: `data/SettingsRepository.kt`; Test: `data/SettingsRepositoryTest.kt` (Robolectric, DataStore in Temp-Datei)

**Produces:**
```kotlin
data class Settings(val heightCm: Int = 175, val weightKg: Int = 70)
class SettingsRepository(store: DataStore<Preferences>) {
    val settings: Flow<Settings>
    suspend fun update(heightCm: Int, weightKg: Int)
}
```
- [ ] **Step 1: Tests** (rot): Defaults 175/70; nach `update(180, 80)` liefert `settings.first()` diese Werte; ungültige Werte (Größe außerhalb 100–250, Gewicht außerhalb 30–300) werfen `IllegalArgumentException`.
- [ ] **Step 2:** Implementieren, grün, Commit.

### Task 7: ViewModels

**Files:** Create: `ui/TodayViewModel.kt`, `ui/StatsViewModel.kt`, `ui/SettingsViewModel.kt`; Tests in `ui/*ViewModelTest.kt` mit Fake-Repositories und `StandardTestDispatcher`.

**Produces:**
```kotlin
data class TodayUi(val steps: Int, val distanceKm: Double, val kcal: Double)
class TodayViewModel(steps: StepRepository, settings: SettingsRepository, clock: Clock) { val ui: StateFlow<TodayUi> }
enum class Range { WEEK, MONTH }
class StatsViewModel(...) { val range: MutableStateFlow<Range>; val stats: StateFlow<Stats> }
class SettingsViewModel(...) { val settings: StateFlow<Settings>; fun save(heightCm: Int, weightKg: Int): Boolean }
```
- [ ] **Step 1:** Je ViewModel Tests (rot): Today kombiniert Schritte mit Settings (10000 Schritte, 180/80 → 7,47 km, 340,6 kcal), ohne Daten 0; Stats WEEK = letzte 7 Tage inkl. heute, MONTH = letzte 30 Tage; Settings `save` gibt bei ungültigen Werten `false` zurück.
- [ ] **Step 2:** Implementieren, grün, Commit.

### Task 8: Service, BootReceiver, Manifest (manuell getestet)

**Files:** Create: `service/StepSensorService.kt`, `service/BootReceiver.kt`, `AndroidManifest.xml` (Modify)

**Consumes:** `StepTracker.onSensor`, `StepRepository.load/save`.
- [ ] **Step 1:** Logik-Test zuerst (rot→grün): kleine Klasse `StepRecorder(tracker, repo, clock)` mit `suspend fun record(raw: Long)` in `service/StepRecorder.kt`; Test mit Fake-Repo prüft load→onSensor→save inklusive Reboot-Fall. Der Service ruft nur noch `record`.
- [ ] **Step 2:** `StepSensorService` (Foreground, Typ `health`) registriert `TYPE_STEP_COUNTER` mit `SENSOR_DELAY_NORMAL` und `maxReportLatency`, ruft `record(event.values[0].toLong())` in einem Coroutine-Scope; Benachrichtigung mit Kanal „Schrittzähler“.
- [ ] **Step 3:** `BootReceiver` startet den Service bei `BOOT_COMPLETED`; Manifest-Berechtigungen laut Constraints.
- [ ] **Step 4:** Build: `./gradlew :app:assembleDebug` → erfolgreich; Commit.

### Task 9: Compose-UI und Verdrahtung

**Files:** Create: `ui/MainActivity.kt`, `ui/TodayScreen.kt`, `ui/StatsScreen.kt`, `ui/SettingsScreen.kt`, `res/values/strings.xml` (deutsch)

- [ ] **Step 1:** Navigation mit drei Tabs (Heute, Statistik, Einstellungen). Heute: große Schrittzahl, Distanz in km, kcal. Statistik: Umschalter Woche/Monat, Balkendiagramm (Compose Canvas), Summe, Durchschnitt, bester Tag. Einstellungen: Größe/Gewicht, Speichern mit Fehlermeldung.
- [ ] **Step 2:** `MainActivity` fragt `ACTIVITY_RECOGNITION` und `POST_NOTIFICATIONS` an und startet danach den Service.
- [ ] **Step 3:** `./gradlew :app:testDebugUnitTest :app:assembleDebug` → alles grün; Commit.

### Task 10: Manuelle Abnahme

- [ ] **Step 1:** Emulator starten, App installieren, Berechtigungen erteilen.
- [ ] **Step 2:** Schritte simulieren: `adb shell cmd sensor` bzw. Emulator-„Virtual sensors“ → Anzeige erhöht sich.
- [ ] **Step 3:** App beenden, Schritte simulieren, App öffnen → Werte stimmen. Neustart des Emulators → Zählung läuft ohne Verlust weiter.
- [ ] **Step 4:** Ergebnis dokumentieren; offene Mängel als neue TDD-Zyklen.

---

## Self-Review

- **Spec-Abdeckung:** StepTracker (T2), Calculator (T3), Aggregator (T4), Room (T5), DataStore (T6), UI/ViewModels (T7, T9), Service/Boot/Berechtigungen (T8), Gerätetest (T10). Keine Lücke.
- **Platzhalter:** keine; Gradle-Download in T1 ist explizit mit Rückfrage markiert.
- **Typkonsistenz:** `DayState`, `DayCount`, `Stats`, `Settings` werden in späteren Tasks unverändert genutzt.
