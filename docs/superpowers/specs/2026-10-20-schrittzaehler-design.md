# Schrittzähler-App – Design

Datum: 2026-10-20

## Ziel
Native Android-App (Kotlin), die Schritte über den Hardware-Schrittzähler korrekt zählt, lokal speichert und Statistiken (Woche/Monat, Distanz, Kalorien) zeigt. Entwicklung strikt per TDD.

## Entscheidungen
- Quelle: `Sensor.TYPE_STEP_COUNTER` (Rohwert seit Reboot).
- Umfang: Heute-Ansicht, Statistik (Woche/Monat), Einstellungen (Größe, Gewicht). Kein Tagesziel, kein Cloud-Sync, keine Konten, keine Widgets, kein Health Connect.
- Distanz: Schrittlänge = Größe × 0,415. Kalorien aus Gewicht und Distanz (0,57 kcal je kg je km).
- UI-Sprache: Deutsch. Package: `de.schritte.app`.
- Stack: Kotlin, Gradle Kotlin DSL, minSdk 29, Jetpack Compose, Room, DataStore, Coroutines/Flow.

## Architektur
Reine Kotlin-Domäne (ohne Android-Abhängigkeit, JUnit-testbar) plus dünne Android-Adapter.

### Domäne
- `StepTracker`: Eingabe `(rawValue, timestamp)`, Ausgabe aktualisierter `DayState`. Uhr/Zeitzone injiziert.
  - Erster Start: Rohwert wird Basis, 0 Schritte.
  - Normal: Tagesschritte = gespeicherte Schritte + (raw − lastRaw).
  - Tageswechsel: neuer Tag startet bei 0; Schritte seit letztem Event werden nicht dem neuen Tag zugerechnet.
  - Reboot (raw < lastRaw): raw zählt als Zuwachs, bisherige Tagesschritte bleiben.
- `StepCalculator`: Distanz (m) und Kalorien (kcal) aus Schritten, Größe, Gewicht.
- `StatsAggregator`: Wochen-/Monatssummen, Durchschnitt, bester Tag; fehlende Tage = 0.

### Daten
- `StepRepository` (Room): pro Tag Schritte; zusätzlich `lastRaw` und Datum des letzten Events.
- `SettingsRepository` (DataStore): Größe, Gewicht.

### Android-Adapter
- `StepSensorService` (Foreground-Service) registriert den Sensor, ruft `StepTracker` und speichert.
- `BootReceiver` startet den Service nach Neustart.
- Compose-UI: Heute, Statistik, Einstellungen; ViewModels mit Flows.
- Berechtigungen: `ACTIVITY_RECOGNITION` (Laufzeit), `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_HEALTH`, `POST_NOTIFICATIONS`.

## Tests
- JUnit für `StepTracker`, `StepCalculator`, `StatsAggregator` (Red → Green → Refactor, ein Fall je Zyklus).
- Room-Tests mit Robolectric, ViewModel-Tests mit Fake-Repositories.
- Service/Sensor: manuell auf Gerät/Emulator.

## Offen / Risiko
- Gradle ist lokal nicht installiert; Wrapper wird nach Rückfrage über eine heruntergeladene Distribution erzeugt.
- Android-SDK liegt unter `~/Library/Android/sdk`, JDK 17 vorhanden.
