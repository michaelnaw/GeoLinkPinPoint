# Contributing to GeoLinkPinPoint

## Prerequisites

- JDK 17
- Android SDK 35 (install via Android Studio or `sdkmanager`)
- Android 8.0+ device or emulator for testing

## Building

```bash
# Debug build
./gradlew assembleDebug

# Release build (R8 minified)
./gradlew assembleRelease

# Install on device
adb install app/build/outputs/apk/debug/app-debug.apk
```

## Testing

```bash
# Unit tests
./gradlew test

# Instrumented tests (requires connected device/emulator)
./gradlew connectedAndroidTest

# Test geo: intent handling
adb shell am start -a android.intent.action.VIEW -d "geo:32.0853,34.7818"
```

## Pull Request Guidelines

1. **One feature per PR** — keep changes focused and reviewable
2. **Include tests** — add unit tests for new logic; update existing tests if behavior changes
3. **Describe the change** — explain what you changed and why in the PR description
4. **Keep it building** — ensure `./gradlew assembleDebug` and `./gradlew test` pass before submitting

## Commit Convention

Use [Conventional Commits](https://www.conventionalcommits.org/):

- `feat:` — new feature
- `fix:` — bug fix
- `docs:` — documentation changes
- `refactor:` — code restructuring without behavior change
- `test:` — adding or updating tests
- `chore:` — build config, CI, dependencies

Examples:
```
feat: add dark theme support
fix: handle geo URI with missing query parameter
test: add bearing calculation edge case tests
docs: update README with new build instructions
```

## Architecture Notes

- **Single module** — `:app` only, no multi-module
- **Single shared ViewModel** — `MainViewModel` handles all screens; no ViewModel split
- **No DI framework** — manual dependency injection via `MainViewModelFactory`
- **KSP only** — no KAPT; Room uses KSP for annotation processing
- **Destructive Room migration** is acceptable during development

## Branch Protection

The `main` branch should have:
- Required pull request reviews before merge
- Required CI checks passing (build + tests)
