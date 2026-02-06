# GeoLinkPinPoint

Native Android field utility for capturing GPS coordinates from WhatsApp shared locations and calculating distance/bearing between two points.

## Features

- **WhatsApp Integration** — Register as a `geo:` URI handler. Tap a shared location in WhatsApp, pick GeoLinkPinPoint, and coordinates auto-fill as Point A or B
- **Distance & Bearing** — Haversine distance (m/km) and forward bearing (degrees + cardinal direction) between two points
- **Manual Coordinate Input** — Enter lat/lng manually or use "Use Current Location" with GPS
- **Live GPS Display** — Current position with latitude, longitude, altitude, and accuracy
- **Compass** — Accelerometer + magnetometer sensor fusion with smooth animation. Shows bearing-to-target when two points are captured
- **Measurement History** — Save measurements to a Room database. Tap to reload, swipe to delete
- **CSV Export** — Share measurement history as a CSV file via Android's share sheet

## Screenshots

| Measure | GPS & Compass | History |
|---------|---------------|---------|
| ![Measure](screenshots/measure.png) | ![GPS & Compass](screenshots/gps-compass.png) | ![History](screenshots/history.png) |

> To add screenshots, capture them with `adb shell screencap` and place in the `screenshots/` directory.

## Requirements

- Android 8.0+ (API 26)
- JDK 17
- Android SDK 35

## Build

```bash
# Debug build
./gradlew assembleDebug

# Release build (R8 minified)
./gradlew assembleRelease

# Install on connected device
adb install app/build/outputs/apk/debug/app-debug.apk

# Run unit tests
./gradlew test

# Run instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest
```

## How It Works

GeoLinkPinPoint registers as an Android intent handler for the `geo:` URI scheme. This means any app that shares a location as a `geo:` link (including WhatsApp) will offer GeoLinkPinPoint as a target.

**Typical workflow:**

1. Open WhatsApp and find a shared location message
2. Tap the location — Android shows an app chooser
3. Select GeoLinkPinPoint
4. The coordinates fill in as **Point A**
5. Repeat with a second location — it fills in as **Point B**
6. Distance and bearing are calculated automatically
7. Optionally save the measurement to history

**Supported `geo:` URI formats:**

| Format | Example |
|--------|---------|
| Raw coordinates | `geo:32.0853,34.7818` |
| Labeled place (0,0 sentinel) | `geo:0,0?q=32.7940,34.9896(Haifa)` |
| With parameters | `geo:32.0853,34.7818;u=35` |

**Testing with adb:**

```bash
adb shell am start -a android.intent.action.VIEW -d "geo:32.0853,34.7818"
adb shell am start -a android.intent.action.VIEW -d "'geo:0,0?q=32.7940,34.9896(Haifa)'"
```

## Project Structure

```
app/src/main/java/com/geolinkpinpoint/
├── MainActivity.kt                 # Single activity, geo: intent handler
├── data/
│   ├── MeasurementDatabase.kt      # Room database (v2, with migration)
│   ├── MeasurementDao.kt           # Data access object
│   ├── MeasurementEntity.kt        # Database entity
│   └── MeasurementRepository.kt    # Repository wrapping DAO
├── location/
│   └── LocationHelper.kt           # FusedLocationProviderClient wrapper
├── sensor/
│   └── CompassHelper.kt            # Accelerometer + magnetometer fusion
├── ui/
│   ├── MainViewModel.kt            # Shared ViewModel (AndroidViewModel)
│   ├── MainViewModelFactory.kt     # ViewModelProvider.Factory
│   ├── components/
│   │   ├── CompassView.kt          # Custom compass Canvas drawing
│   │   ├── CoordinateInput.kt      # Lat/lng input fields
│   │   └── PointCard.kt            # Point A/B display card
│   ├── navigation/
│   │   └── AppNavigation.kt        # 3-tab bottom navigation
│   ├── screens/
│   │   ├── MeasureScreen.kt        # Main measurement screen
│   │   ├── GpsCompassScreen.kt     # GPS + compass display
│   │   └── HistoryScreen.kt        # Saved measurements list
│   └── theme/
│       └── Theme.kt                # Material 3 theme
└── util/
    ├── GeoUriParser.kt             # geo: URI parsing with sentinel handling
    └── GeoCalculations.kt          # Haversine distance, bearing, formatting
```

## Tech Stack

| Component | Version |
|-----------|---------|
| Kotlin | 2.1.0 |
| Jetpack Compose BOM | 2025.01.01 |
| Room (KSP) | 2.7.0 |
| Navigation Compose | 2.8.5 |
| Play Services Location | 21.3.0 |
| Min SDK | 26 (Android 8.0) |
| Target SDK | 35 |
| JVM Target | 17 |

## License

This project is licensed under the MIT License. See [LICENSE](LICENSE) for details.
