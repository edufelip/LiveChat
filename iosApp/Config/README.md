# iOS Firebase Configuration

This directory contains Firebase configuration files for different product flavors.

## Directory Structure

```
config/
├── dev/
│   └── GoogleService-Info.plist     # Development Firebase project
└── prod/
    └── GoogleService-Info.plist     # Production Firebase project
```

## Setup Instructions

### 1. Obtain Firebase Configuration Files

#### For Development (dev flavor):
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your **development** Firebase project (or create one)
3. Navigate to Project Settings → Your apps → iOS app
4. Download `GoogleService-Info.plist`
5. Save it to `iosApp/config/dev/GoogleService-Info.plist`

#### For Production (prod flavor):
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your **production** Firebase project
3. Navigate to Project Settings → Your apps → iOS app
4. Download `GoogleService-Info.plist`
5. Save it to `iosApp/config/prod/GoogleService-Info.plist`

### 2. Configure Xcode Build Phase

The Firebase config is automatically copied during the build process via the script:
`iosApp/scripts/copy-firebase-config.sh`

**To set this up in Xcode:**

1. Open the workspace: `open iosApp/iosApp.xcworkspace`
2. Select the `iosApp` target
3. Go to **Build Phases** tab
4. Click the **+** button → **New Run Script Phase**
5. Drag it to be **after** "Dependencies" but **before** "Compile Sources"
6. Paste this script:

```bash
bash "${SRCROOT}/scripts/copy-firebase-config.sh"
```

7. Name the phase: "Copy Firebase Config"

### 3. Configure Build Settings for Flavors

You need to define the `FLAVOR` build setting so the script knows which config to use.

**Option 1: User-Defined Build Settings (Recommended)**

1. Select the `iosApp` target → **Build Settings** tab
2. Click **+** → **Add User-Defined Setting**
3. Name it: `FLAVOR`
4. Set values:
   - **Debug**: `dev`
   - **Release**: `prod`

**Option 2: Duplicate Configurations**

Create separate configurations like `Debug-Dev`, `Debug-Prod`, `Release-Dev`, `Release-Prod` and set `FLAVOR` accordingly.

See detailed instructions in: `docs/specs/build_configuration.md`

## Important Notes

- **Security**: These `.plist` files are automatically ignored by git (see root `.gitignore`)
- **Never commit** Firebase config files to version control
- **CI/CD**: GitHub Actions workflows write these files during build from secrets
- Each developer needs to download and place their own config files locally
- The script will fail the build if the config file is missing, ensuring you don't build without Firebase

## Verification

After setup, build the app and check the build logs for:

```
✅ Using dev Firebase config
Copying: iosApp/config/dev/GoogleService-Info.plist
```

Or for production builds:

```
✅ Using prod Firebase config
Copying: iosApp/config/prod/GoogleService-Info.plist
```

## Troubleshooting

**Build fails with "Firebase config not found":**
- Ensure you've downloaded the `.plist` files to the correct directories
- Check that the `FLAVOR` build setting is defined
- Verify the script has execute permissions: `chmod +x iosApp/scripts/copy-firebase-config.sh`

**Wrong Firebase project being used:**
- Check the build logs to see which flavor is active
- Verify the `FLAVOR` build setting matches your configuration (Debug/Release)
- Clean build folder: Product → Clean Build Folder in Xcode

## Related Documentation

- Complete build configuration guide: `docs/specs/build_configuration.md`
- Setup instructions: `docs/specs/setup.md`
- Deployment guide: `docs/specs/deployment.md`
