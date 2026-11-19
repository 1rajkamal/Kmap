# KMap 📍🗺️
### Real Map-Based Personal Location Tracker & Offline Wayfinding Application for Android

[![Direct Download APK](https://img.shields.io/badge/Download_APK-Kmap--release.apk-00E676?style=for-the-badge&logo=android&logoColor=black)](https://github.com/1rajkamal/Kmap/raw/main/Kmap-release.apk)
[![GitHub Repository](https://img.shields.io/badge/GitHub-1rajkamal%2FKmap-00E5FF?style=for-the-badge&logo=github&logoColor=black)](https://github.com/1rajkamal/Kmap)

---

### 📥 [Direct Download Latest Release APK](https://github.com/1rajkamal/Kmap/raw/main/Kmap-release.apk)
> **One-Click Download**: Click the link above on your Android phone to download the latest signed `Kmap-release.apk` directly!

---

**KMap** is a native Android application providing a **real, interactive Google Maps-style geographical map experience** designed from the ground up for **100% offline-first operation**. It allows users to bookmark real-world physical locations (such as parked cars, hotel rooms, bags, keys, shoes, bikes, campsites, or meeting points) and navigate back to them anywhere on Earth using live GPS, connecting distance lines, turn/route wayfinding, and live distance tracking—without needing accounts, logins, subscriptions, or paid APIs.

---

## ✨ Core Highlights & Features

- **Real Interactive Geographical Map (Main Home Screen)**:
  - Powered by MapLibre / OpenStreetMap engine (`osmdroid`).
  - Supports pinch-to-zoom, double-tap zoom, one-finger smooth panning, and camera animations.
  - User location indicator (Google Maps-style Royal Blue dot with white ring and accuracy halo).
  - Recenter on user's current GPS position anytime.
  - Reset to North / compass orientation button.
  - Long-press anywhere on the map to drop a marker instantly at exact tapped coordinates!

- **All Saved Objects Rendered Simultaneously**:
  - Every object saved by the user appears at its actual saved GPS coordinates simultaneously.
  - Distinguishable markers and custom teardrop pins with category icons:
    - 🚗 **My Car** / 🅿️ Parking Spot
    - 🔑 **My Keys**
    - 👟 **My Shoes**
    - 🎒 **My Bag**
    - 🏨 **My Hotel**
    - 📍 **Meeting Point**
    - 🏍️ **My Bike**
    - ⛺ **Campsite & Nature**
  - Unlimited saved locations, limited only by device storage.
  - Retrieved directly from the local Room database (never dummy or fake coordinates).

- **Click on Any Marker (Bottom Info Panel)**:
  - Clicking any saved marker opens an elegant bottom information panel:
    - Object icon and name.
    - Category.
    - Live distance calculated continuously from current GPS position.
    - Direction heading (degrees + cardinal, e.g., $45^\circ\text{ NE}$).
    - Two prominent actions: `[ START GOING ]` and `[ DELETE ]`.

- **Live Distance Tracking**:
  - As the user walks away from a saved object, the distance increases dynamically.
  - As the user walks closer, the distance decreases dynamically.
  - Calculated using real-time GPS coordinates with smooth transitions.

- **Dynamic Connecting Distance Line**:
  - Real-time connecting line between **Current Location** and **Selected Object**.
  - Automatically queries free OSRM road walking routes when online.
  - Seamlessly falls back to an emerald direct straight-line connecting vector when offline, clearly labeled as straight-line distance.
  - Moves and updates dynamically as the user walks.

- **Start Going (Dedicated Navigation Mode)**:
  - Fullscreen navigation mode with live HUD:
    - Top navigation banner showing destination name and remaining distance.
    - Real geographical map following user movement.
    - Dynamic connecting route line.
    - `[ Recenter ]` and `[ Stop Going ]` controls.
    - **Arrival Detection**: Displays `DESTINATION REACHED!` celebration banner and provides distinct haptic vibration feedback when arriving within the configured radius ($5\text{m}$, $10\text{m}$, $20\text{m}$, $50\text{m}$).

- **Zero Cloud & 100% Privacy**:
  - No login, no signup, no registration, no cloud tracking, no advertisements.
  - Works completely offline.
  - All data is stored in the local SQLite Room database on the device.
  - Full local backup export to clipboard and import from JSON.

- **Breadcrumb Movement Trail Tracker**:
  - Background foreground service (`BreadcrumbService`) with notification controls.
  - Visual polyline trail of user's path.
  - Reverse wayfinding guide ("Guide Me Back to Start") to retrace steps.

- **Dedicated Settings Screen**:
  - Map & Display settings (light/dark theme, default zoom level, auto-recenter, road routing preference).
  - Units & Navigation (Metric meters/km vs Imperial feet/miles, arrival radius).
  - GPS update frequency (1s Ultra, 3s Balanced, 7s Battery Saver).
  - Privacy & Storage management (export/import backup, clear data).
  - GitHub Updates Guide: Built-in instructions and one-tap button to open the repository and download new APK updates.

---

## 📱 Pre-Built APK Deliverables

The project comes with ready-to-install Android packages directly in the root directory:

| File | Size | Type | Notes |
| :--- | :--- | :--- | :--- |
| **`Kmap-release.apk`** | **11.4 MB** | Release (Signed) | Recommended for installation on any physical Android phone (Android 8.0+ / API 26+). |
| **`Kmap-debug.apk`** | **18.6 MB** | Debug | For development testing, ADB profiling, and inspection. |

### How to Install & Update on Your Android Phone:
1. Tap [Download Kmap-release.apk](https://github.com/1rajkamal/Kmap/raw/main/Kmap-release.apk) on your Android phone browser.
2. Tap the downloaded `.apk` file and select **Install** (allow "Install from this source" if prompted).
3. Open the app, grant Location permission when prompted, and start marking and tracking your places!

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
- **Road Routing**: Free OpenStreetMap OSRM Walking API with offline straight-line fallback
- **Serialization**: Google Gson 2.10.1
- **Architecture**: Clean MVVM (Model-View-ViewModel) + Repository pattern

---

## 📄 License
Open source under the [Apache License 2.0](LICENSE).
