# Deployment & CI/CD

The project uses GitHub Actions for automated building, testing, and distribution. The build system supports multiple flavors (dev/prod) and build types (debug/release).

## Build Configuration

LiveChat uses a multi-flavor build system:
- **Build Types**: `debug` (debuggable) and `release` (optimized with ProGuard/R8)
- **Product Flavors**: `dev` (development backend) and `prod` (production backend)
- **Build Variants**: `devDebug`, `devRelease`, `prodDebug`, `prodRelease`

See [Build Configuration](build_configuration.md) for complete details on flavors, build commands, and ProGuard configuration.

## Continuous Integration (CI)

On every pull request and push to the `main` branch, the following checks are typically performed:
- Linting and code style verification (Spotless).
- Unit tests across all shared modules.
- Build verification for Android and iOS.

## Continuous Deployment (CD)

### Android Release (`android-release.yml`)
Triggered on pushes to the `main` branch or manually via `workflow_dispatch`.

1.  **Build**: Compiles the `prodRelease` variant and generates a signed Android App Bundle (AAB).
    - Uses ProGuard/R8 for code optimization and obfuscation
    - Minifies resources and removes unused code
    - Firebase config loaded from `app/src/prod/google-services.json`
2.  **Artifact Storage**: Uploads the bundle to GitHub Actions artifacts.
3.  **Play Store Distribution**: Automatically uploads the bundle to the Google Play Console (Internal/Alpha/Beta tracks).

**Output**: `app/build/outputs/bundle/prodRelease/app-prod-release.aab`

### iOS Release (`ios-release.yml`)
Automates the build process for the iOS application, handling provisioning and certificate management to generate IPA files for TestFlight or the App Store.

- Builds the Release configuration (prod flavor)
- Firebase config loaded from `iosApp/config/prod/GoogleService-Info.plist`
- Generates optimized IPA for distribution

**Output**: `iosApp/build/export/iosApp.ipa`

## Environment Variables & Secrets

Sensitive data is managed via GitHub Secrets:

### Android Secrets
- `ANDROID_RELEASE_KEYSTORE_BASE64`: Release keystore file (base64 encoded).
- `ANDROID_RELEASE_STORE_PASSWORD`: Keystore password.
- `ANDROID_RELEASE_KEY_ALIAS`: Key alias in the keystore.
- `ANDROID_RELEASE_KEY_PASSWORD`: Key password.
- `ANDROID_GOOGLE_SERVICES_JSON_BASE64`: Production Firebase configuration (base64 encoded).
- `PLAY_SERVICE_ACCOUNT_JSON`: Credentials for Google Play Console API.

### iOS Secrets
- `IOS_DIST_CERT_P12_BASE64`: Distribution certificate (base64 encoded).
- `IOS_DIST_CERT_PASSWORD`: Certificate password.
- `IOS_PROFILE_BASE64`: Provisioning profile (base64 encoded).
- `IOS_TEAM_ID`: Apple Developer Team ID.
- `IOS_KEYCHAIN_PASSWORD`: Temporary keychain password for CI.
- `IOS_GOOGLE_SERVICE_INFO_PLIST_BASE64`: Production Firebase configuration (base64 encoded).
- `APP_STORE_CONNECT_API_KEY_ID`: App Store Connect API key ID.
- `APP_STORE_CONNECT_API_KEY_ISSUER_ID`: API issuer ID.
- `APP_STORE_CONNECT_API_KEY_CONTENT`: API key content (base64 encoded).

### Encoding Files to Base64

To encode files for GitHub Secrets:

```bash
# Android
base64 -i app/release.keystore | pbcopy  # macOS
base64 -w 0 app/release.keystore         # Linux

base64 -i app/src/prod/google-services.json | pbcopy

# iOS
base64 -i certificate.p12 | pbcopy
base64 -i profile.mobileprovision | pbcopy
base64 -i iosApp/config/prod/GoogleService-Info.plist | pbcopy
```

## Versioning
Version codes and build numbers are automatically incremented based on the GitHub Run Number, ensuring unique builds for every deployment.

### Android
```bash
./gradlew bundleProdRelease \
  -PversionCode="$GITHUB_RUN_NUMBER" \
  -PversionName="1.0.$GITHUB_RUN_NUMBER"
```

### iOS
```bash
/usr/libexec/PlistBuddy -c "Set :CFBundleVersion $GITHUB_RUN_NUMBER" iosApp/iosApp/Info.plist
```

## Build Variants in CI/CD

### Current Production Workflow
- **Android**: Builds `prodRelease` variant (optimized for Play Store)
- **iOS**: Builds Release configuration with prod flavor (for TestFlight)

### Adding Dev Release Workflow (Optional)

To build and distribute dev releases via CI/CD:

1. **Add Dev Secrets**:
   - `ANDROID_GOOGLE_SERVICES_JSON_DEV_BASE64`
   - `IOS_GOOGLE_SERVICE_INFO_PLIST_DEV_BASE64`

2. **Create Dev Workflow Files**:
   - `.github/workflows/android-dev-release.yml`
   - `.github/workflows/ios-dev-release.yml`

3. **Modify Build Commands**:
   ```bash
   # Android
   ./gradlew bundleDevRelease
   
   # iOS - use Debug or Release-Dev configuration
   ```

4. **Distribute**:
   - Firebase App Distribution for internal testing
   - TestFlight with separate bundle identifier

See [Build Configuration](build_configuration.md) for more details on flavors and variants.

## ProGuard/R8 Configuration

Release builds use R8 for code optimization, minification, and obfuscation. ProGuard rules are defined in:

📄 `app/proguard-rules.pro`

The mapping file for deobfuscating crash reports is generated at:
- `app/build/outputs/mapping/prodRelease/mapping.txt`

**Important**: Upload the mapping file to Google Play Console for each release to enable crash report symbolication.

## Related Documentation

- [Build Configuration](build_configuration.md) - Complete build system documentation
- [Developer Setup](setup.md) - Initial project setup
- [IOS_SETUP_INSTRUCTIONS.md](../../IOS_SETUP_INSTRUCTIONS.md) - iOS Xcode configuration
