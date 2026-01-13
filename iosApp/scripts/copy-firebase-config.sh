#!/bin/bash

# Script to copy the correct GoogleService-Info.plist based on the build configuration
# This script should be run as a build phase in Xcode

set -e

PLIST_DESTINATION="${BUILT_PRODUCTS_DIR}/${PRODUCT_NAME}.app/GoogleService-Info.plist"

# Determine which plist to use based on PRODUCT_BUNDLE_IDENTIFIER
if [[ "${PRODUCT_BUNDLE_IDENTIFIER}" == *".dev" ]]; then
    PLIST_SOURCE="${SRCROOT}/iosApp/GoogleService-Info-Dev.plist"
    echo "Using Dev Firebase configuration"
else
    PLIST_SOURCE="${SRCROOT}/iosApp/GoogleService-Info-Prod.plist"
    echo "Using Prod Firebase configuration"
fi

if [ ! -f "$PLIST_SOURCE" ]; then
    echo "error: Firebase config file not found at $PLIST_SOURCE"
    echo "Please download the appropriate GoogleService-Info.plist from Firebase Console"
    exit 1
fi

cp "$PLIST_SOURCE" "$PLIST_DESTINATION"
echo "Copied $PLIST_SOURCE to $PLIST_DESTINATION"
