# Compose Recomposition Optimization Report

## Summary

- Goal: Apply privacy "share usage data" setting to analytics collection.
- Measurement artifacts used (Layout Inspector / compiler reports / benchmarks / traces): None (heuristic review only).
- Scope (modules/screens): App root and analytics controller bindings.

## Files Changed

- [x] app/src/commonMain/kotlin/com/edufelip/livechat/ui/app/LiveChatApp.kt
- [x] app/src/commonMain/kotlin/com/edufelip/livechat/analytics/AnalyticsController.kt
- [x] app/src/androidMain/kotlin/com/edufelip/livechat/analytics/AnalyticsController.android.kt
- [x] app/src/iosMain/kotlin/com/edufelip/livechat/analytics/AnalyticsController.ios.kt

## Changes by Rule

### Rule C — Use snapshotFlow for Side Effects

- **File:** app/src/commonMain/kotlin/com/edufelip/livechat/ui/app/LiveChatApp.kt
- **Composables:** LiveChatApp
- **Pattern observed:** Privacy setting needed to drive a side-effect (analytics collection) globally.
- **Fix applied:** Added `LaunchedEffect` keyed by `shareUsageData` to update analytics collection.
- **Why it reduces recomposition:** Not a recomposition change; centralizes side effects and avoids recomposition-time work.
- **Risk level:** Low
- **Validation:** Not measured; manual validation recommended.

### Rule A/B/D/E/F/G

- No changes applied.

## Compiler Reports Summary (If Available)

- Restartable but not skippable (before → after): Not measured.
- Notable unstable parameters (before → after): Not measured.
- Strong skipping config changes: None.

## Recommended Follow-Ups

- [ ] Manual: toggle "Share Usage Data" and verify analytics collection changes on Android.
- [ ] Manual: verify no crash on iOS (no-op controller).
