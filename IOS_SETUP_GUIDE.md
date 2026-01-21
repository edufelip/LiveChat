# iOS Build Configuration Setup Guide

Quick-start guide for configuring iOS build flavors (dev/prod) in Xcode.

## Overview

This project uses **product flavors** to maintain separate development and production builds:

- **Dev flavor**: Uses development Firebase project, app name "LiveChat Dev"
- **Prod flavor**: Uses production Firebase project, app name "LiveChat"

## Prerequisites

- Xcode installed
- Firebase projects created (dev and prod)
- Firebase config files downloaded (see step 1)

## Setup Steps

### Step 1: Add Firebase Configuration Files

Download Firebase configuration files and place them in the correct directories:

```
iosApp/
└── config/
    ├── dev/
    │   └── GoogleService-Info.plist    # Development Firebase project
    └── prod/
        └── GoogleService-Info.plist    # Production Firebase project
```

**How to get the files:**
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your Firebase project (dev or prod)
3. Navigate to: Project Settings → Your apps → iOS app
4. Click "Download GoogleService-Info.plist"
5. Save to the appropriate directory above

> **Note**: See `iosApp/config/README.md` for detailed Firebase setup instructions.

---

### Step 2: Configure Xcode Build Settings

Open the Xcode workspace:
```bash
open iosApp/iosApp.xcworkspace
```

#### Option A: User-Defined Settings (Recommended)

This approach uses two configurations (Debug and Release) with build settings to control flavor.

**2.1. Add FLAVOR Build Setting**

1. Select the `iosApp` target in Xcode
2. Go to **Build Settings** tab
3. Click the **+** button → **Add User-Defined Setting**
4. Name: `FLAVOR`
5. Set values:
   - **Debug**: `dev`
   - **Release**: `prod`

**2.2. Add APP_DISPLAY_NAME Build Setting**

1. Click **+** → **Add User-Defined Setting**
2. Name: `APP_DISPLAY_NAME`
3. Set values:
   - **Debug**: `LiveChat Dev`
   - **Release**: `LiveChat`

**2.3. Add BUNDLE_ID_SUFFIX Build Setting**

1. Click **+** → **Add User-Defined Setting**
2. Name: `BUNDLE_ID_SUFFIX`
3. Set values:
   - **Debug**: `.dev`
   - **Release**: (leave empty)

**2.4. Update Product Bundle Identifier**

1. Still in **Build Settings**, find **"Product Bundle Identifier"**
2. Change from `com.yourcompany.livechat` to:
   ```
   com.yourcompany.livechat$(BUNDLE_ID_SUFFIX)
   ```
   This makes Debug builds use `com.yourcompany.livechat.dev`

**2.5. Update Display Name**

1. Go to **Info** tab (or edit Info.plist)
2. Find **"Bundle display name"** (or `CFBundleDisplayName`)
3. Set value to: `$(APP_DISPLAY_NAME)`

#### Option B: Duplicate Configurations (Alternative)

This approach creates separate Debug-Dev, Debug-Prod, Release-Dev, Release-Prod configurations.

See `docs/specs/build_configuration.md` for detailed instructions on this approach.

---

### Step 3: Add Build Phase Script

This script automatically copies the correct Firebase config during build.

**3.1. Add Run Script Phase**

1. Select the `iosApp` target
2. Go to **Build Phases** tab
3. Click **+** → **New Run Script Phase**
4. Drag the new phase:
   - **After**: "Dependencies" phase
   - **Before**: "Compile Sources" phase
5. Rename it to: **"Copy Firebase Config"**

**3.2. Add Script Content**

Paste this into the script text area:

```bash
bash "${SRCROOT}/scripts/copy-firebase-config.sh"
```

**3.3. Configure Script Settings**

- **Shell**: `/bin/sh`
- **Based on dependency analysis**: ✓ (checked)
- **Show environment variables in build log**: ☐ (optional)

---

### Step 4: Remove Old Firebase Config (if exists)

If you previously had a Firebase config in the main app bundle:

1. In Xcode Project Navigator, find `iosApp/iosApp/GoogleService-Info.plist`
2. Select it → **Delete** → Choose **"Remove Reference"** (keep the file on disk as backup)

> **Important**: Do NOT add the config files from `iosApp/config/dev/` or `iosApp/config/prod/` to the Xcode target. They are copied automatically by the script.

---

### Step 5: Verify Setup

