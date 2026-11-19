# Developer & AI Agent Guidelines — `aod` (Always On Display / Keep Screen Awake)

> **Location:** `./android-apps/aod/`  
> **Parent Workspace:** `work`  
> **Role:** Android utility application to keep the phone screen always awake, with AMOLED AOD screensaver, OLED anti-burn-in protection, and Quick Settings tile.  
> **Design Language:** Slate Dark Material Design 3 (inspired by modern dark theme UI with signature wavy scallop buttons, pill filters, and inset cards).

---

## 📱 Tech Stack & Specifications

- **Language:** Kotlin 1.9.23
- **Android Target:** Min SDK 26 (Android 8.0 Oreo), Target & Compile SDK 35 (Android 15)
- **UI Framework:** Jetpack Compose + Material Design 3 (`androidx.compose.material3:material3`)
- **State Management:** Android ViewModel + Kotlin Coroutines & `StateFlow`
- **Data Persistence:** Android `SharedPreferences` (AodPreferences)
- **Architecture:** Clean Architecture with MVI/MVVM presentation layer
- **JDK Requirement:** OpenJDK 17 (`C:\Program Files\Android\Android Studio\jbr`)

---

## 🏗️ Project Architecture

```
android-apps/aod/
├── app/
│   ├── build.gradle.kts                   # Module build configuration & dependencies
│   ├── proguard-rules.pro                 # Release obfuscation & optimization rules
│   └── src/main/
│       ├── AndroidManifest.xml            # Permissions, Services, Quick Settings Tile
│       ├── java/com/antigravity/aod/
│       │   ├── MainActivity.kt            # App entry point, immersive full-screen handler
│       │   ├── data/
│       │   │   └── AodPreferences.kt      # Persistent settings (WakeLock, AMOLED, timeouts)
│       │   ├── service/
│       │   │   ├── KeepScreenService.kt   # Foreground service with WakeLock & notifications
│       │   │   └── KeepScreenTileService.kt # Android Quick Settings Tile (1-tap toggle)
│       │   ├── ui/
│       │   │   ├── components/
│       │   │   │   └── ScallopButton.kt   # Signature wavy scallop power button
│       │   │   ├── screens/
│       │   │   │   ├── HomeScreen.kt      # Main dashboard with big scallop power toggle
│       │   │   │   ├── ModesScreen.kt     # Timeout & charging modes with filter chips
│       │   │   │   ├── AodProfilesScreen.kt # Rich profile cards & AMOLED info
│       │   │   │   ├── AodDisplayScreen.kt # Fullscreen OLED clock with anti-burn-in
│       │   │   │   └── SettingsScreen.kt  # Toggles, slider, QS tile instructions
│       │   │   ├── theme/
│       │   │   │   ├── Color.kt           # Slate dark palette tokens (RyBackground, RyPrimary)
│       │   │   │   ├── Theme.kt           # MD3 dark theme & shapes
│       │   │   │   └── Type.kt            # Material 3 Typography scale
│       │   │   └── viewmodel/
│       │   │       └── AodViewModel.kt    # StateFlow, battery monitor, service intents
│       └── res/                           # Values, vector drawables, mipmaps, XML rules
├── build.gradle.kts                       # Root project build file
├── settings.gradle.kts                    # Root settings & plugin repositories
├── gradle.properties                     # JVM args & AndroidX flags
├── gradlew / gradlew.bat                  # Gradle Wrapper 8.6 scripts
└── AGENTS.md                              # This file
```

---

## 🛠️ Build & Verification Protocol

When working inside `./android-apps/aod/`:
1. **Always use JDK 17:**
   ```powershell
   $env:JAVA_HOME = "C:\Program Files\Android\Android Studio\jbr"
   $env:Path = "$env:JAVA_HOME\bin;" + $env:Path
   ```
2. **Build debug APK:**
   ```powershell
   .\gradlew.bat assembleDebug --no-daemon
   ```
3. **Compile debug sources check:**
   ```powershell
   .\gradlew.bat compileDebugSources --no-daemon
   ```
4. **Run unit tests:**
   ```powershell
   .\gradlew.bat test --no-daemon
   ```

---

## 🎨 UI & Aesthetics Rules

- **Palette:** Slate Dark (`#0D1117`), Dark Surface (`#151A21`), Inset Cards (`#1C232D`), Bright Blue (`#2E6DD6`), Accent (`#58A6FF`), Deep Wavy Scallop (`#1C477D`).
- **Pills & Chips:** Fully rounded pills (`shape = RoundedCornerShape(50.dp)`) for status tags, filter tabs, and action items.
- **AMOLED Safety:** AOD clock screen must maintain pure `#000000` background and execute gentle pixel coordinate shifts (`offsetX`, `offsetY`) every 60 seconds to prevent OLED diode burn-in.
