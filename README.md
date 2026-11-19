# Kmap 📍🛰️
### Premium Personal Location Bookmarking & Offline Wayfinding Application for Android

[![Direct Download APK](https://img.shields.io/badge/Download_APK-Kmap--release.apk-00E676?style=for-the-badge&logo=android&logoColor=black)](https://github.com/1rajkamal/Kmap/raw/main/Kmap-release.apk)
[![GitHub Repository](https://img.shields.io/badge/GitHub-1rajkamal%2FKmap-00E5FF?style=for-the-badge&logo=github&logoColor=black)](https://github.com/1rajkamal/Kmap)

---

### 📥 [Direct Download Latest Release APK](https://github.com/1rajkamal/Kmap/raw/main/Kmap-release.apk)
> **One-Click Download**: Click the link above on your Android phone to download the latest signed `Kmap-release.apk` directly!

---

**Kmap** is a native Android application designed from the ground up for **100% offline-first operation**. It allows users to bookmark real-world physical locations (such as parked cars, hotel rooms, campsites, bags, shoes, bikes, or meeting points) and navigate back to them anywhere on Earth using only the device's internal GPS receiver and magnetic compass sensors—without needing cellular data, internet connection, accounts, logins, or paid APIs.

---

## ✨ Core Highlights & Features

- **Custom App Branding**:
  - Official custom **Kmap** identity, launcher icons (all densities: MDPI to XXXHDPI), and in-app branding.
- **Zero Cloud & True Privacy**:
  - No login, no signup, no registration, no analytics, no subscriptions, no ads.
  - Open the app and use it immediately.
  - All location bookmarks and breadcrumb trails remain strictly stored on the device's encrypted Room SQLite database.
- **Hardware-Direct Navigation**:
  - Combines Android `FusedLocationProviderClient` with automatic fallback to standard `LocationManager.GPS_PROVIDER` for satellite-direct positioning.
  - `CompassSensorManager` utilizes `TYPE_ROTATION_VECTOR` (with accelerometer + magnetometer fallback) and a circular trigonometric low-pass filter to eliminate needle jitter across the $359^\circ \leftrightarrow 0^\circ$ wrap.
- **Animated Sci-Fi Radar**:
  - Concentric distance range rings with degree tick marks and cardinal headings ($N, E, S, W$).
  - Rotating animated radar sweep wedge with gradient tail.
  - High-precision directional arrow pointing toward the selected target relative to device heading.
  - Target blip indicator placed at exact relative azimuth and scaled distance.
  - Dynamic live distance display with primary (Meters/Feet) and secondary (Kilometers/Miles) readouts.
  - Live elevation delta ($+X\text{m higher}$ or $-Y\text{m lower}$).
  - Arrival celebration haptics (custom vibration pattern) when within configurable arrival radius ($5\text{m}$, $10\text{m}$, $20\text{m}$, $50\text{m}$).
  - Compass recalibration dialog with interactive Figure-8 instructions.
- **Interactive Home Dashboard**:
  - Welcome greeting: *"Never Lose Your Way"* with custom Kmap logo badge.
  - Live animated GPS status badge with satellite lock state and precision in meters/feet.
  - Large pulsing circular **RADAR** launch button.
  - Quick action: **Mark Location** with one-tap presets (*"My Car"*, *"Parked Spot"*, *"Hotel"*, *"Baggage"*, *"Campsite"*, *"Bike"*, *"Meeting Spot"*).
  - *"What do you want to locate?"* interactive destination selector showing live distances to all saved places.
- **My Places Library**:
  - Searchable, filterable by category (*Vehicles, Stays, Personal Gear, Outdoor, Meetup, Custom*).
  - Sort by Nearest First, Newest, Alphabetical, or Favorites.
  - One-tap navigation to Radar.
  - Full local JSON Backup **Export to Clipboard** and **Import from JSON**.
- **Optional OpenStreetMap Integration**:
  - Built with `osmdroid` for free, open-source mapping.
  - Renders all saved location markers and route vector to target.
  - Transparent offline tile notice: honestly explains that previously viewed tiles are cached in device storage, while basic radar and direct-distance navigation work everywhere with zero maps needed.
- **Breadcrumb Trail Tracker**:
  - Background foreground service (`BreadcrumbService`) with notification controls.
  - Records GPS points at configurable intervals without draining battery.
  - Custom canvas polyline renderer visualizing the exact path walked.
  - **Reverse Wayfinding Guide**: *"Guide Me Back to Start"* retraces footsteps back to departure point.
  - Pause, Resume, Stop, and Clear controls.
- **Dedicated 7-Category Settings Screen**:
  1. *Appearance*: Dark, Light, System default; Custom Accent Colors (*Emerald, Cyan, Amber, Monochrome*); Dynamic color.
  2. *Radar Settings*: Animation sweep speed (*Fast, Normal, Relaxed, Static*); Radar glow; Distance units (*Metric vs Imperial*); Arrival detection radius.
  3. *Location Settings*: GPS polling frequency (*Ultra 1s, Balanced 3s, Eco 7s*); System permission manager.
  4. *Notifications*: Arrival vibration; GPS lost warnings; Nearby reminders.
  5. *Privacy & Storage*: 100% local guarantee; Clear trail history; Export/Import backup; Delete all data with confirmation.
  6. *Battery & Performance*: Eco Radar mode; Background tracking intervals.
  7. *About*: Version 1.0.0, Open-source licenses, Privacy policy.

---

## 📱 Pre-Built APK Deliverables

The project comes with ready-to-install Android packages directly in the root directory:

| File | Size | Type | Notes |
| :--- | :--- | :--- | :--- |
| **`Kmap-release.apk`** | **11.4 MB** | Release (Signed) | Recommended for installation on any physical Android phone (Android 8.0+ / API 26+). |
| **`Kmap-debug.apk`** | **18.6 MB** | Debug | For development testing, ADB profiling, and inspection. |

### How to Install on Your Android Phone:
1. Connect your phone via USB or send `Kmap-release.apk` to your phone (via USB cable, Local Send, or Google Drive).
2. Tap the `.apk` file on your phone and select **Install**.
3. Grant Location permission when prompted.
4. Step outside or near a window to acquire GPS satellites and mark your first location!

---

## 🛠️ Technology Stack & Architecture

- **Language**: Kotlin 1.9.0
- **UI Framework**: Jetpack Compose (BOM 2023.10.01 / Material 3)
- **Local Persistence**: Room SQLite 2.6.1 (`LocationDao`, `BreadcrumbDao`, `AppDatabase`)
- **Settings Store**: `SharedPreferences` + reactive Kotlin `StateFlow`
- **Location & Sensors**:
  - Android `LocationManager` + Google Play Services `FusedLocationProviderClient`
  - Android `SensorManager` (`TYPE_ROTATION_VECTOR`, `TYPE_ACCELEROMETER`, `TYPE_MAGNETIC_FIELD`)
- **Mapping**: OpenStreetMap Android (`org.osmdroid:osmdroid-android:6.1.18`)
- **Serialization**: Google Gson 2.10.1
- **Architecture**: Clean MVVM (Model-View-ViewModel) + Repository pattern

---

## 🔨 How to Build from Command Line

```powershell
# Set environment variables for your local JDK & SDK
$env:JAVA_HOME = "C:\Program Files\Java\jdk-23"
$env:ANDROID_HOME = "C:\Users\Raj Kamal\AppData\Local\Android\Sdk"
$env:GRADLE_USER_HOME = "C:\Users\Raj Kamal\.gradle"

# Build debug APK
.\gradlew.bat assembleDebug

# Build production release APK
.\gradlew.bat assembleRelease
```
