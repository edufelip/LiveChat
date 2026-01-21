#!/bin/bash
# Script to copy the correct Firebase configuration based on build configuration
# This script should be added as a "Run Script" build phase in Xcode

# Determine the flavor based on user-defined setting
FLAVOR="${FLAVOR:-prod}"

echo "Using Firebase config for flavor: ${FLAVOR}"

SOURCE_PATH="${PROJECT_DIR}/config/${FLAVOR}/GoogleService-Info.plist"
DEST_PATH="${BUILT_PRODUCTS_DIR}/${PRODUCT_NAME}.app/GoogleService-Info.plist"

if [ ! -f "${SOURCE_PATH}" ]; then
    echo "Error: Firebase config not found at ${SOURCE_PATH}"
    exit 1
fi

cp "${SOURCE_PATH}" "${DEST_PATH}"
echo "Copied Firebase config from ${SOURCE_PATH}"