**5.1. Clean Build**

In Xcode: **Product** → **Clean Build Folder** (Shift+Cmd+K)

**5.2. Build Debug Configuration**

1. Select **Debug** configuration
2. Build (Cmd+B)
3. Check the build log for:
   ```
   ✅ Using dev Firebase config
   Copying: iosApp/config/dev/GoogleService-Info.plist
   ```

**5.3. Build Release Configuration**

1. Select **Release** configuration  
   (Edit Scheme → Run → Build Configuration → Release)
2. Build (Cmd+B)
3. Check the build log for:
   ```
   ✅ Using prod Firebase config
   Copying: iosApp/config/prod/GoogleService-Info.plist
   ```

**5.4. Verify App Names**

- Debug build: App should appear as **"LiveChat Dev"** on device
- Release build: App should appear as **"LiveChat"** on device

---

## Build Configurations Summary

| Configuration | Flavor | App Name | Bundle ID | Firebase Project |
|--------------|--------|----------|-----------|-----------------|
| Debug | dev | LiveChat Dev | `com.yourcompany.livechat.dev` | Development |
| Release | prod | LiveChat | `com.yourcompany.livechat` | Production |

---

## Troubleshooting

### Build fails with "Firebase config not found"

**Cause**: Missing `.plist` file or incorrect `FLAVOR` setting

**Solution**:
1. Verify files exist:
   ```bash
   ls -la iosApp/config/dev/GoogleService-Info.plist
   ls -la iosApp/config/prod/GoogleService-Info.plist
   ```
2. Check `FLAVOR` build setting is defined in Xcode
3. Verify script has execute permissions:
   ```bash
   chmod +x iosApp/scripts/copy-firebase-config.sh
   ```

### Wrong Firebase project being used

**Cause**: `FLAVOR` setting doesn't match configuration

**Solution**:
1. Check build logs to see which flavor is active
2. Verify `FLAVOR` values:
   - Debug → `dev`
   - Release → `prod`
3. Clean build folder and rebuild

### App installs but Firebase doesn't work

**Cause**: Config file not embedded in app bundle

**Solution**:
1. Check build logs for successful copy operation
2. Verify the script runs **before** "Compile Sources" phase
3. Inspect the built app bundle:
   ```bash
   # Find the app bundle in DerivedData
   find ~/Library/Developer/Xcode/DerivedData -name "iosApp.app" -type d
   # Check if GoogleService-Info.plist exists inside
   ls -la [path-to-app]/GoogleService-Info.plist
   ```

### Both Dev and Prod apps have same name

**Cause**: Display name not using `$(APP_DISPLAY_NAME)`

**Solution**:
1. Check Info tab or Info.plist
2. Ensure `CFBundleDisplayName` = `$(APP_DISPLAY_NAME)`
3. Verify `APP_DISPLAY_NAME` build setting exists with correct values

### Cannot install both Dev and Prod simultaneously

**Cause**: Bundle IDs are identical

**Solution**:
1. Check Product Bundle Identifier includes `$(BUNDLE_ID_SUFFIX)`
2. Should be: `com.yourcompany.livechat$(BUNDLE_ID_SUFFIX)`
3. Verify `BUNDLE_ID_SUFFIX` is `.dev` for Debug, empty for Release

---

## Quick Reference Commands

```bash
# Open Xcode workspace
open iosApp/iosApp.xcworkspace

# Check script permissions
ls -la iosApp/scripts/copy-firebase-config.sh

# Make script executable (if needed)
chmod +x iosApp/scripts/copy-firebase-config.sh

# View Firebase configs
cat iosApp/config/dev/GoogleService-Info.plist
cat iosApp/config/prod/GoogleService-Info.plist
```

---

## Related Documentation

- **Complete guide**: `docs/specs/build_configuration.md`
- **iOS config setup**: `iosApp/config/README.md`
- **Project setup**: `docs/specs/setup.md`
- **Deployment guide**: `docs/specs/deployment.md`

---

## Next Steps After Setup

1. ✅ Test Debug build on simulator/device
2. ✅ Test Release build on simulator/device
3. ✅ Verify correct Firebase projects are connected
4. ✅ Test that both apps can be installed simultaneously
5. ✅ Update CI/CD secrets with Firebase configs (if not done)

---

**Need help?** See the complete build configuration documentation in `docs/specs/build_configuration.md` for more details and advanced scenarios.
