# GeoLink App — Project Kickoff

## App Name Options

| Name | Vibe | Notes |
|------|------|-------|
| **GeoLink** | Clean, professional | "Link" = linking two locations together |
| **PinPoint** | Precise, tactical | Distance between two pinned points |
| **BearingCalc** | Descriptive, utilitarian | Says exactly what it does |
| **FieldNav** | Tactical/field ops | Fits your EW/defense background |
| **GeoSnap** | Quick, catchy | "Snap" two locations and measure |
| **DualPin** | Descriptive | Two pins → distance & bearing |
| **LocLink** | Short, available | Location + Link |

---

## Environment Setup Checklist

Run these on your actual Ubuntu machine. Copy-paste each block.

### 1. ✅ Java 21 — You have this

### 2. ✅ Node.js 22 — You have this

### 3. ✅ Git — You have this

### 4. ❌ Claude Code — Install it

```bash
npm install -g @anthropic-ai/claude-code
claude --version
```

### 5. ❌ Android Studio + SDK — Install it

Android Studio is the easiest way to get the full SDK, emulator, and build tools:

```bash
# Option A: Snap (easiest, auto-updates)
sudo snap install android-studio --classic

# Option B: Manual download
# Download from https://developer.android.com/studio
# Extract to /opt/android-studio and run ./bin/studio.sh
```

After installing, launch Android Studio once to complete the SDK setup wizard.
It will install: Android SDK, platform-tools (adb), build-tools, and an emulator image.

Then add to your `~/.bashrc` or `~/.zshrc`:

```bash
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/platform-tools
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin
```

Reload: `source ~/.bashrc`

### 6. Verify everything works

```bash
# Android SDK
adb version
sdkmanager --list | head -20

# Claude Code
claude --version

# Create project directory
mkdir -p ~/projects && cd ~/projects
```

---

## Claude Code Kickoff Prompt

Once your environment is set up, create the project folder and run Claude Code:

```bash
mkdir -p ~/projects/geolink && cd ~/projects/geolink
claude
```

Then paste this prompt:

---

```
Create a native Android app called "GeoLinkPinPoint" using Kotlin + Jetpack Compose. This is a field utility app for capturing GPS coordinates from WhatsApp shared locations and calculating distance/bearing between two points.

## Core Architecture

- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 35
- **Build system**: Gradle with Kotlin DSL (build.gradle.kts)
- **UI**: Jetpack Compose with Material 3
- **Database**: Room for measurement history
- **Location**: Google FusedLocationProviderClient
- **No external map SDK** — keep it lightweight

## Feature 1: WhatsApp Geo Intent Handler (CRITICAL)

Register as a handler for `geo:` URI scheme intents. This is the app's primary input method.

AndroidManifest.xml must include:
- Intent filter for `android.intent.action.VIEW` with `geo` scheme
- `android:exported="true"` (required for Android 12+)
- `android:launchMode="singleTask"` on the handler activity

Parse these geo URI formats:
- `geo:lat,lng` — raw coordinates
- `geo:0,0?q=lat,lng(Label)` — labeled place
- `geo:lat,lng?z=zoom` — with zoom level

Workflow:
1. User taps shared location in WhatsApp → Android shows app chooser → user picks GeoLink
2. App receives geo: intent, parses coordinates
3. If no Point A stored → save as Point A
4. If Point A exists → save as Point B, auto-calculate distance & bearing
5. Show results with option to clear/swap/save to history

Handle both `onCreate()` (cold start) and `onNewIntent()` (app already running).

## Feature 2: Manual Coordinate Input

Fallback for when intent handling isn't available:
- Text fields for lat/lng (decimal degrees)
- "Use Current Location" button for each point (requires GPS permission)
- Paste support for common coordinate formats

## Feature 3: GPS Display

- Show current lat/lng/altitude with accuracy indicator
- Use FusedLocationProviderClient with PRIORITY_HIGH_ACCURACY
- Runtime permission handling for ACCESS_FINE_LOCATION
- Update interval: 1 second when screen is active

## Feature 4: Compass

- Use SensorManager with TYPE_ACCELEROMETER + TYPE_MAGNETIC_FIELD
- Compute azimuth via getRotationMatrix() + getOrientation()
- Show heading in degrees + cardinal direction (N/NE/E/SE/S/SW/W/NW)
- Smooth animation for compass needle rotation
- When two points are captured, show bearing TO target point

## Feature 5: Distance & Bearing Calculator

- Haversine formula for distance (or use Location.distanceBetween())
- Forward azimuth for bearing
- Display distance in meters AND km
- Display bearing in degrees + cardinal direction
- Show "from Point A to Point B" with labels if available

## Feature 6: Measurement History

- Room database with entity: id, pointA_lat, pointA_lng, pointA_label, pointB_lat, pointB_lng, pointB_label, distance_m, bearing_deg, timestamp
- List view with most recent first
- Tap to reload a measurement
- Swipe to delete

## UI Structure (Single Activity, Compose Navigation)

Bottom navigation with 3 tabs:
1. **Measure** — Main screen: Point A/B display, distance/bearing result, manual input option
2. **GPS/Compass** — Live GPS + compass display
3. **History** — Saved measurements list

Top bar should show app name and a "Clear Points" action when points are captured.

## Project Structure

```
app/src/main/
├── java/com/geolink/app/
│   ├── MainActivity.kt          // Single activity, handles geo: intents
│   ├── ui/
│   │   ├── theme/Theme.kt
│   │   ├── navigation/AppNavigation.kt
│   │   ├── screens/
│   │   │   ├── MeasureScreen.kt
│   │   │   ├── GpsCompassScreen.kt
│   │   │   └── HistoryScreen.kt
│   │   └── components/
│   │       ├── CompassView.kt
│   │       ├── CoordinateInput.kt
│   │       └── PointCard.kt
│   ├── data/
│   │   ├── MeasurementDatabase.kt
│   │   ├── MeasurementDao.kt
│   │   └── MeasurementEntity.kt
│   ├── sensor/
│   │   └── CompassHelper.kt
│   ├── location/
│   │   └── LocationHelper.kt
│   └── util/
│       ├── GeoUriParser.kt
│       └── GeoCalculations.kt
└── res/
    └── ...
```

## Important Implementation Notes

- Use `viewModel()` with Compose for state management
- Handle configuration changes properly (don't lose captured points on rotation)
- The geo URI parser must be robust — handle edge cases like `geo:0,0?q=...` where 0,0 means "use the query parameter instead"
- Compass should fuse accelerometer + magnetometer (not use deprecated TYPE_ORIENTATION)
- All sensor listeners must be properly registered/unregistered with lifecycle
- Include proper null safety and error handling for malformed URIs
- RTL support should work out of the box with Compose Material 3

Start by creating the full project structure with build.gradle.kts, then implement each feature. Make sure the app compiles and the geo: intent filter is correctly configured in AndroidManifest.xml.
```

---

## Testing the Intent Filter

After building and installing on a device/emulator:

```bash
# Test geo: intent from command line
adb shell am start -a android.intent.action.VIEW -d "geo:32.0853,34.7818"
adb shell am start -a android.intent.action.VIEW -d "geo:0,0?q=32.0853,34.7818(Tel+Aviv)"

# Then open WhatsApp, find a shared location message, tap it
# Your app should appear in the chooser alongside Google Maps
```
