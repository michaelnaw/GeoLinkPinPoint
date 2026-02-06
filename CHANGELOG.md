# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1.0.0] - 2025-06-01

### Added
- `geo:` URI intent handler for receiving shared locations from WhatsApp and other apps
- Support for `geo:lat,lng`, `geo:0,0?q=lat,lng(Label)`, and parameterized geo URIs
- Manual coordinate input with decimal degree fields
- "Use Current Location" button using FusedLocationProviderClient
- Haversine distance calculation with m/km formatting
- Forward bearing calculation with cardinal direction display
- Live GPS display showing latitude, longitude, altitude, and accuracy
- Compass using accelerometer + magnetometer sensor fusion with smooth animation
- Bearing-to-target overlay on compass when two points are captured
- Measurement history stored in Room database (with migration v1 to v2 for tag support)
- Swipe-to-delete on history items
- Tap history item to reload measurement on the Measure screen
- CSV export of measurement history via Android share sheet
- 3-tab bottom navigation: Measure, GPS/Compass, History
- Material 3 theming with Jetpack Compose
- MeasurementRepository layer with ViewModelProvider.Factory for testability
- Unit tests for GeoUriParser, GeoCalculations, and MeasurementRepository
- Instrumented tests for Room database operations and migration
- All UI strings extracted to strings.xml for localization readiness
- Error handling: CSV export try-catch, GeoUriParser debug logging, permission denial snackbar feedback
