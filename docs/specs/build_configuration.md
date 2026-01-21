# Build Configuration & Flavors

This document describes the build configuration structure for LiveChat, including build types, product flavors, and how to build different variants for development and production.

## Overview

LiveChat uses a multi-flavor build system to support different environments and configurations:

- **Build Types**: `debug` and `release`
- **Product Flavors**: `dev` and `prod`
- **Build Variants**: Combinations of build types and flavors (e.g., `devDebug`, `prodRelease`)

This structure allows you to:
- Install development and production builds simultaneously on the same device
- Use different Firebase projects for dev and prod environments
- Apply code optimization and obfuscation only to production builds
- Easily distinguish between environments during development

## Android Build Configuration

### Product Flavors

#### Dev Flavor
- **Application ID**: `com.edufelip.livechat.dev`
- **App Name**: "LiveChat Dev"
- **Use Case**: Development, testing with dev backend
- **Firebase Config**: `app/src/dev/google-services.json`

#### Prod Flavor
- **Application ID**: `com.edufelip.livechat`
- **App Name**: "LiveChat"
- **Use Case**: Production releases (Google Play Store)
- **Firebase Config**: `app/src/prod/google-services.json`

### Build Types

#### Debug
- **Debuggable**: Yes
- **Minification**: Disabled
- **Obfuscation**: Disabled
- **Use Case**: Local development, debugging

#### Release
- **Debuggable**: No
- **Minification**: Enabled (R8/ProGuard)
- **Resource Shrinking**: Enabled
- **Obfuscation**: Enabled
- **ProGuard Rules**: `app/proguard-rules.pro`
- **Use Case**: Distribution (Play Store, internal testing)

### Android Build Variants

| Variant | Application ID | App Name | Optimized | Use Case |
|---------|---------------|----------|-----------|----------|
| `devDebug` | `com.edufelip.livechat.dev` | LiveChat Dev | No | Local development |
| `devRelease` | `com.edufelip.livechat.dev` | LiveChat Dev | Yes | Internal testing with dev backend |
| `prodDebug` | `com.edufelip.livechat` | LiveChat | No | Production debugging |
| `prodRelease` | `com.edufelip.livechat` | LiveChat | Yes | **Google Play Store** |

### Building Android Variants

```bash
# Build APKs
./gradlew assembleDevDebug          # Dev + Debug
./gradlew assembleDevRelease        # Dev + Release (minified)
./gradlew assembleProdDebug         # Prod + Debug
./gradlew assembleProdRelease       # Prod + Release (minified)

# Build AAB bundles (for Play Store)
./gradlew bundleDevDebug
./gradlew bundleDevRelease
./gradlew bundleProdDebug
./gradlew bundleProdRelease         # Primary release bundle

# Install on connected device
./gradlew installDevDebug
./gradlew installDevRelease
./gradlew installProdDebug
./gradlew installProdRelease

# Clean build
./gradlew clean
```

### Selecting Build Variant in Android Studio

1. Click **Build Variants** in the left sidebar (or **View → Tool Windows → Build Variants**)
2. Select the desired variant from the dropdown (e.g., `devDebug`, `prodRelease`)
3. Sync and build the project

## iOS Build Configuration

### Configuration Structure

iOS uses Xcode build configurations and user-defined settings to manage flavors:

- **Debug Configuration**: Uses `dev` flavor
- **Release Configuration**: Uses `prod` flavor

### Bundle Identifiers

- **Dev**: `com.edufelip.livechat.dev`
- **Prod**: `com.edufelip.livechat`

### App Display Names

- **Dev**: "LiveChat Dev"
- **Prod**: "LiveChat"

### Firebase Configuration

Firebase config files are stored in flavor-specific directories:
- **Dev**: `iosApp/config/dev/GoogleService-Info.plist`
- **Prod**: `iosApp/config/prod/GoogleService-Info.plist`

A build phase script automatically copies the correct configuration based on the active build configuration.

### Xcode Setup

The iOS project requires one-time Xcode configuration. Follow the detailed instructions in:

📄 **[IOS_SETUP_INSTRUCTIONS.md](../IOS_SETUP_INSTRUCTIONS.md)**

Two setup options are available:
- **Option 1 (Recommended)**: User-defined build settings
- **Option 2**: Duplicate configurations

### Building iOS Variants

#### Using Xcode

1. Open `iosApp/iosApp.xcworkspace`
2. Select the scheme: **iosApp**
3. Select the configuration:
   - **Debug** for dev builds
   - **Release** for prod builds
4. Build: **Product → Build** (⌘B)
5. Archive: **Product → Archive** (for distribution)

#### Using Command Line

