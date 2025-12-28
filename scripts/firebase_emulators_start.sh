#!/usr/bin/env bash
set -euo pipefail

PROJECT_ID="${FIREBASE_PROJECT_ID:-livechat-emulator}"
EXPORT_DIR="${FIREBASE_EMULATORS_EXPORT_DIR:-./.firebase/emulator-data}"
ONLY_SERVICES="${FIREBASE_EMULATORS_ONLY:-auth,firestore,storage,functions}"

if command -v firebase >/dev/null 2>&1; then
  firebase emulators:start \
    --project "$PROJECT_ID" \
    --only "$ONLY_SERVICES" \
    --import="$EXPORT_DIR" \
    --export-on-exit "$EXPORT_DIR"
else
  npx firebase-tools emulators:start \
    --project "$PROJECT_ID" \
    --only "$ONLY_SERVICES" \
    --import="$EXPORT_DIR" \
    --export-on-exit "$EXPORT_DIR"
fi
