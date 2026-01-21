# Developer Setup Guide

Follow these steps to set up the LiveChat development environment on your local machine.

## Prerequisites

- **JDK 17**: Required for Android and Gradle builds.
- **Android Studio**: Latest version (Ladybug or newer recommended).
- **Xcode**: Required for iOS development (macOS only).
- **Firebase CLI**: Required for managing backend functions and emulators.

## Initial Setup

1.  **Clone the repository**:
    ```bash
    git clone https://github.com/edufelip/livechat.git
    cd livechat
    ```
2.  **Firebase Configuration**:
    - **Android**: Place Firebase config files in flavor-specific directories:
      - Dev: `app/src/dev/google-services.json`
      - Prod: `app/src/prod/google-services.json`
    - **iOS**: Place Firebase config files in flavor-specific directories:
      - Dev: `iosApp/config/dev/GoogleService-Info.plist`
      - Prod: `iosApp/config/prod/GoogleService-Info.plist`
    - See [Build Configuration](build_configuration.md) and [FIREBASE_CONFIG_README.md](../../FIREBASE_CONFIG_README.md) for details.
3.  **Install Git Hooks**:
    ```bash
    chmod +x .githooks/*
    git config core.hooksPath .githooks
    ```

## Local Development Flow

### Running the Firebase Emulators
To develop without hitting the production Firebase instance:
```bash
firebase emulators:start
```

### Running Android
Open the project in Android Studio:
1. Select the desired build variant from **Build Variants** panel (e.g., `devDebug`, `prodRelease`)
2. Run the `app` configuration

See [Build Configuration](build_configuration.md) for details on available variants.

### Running iOS
1. **Complete iOS build configuration** (one-time setup):
   - Follow instructions in [IOS_SETUP_INSTRUCTIONS.md](../../IOS_SETUP_INSTRUCTIONS.md)
   - Configure Xcode build settings and Firebase copy script
2. Open `iosApp/iosApp.xcworkspace` in Xcode and run, or use the provided scripts:
   ```bash
   ./scripts/run_ios.sh
   ```

See [Build Configuration](build_configuration.md) for details on iOS flavors.

## Build Tools & Scripts

- `./gradlew spotlessApply`: Formats the code according to the project style.
- `./gradlew assembleDevDebug`: Builds dev debug variant (fast, for development).
- `./gradlew assembleProdRelease`: Builds production release variant (optimized, for Play Store).
- `./scripts/rebuild_ios.sh`: Performs a clean build of the iOS framework.
- `tools/countrydata`: Run this tool if you need to update the country list.

See [Build Configuration](build_configuration.md) for all available build commands and variants.