```bash
# Build Compose framework first
./gradlew :app:assembleLiveChatComposeDebugXCFramework    # Debug
./gradlew :app:assembleLiveChatComposeReleaseXCFramework  # Release

# Then use xcodebuild
cd iosApp

# Dev (Debug)
xcodebuild \
  -workspace iosApp.xcworkspace \
  -scheme iosApp \
  -configuration Debug \
  -sdk iphonesimulator \
  -destination 'platform=iOS Simulator,name=iPhone 15'

# Prod (Release) - for archive
xcodebuild \
  -workspace iosApp.xcworkspace \
  -scheme iosApp \
  -configuration Release \
  -sdk iphoneos \
  -archivePath build/iosApp.xcarchive \
  archive
```

## Firebase Configuration

### File Locations

#### Android
- Dev: `app/src/dev/google-services.json`
- Prod: `app/src/prod/google-services.json`

#### iOS
- Dev: `iosApp/config/dev/GoogleService-Info.plist`
- Prod: `iosApp/config/prod/GoogleService-Info.plist`

### Setting Up Firebase Files

1. **Download Firebase Config Files**
   - Go to [Firebase Console](https://console.firebase.google.com/)
   - Select your project (or create separate dev/prod projects)
   - Download `google-services.json` (Android) and `GoogleService-Info.plist` (iOS)

2. **Place Files in Correct Locations**
   ```bash
   # Android
   cp ~/Downloads/google-services.json app/src/dev/
   cp ~/Downloads/google-services.json app/src/prod/  # or different prod config

   # iOS
   cp ~/Downloads/GoogleService-Info.plist iosApp/config/dev/
   cp ~/Downloads/GoogleService-Info.plist iosApp/config/prod/  # or different prod config
   ```

3. **Verify Files Are Ignored**
   - These files are in `.gitignore` and should NOT be committed
   - Use CI/CD secrets for automated builds

📄 See **[FIREBASE_CONFIG_README.md](../FIREBASE_CONFIG_README.md)** for more details

## ProGuard Configuration

### Overview

Release builds use R8/ProGuard for code optimization and obfuscation. The configuration file is located at:

📄 `app/proguard-rules.pro`

### What's Included

The ProGuard rules file includes keep rules for:
- ✅ Kotlin standard library and coroutines
- ✅ Kotlin serialization
- ✅ Firebase (Auth, Firestore, Storage, Functions, Analytics)
- ✅ Jetpack Compose
- ✅ Koin dependency injection
- ✅ kotlinx.datetime
- ✅ AndroidX libraries
- ✅ Kotlin Multiplatform common code

### Adding Custom Rules

If you add new libraries or encounter issues with release builds, you may need to add custom ProGuard rules:

```proguard
# Keep specific class
-keep class com.example.MyClass { *; }

# Keep all classes in a package
-keep class com.example.package.** { *; }

# Keep data classes used with Firebase
-keep class com.edufelip.livechat.shared.data.model.** { *; }
```

### Troubleshooting ProGuard Issues

If your release build crashes but debug works:

1. **Check the mapping file**: `app/build/outputs/mapping/prodRelease/mapping.txt`
2. **Add keep rules** for classes that are accessed via reflection
3. **Test incrementally**: Build with `minifyEnabled = false` first, then re-enable
4. **Use `-printusage`** to see what's being removed

## CI/CD Configuration

### GitHub Actions Workflows

#### Android Release (`android-release.yml`)
- **Trigger**: Push to `main` or manual dispatch
- **Builds**: `prodRelease` variant
- **Output**: AAB bundle
- **Distribution**: Google Play Console (internal/alpha/beta)

#### iOS Release (`ios-release.yml`)
- **Trigger**: Push to `main` or manual dispatch
- **Builds**: Release configuration (prod flavor)
- **Output**: IPA file
- **Distribution**: TestFlight

### Required Secrets

#### Android
- `ANDROID_RELEASE_KEYSTORE_BASE64`: Release keystore file (base64 encoded)
- `ANDROID_RELEASE_STORE_PASSWORD`: Keystore password
- `ANDROID_RELEASE_KEY_ALIAS`: Key alias
- `ANDROID_RELEASE_KEY_PASSWORD`: Key password
- `ANDROID_GOOGLE_SERVICES_JSON_BASE64`: Prod Firebase config (base64 encoded)
- `PLAY_SERVICE_ACCOUNT_JSON`: Google Play API credentials

#### iOS
- `IOS_DIST_CERT_P12_BASE64`: Distribution certificate (base64 encoded)
- `IOS_DIST_CERT_PASSWORD`: Certificate password
- `IOS_PROFILE_BASE64`: Provisioning profile (base64 encoded)
- `IOS_TEAM_ID`: Apple Developer Team ID
- `IOS_KEYCHAIN_PASSWORD`: Temporary keychain password
- `IOS_GOOGLE_SERVICE_INFO_PLIST_BASE64`: Prod Firebase config (base64 encoded)
- `APP_STORE_CONNECT_API_KEY_ID`: App Store Connect API key ID
- `APP_STORE_CONNECT_API_KEY_ISSUER_ID`: API issuer ID
- `APP_STORE_CONNECT_API_KEY_CONTENT`: API key content (base64 encoded)

### Building Dev Releases in CI (Optional)

To add dev release builds to CI/CD:

1. Add dev Firebase config secrets
2. Create new workflow files or add jobs:
   - `android-dev-release.yml`
   - `ios-dev-release.yml`
3. Build dev variants instead of prod
4. Distribute to Firebase App Distribution or TestFlight

## BuildConfig Fields

The following fields are available in your code:

### Android

```kotlin
// In Android code
import com.edufelip.livechat.BuildConfig

val isDev = BuildConfig.IS_DEV        // Boolean: true for dev, false for prod
val flavor = BuildConfig.FLAVOR_NAME  // String: "dev" or "prod"
val isDebug = BuildConfig.DEBUG       // Boolean: true for debug builds
```

### Shared Code

To access build configuration in shared KMM code, you can create an expect/actual pattern:

```kotlin
// commonMain
expect object BuildConfig {
    val IS_DEV: Boolean
    val FLAVOR: String
}

// androidMain
actual object BuildConfig {
    actual val IS_DEV: Boolean = com.edufelip.livechat.BuildConfig.IS_DEV
    actual val FLAVOR: String = com.edufelip.livechat.BuildConfig.FLAVOR_NAME
}

// iosMain
actual object BuildConfig {
    actual val IS_DEV: Boolean = // Read from Info.plist or use compiler flag
    actual val FLAVOR: String = // "dev" or "prod"
}
```

## Versioning

### Android

Version codes are automatically incremented in CI/CD based on GitHub run number:

```bash
./gradlew assembleProdRelease \
  -PversionCode="$GITHUB_RUN_NUMBER" \
  -PversionName="1.0.$GITHUB_RUN_NUMBER"
```

For local builds, defaults are used (see `app/build.gradle.kts`).

### iOS

Build numbers are bumped in CI/CD:

```bash
/usr/libexec/PlistBuddy -c "Set :CFBundleVersion $GITHUB_RUN_NUMBER" iosApp/iosApp/Info.plist
```

## Best Practices

### Development Workflow

1. **Use `devDebug` for daily development**
   - Fast build times (no optimization)
   - Debuggable
   - Uses dev backend

2. **Test with `devRelease` before merging**
   - Catches ProGuard issues early
   - Tests optimized build performance
   - Still uses dev backend

3. **Only build `prodRelease` for distribution**
   - Final testing before release
   - Upload to Play Store / TestFlight

### Testing Releases

Before deploying `prodRelease`:

1. ✅ Test on multiple devices
2. ✅ Verify Firebase connection works
3. ✅ Check ProGuard hasn't broken functionality
4. ✅ Test user flows end-to-end
5. ✅ Verify app name and icon are correct

### Debugging Release Builds

If you need to debug a release build:

1. Temporarily set `isDebuggable = true` in release build type
2. Or use `prodDebug` variant (no obfuscation but uses prod backend)
3. Check ProGuard mapping file to deobfuscate crash logs

## Troubleshooting

### Build Fails: "google-services.json not found"

**Solution**: Ensure Firebase config files exist:
```bash
ls app/src/dev/google-services.json
ls app/src/prod/google-services.json
```

### iOS: "GoogleService-Info.plist not found"

**Solution**: 
1. Check files exist: `ls iosApp/config/dev/GoogleService-Info.plist`
2. Verify build phase script is added in Xcode
3. Check Xcode build logs for script errors

### Release Build Crashes

**Cause**: ProGuard may have removed necessary code

**Solution**:
1. Check crash logs and mapping file
2. Add keep rules to `proguard-rules.pro`
3. Test with `minifyEnabled = false` to confirm

### Wrong Flavor Building

**Android**: Check Build Variants panel in Android Studio

**iOS**: 
- Check active scheme and configuration in Xcode
- Verify build settings are configured correctly

### Both Apps Have Same Name

**Android**: Check `build.gradle.kts` for correct `resValue("string", "app_name", ...)`

**iOS**: Verify Info.plist has `CFBundleDisplayName` set to `$(APP_DISPLAY_NAME)`

## Related Documentation

- 📄 [BUILD_CONFIG_SUMMARY.md](../BUILD_CONFIG_SUMMARY.md) - Implementation overview
- 📄 [BUILD_CHECKLIST.md](../BUILD_CHECKLIST.md) - Setup checklist
- 📄 [IOS_SETUP_INSTRUCTIONS.md](../IOS_SETUP_INSTRUCTIONS.md) - Detailed iOS setup
- 📄 [FIREBASE_CONFIG_README.md](../FIREBASE_CONFIG_README.md) - Firebase configuration
- 📄 [Deployment & CI/CD](deployment.md) - CI/CD workflows
- 📄 [Developer Setup](setup.md) - Initial project setup
