# GAA Compass
**A Training and Wellness Application for GAA Athletes**

Kevin Burke | 22355634 | BSc Computer Systems | University of Limerick | 2025/2026

---

## Project Overview

GAA Compass is a native Android application designed to improve training management and wellness monitoring for Gaelic Athletic Association (GAA) athletes. It integrates GAA-specific session logging, weekly training planning, daily wellness check-ins and evidence-based recovery prompts into a single platform built specifically for GAA athletes.

The app was developed as a Final Year Project using a three-phase user-centred design methodology: an online survey of 99 GAA athletes (Phase 1), two rounds of Figma prototype testing with 16 participants (Phase 2) and Android development and usability evaluation (Phase 3).

---

## Features

- **Free Taking Session Logger** - Interactive pitch diagram with tappable scoring zones; records attempts, scores and accuracy in real time
- **Weekly Training Planner** - Plan gym, pitch, match, recovery and rest days across the week with a weighted load balance indicator
- **Daily Wellness Check-in** - Rate sleep, energy, soreness and stress in ~20 seconds; generates a composite recovery score and actionable prompt
- **Progress & Analytics** - View free-taking accuracy trends, session history and recovery score over time
- **County Colours Theme Selector** - Personalise the app to your GAA county from all 32 county colour schemes
- **Home Dashboard** - At-a-glance streak, accuracy and recovery score cards with single-tap access to all core features

---

## Requirements

| Requirement | Detail |
|---|---|
| Android Studio | Meerkat (2024.3.1) or later - required for AGP 9.0.0 |
| Android Gradle Plugin | 9.0.0 (included in project) |
| Java | JDK 11 or later |
| Minimum Android SDK | API 24 (Android 7.0 Nougat) |
| Target Android SDK | API 36 |
| Compile SDK | 36 |

> **Important:** This project uses AGP 9.0.0 which requires Android Studio Meerkat or newer. Older versions of Android Studio will fail to sync. You can download the latest Android Studio at [developer.android.com/studio](https://developer.android.com/studio).

---

## How to Open and Run the Project

### Step 1 - Clone or Extract

If you have the zip file, extract it to a location of your choice. You should have a folder called `GAACompass-main` containing the full Android Studio project.

### Step 2 - Open in Android Studio

1. Open Android Studio
2. Select **File → Open**
3. Navigate to and select the `GAACompass-main` folder
4. Click **OK**

### Step 3 - Sync Gradle

Android Studio will automatically prompt you to sync Gradle when the project opens. Click **Sync Now** in the notification bar if it appears. This downloads all required dependencies automatically, no manual installation needed.

If sync fails, go to **File → Sync Project with Gradle Files** and try again. Make sure you have an active internet connection for the first sync.

### Step 4 - Run the App

**Option A - Physical Android Device (Recommended)**
1. Enable Developer Options on your Android device (Settings → About Phone → tap Build Number 7 times)
2. Enable USB Debugging in Developer Options
3. Connect your device via USB
4. Select your device from the device dropdown in the Android Studio toolbar
5. Click the **Run** button (green play icon) or press `Shift + F10`

**Option B - Android Emulator**
1. Open **Device Manager** in Android Studio (right sidebar or Tools → Device Manager)
2. Click **Create Device**
3. Select a device definition (e.g. Pixel 6) and a system image with API level 24 or above
4. Click **Finish** to create the AVD
5. Select the emulator from the device dropdown and click **Run**

> The emulator works fine for all features. The interactive pitch diagram, weekly planner, daily check-in and county colours selector all function correctly in the emulator.

---

## Project Structure

```
GAACompass-main/
├── app/
│   └── src/
│       └── main/
│           ├── java/com/example/gaacompass/
│           │   ├── MainActivity.java             # Home dashboard
│           │   ├── LogFreesActivity.java         # Free taking session logger
│           │   ├── GaaPitchView.java             # Custom interactive pitch diagram view
│           │   ├── FreeAttempt.java              # Data model for a single attempt
│           │   ├── WeeklyPlannerActivity.java    # Weekly training planner
│           │   ├── DailyCheckInActivity.java     # Wellness check-in and recovery prompt
│           │   ├── ProgressActivity.java         # Progress and analytics screen
│           │   ├── SettingsActivity.java         # County colours theme selector
│           │   ├── ThemePrefs.java               # SharedPreferences helper for theme
│           │   └── AnalyticsStore.java           # SharedPreferences helper for session data
│           ├── res/
│           │   ├── layout/                       # XML screen layouts
│           │   ├── drawable/                     # County colour gradients and UI backgrounds
│           │   ├── values/                       # Colours, strings, dimensions, themes
│           │   └── ...
│           └── AndroidManifest.xml
├── build.gradle.kts                              # Project-level Gradle config
├── app/build.gradle.kts                          # App-level Gradle config and dependencies
└── gradle/libs.versions.toml                     # Dependency version catalogue
```

---

## Architecture

The application follows the **MVVM (Model-View-ViewModel)** architecture pattern as recommended by Google for Android development. Data persistence is handled through **SharedPreferences** for lightweight structured storage of session logs, wellness check-ins, training plans and user preferences. The app operates fully offline with no network connection required.

**Key architectural decisions:**
- No external API keys or network calls required, the app is fully self-contained
- All data is stored locally on the device using SharedPreferences via `AnalyticsStore` and `ThemePrefs`
- The custom `GaaPitchView` is a fully drawn canvas-based view with tappable pitch zones
- County colour themes are applied at runtime via a custom theme system in `ThemePrefs`

---

## Dependencies

All dependencies are standard AndroidX libraries, no third-party SDKs or API keys are required.

| Library | Version | Purpose |
|---|---|---|
| androidx.appcompat | 1.6.1 | Backwards compatibility |
| com.google.android.material | 1.10.0 | Material Design components |
| androidx.activity | 1.8.0 | Activity result APIs |
| androidx.constraintlayout | 2.1.4 | Flexible XML layouts |

---

## Testing the Core Features

Once the app is running, here is a suggested walkthrough to explore all core features:

1. **Home Screen** - View the dashboard with streak, accuracy and recovery score cards. Tap any of the four navigation tiles.

2. **Log a Free Taking Session** - Tap "Log Frees" → tap zones on the interactive pitch diagram to record attempts → mark each as a Score or Miss → tap "End Session" to save.

3. **Plan Your Training Week** - Tap "Weekly Plan" → tap the activity icons (Rest, Recovery, Gym, Pitch, Match) for each day of the week → observe the weekly balance feedback at the top.

4. **Complete a Daily Check-in** - Tap "Daily Check-in" → use the sliders to rate sleep, energy, soreness, stress and yesterday's RPE → tap "Save Check-in" to see your recovery score and recommendation.

5. **View Progress** - Tap "Progress" from the home screen to view accuracy trends and session history.

6. **Change County Colours** - Tap the settings icon on the home screen → select your county from the grid to apply its colours across the whole app.

---

## Notes for Examiners

- There are no login screens or accounts, the app opens directly to the home dashboard on first launch
- All data entered during testing persists locally on the device or emulator until the app is uninstalled or data is cleared
- The Progress screen populates with data as you log free-taking sessions and complete daily check-ins
- The county colours theme updates the header gradient and primary colour accent across all screens immediately on selection
- `local.properties` is excluded from the project (as per `.gitignore`) and will be generated automatically by Android Studio for your local SDK path

---

## Contact

Kevin Burke - 22355634@studentmail.ul.ie

Supervisor: Dr Lilian Motti Ader - lilian.motti@ul.ie
