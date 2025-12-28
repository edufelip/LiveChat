#!/usr/bin/env bash
set -euo pipefail

if [ "$#" -eq 0 ]; then
  echo "Usage: $0 <command>"
  exit 1
fi

PROJECT_ID="${FIREBASE_PROJECT_ID:-livechat-emulator}"
ONLY_SERVICES="${FIREBASE_EMULATORS_ONLY:-auth,firestore,storage,functions}"
EXPORT_DIR="${FIREBASE_EMULATORS_EXPORT_DIR:-./.firebase/emulator-data}"

if command -v firebase >/dev/null 2>&1; then
  firebase emulators:exec \
    --project "$PROJECT_ID" \
    --only "$ONLY_SERVICES" \
    --import="$EXPORT_DIR" \
    --export-on-exit "$EXPORT_DIR" \
    -- "$@"
else
  npx firebase-tools emulators:exec \
    --project "$PROJECT_ID" \
    --only "$ONLY_SERVICES" \
    --import="$EXPORT_DIR" \
    --export-on-exit "$EXPORT_DIR" \
    -- "$@"
fi
