# Compose Recomposition Optimization Report

## Summary

- Goal: Add a system-settings CTA for denied notification permission without introducing unnecessary recompositions.
- Measurement artifacts used (Layout Inspector / compiler reports / benchmarks / traces): Not available (heuristic review only).
- Scope (modules/screens): Notification settings route + screen.

## Files Changed

- [x] app/src/commonMain/kotlin/com/edufelip/livechat/ui/features/settings/notifications/NotificationSettingsRoute.kt
- [x] app/src/commonMain/kotlin/com/edufelip/livechat/ui/features/settings/notifications/NotificationSettingsScreen.kt

## Changes by Rule

### Rule A — Read State in the Narrowest Scope

- **File:** app/src/commonMain/kotlin/com/edufelip/livechat/ui/features/settings/notifications/NotificationSettingsScreen.kt
- **Composables:** NotificationSettingsScreen
- **Pattern observed:** New CTA depends only on `systemPermissionGranted` and `onOpenSystemSettings`.
- **Fix applied:** None.
- **Why it reduces recomposition:** N/A.
- **Risk level:** Low.
- **Validation:** Heuristic only.

### Rule B — Use derivedStateOf for Fast-Changing Inputs

- **File:** app/src/commonMain/kotlin/com/edufelip/livechat/ui/features/settings/notifications/NotificationSettingsRoute.kt
- **Composables:** NotificationSettingsRoute
- **Pattern observed:** No fast-changing inputs added.
- **Fix applied:** None.
- **Why it reduces recomposition:** N/A.
- **Risk level:** Low.
- **Validation:** Heuristic only.

### Rule C — Use snapshotFlow for Side Effects

- **File:** app/src/commonMain/kotlin/com/edufelip/livechat/ui/features/settings/notifications/NotificationSettingsRoute.kt
- **Composables:** NotificationSettingsRoute
- **Pattern observed:** CTA trigger is event-driven; no high-frequency effects.
- **Fix applied:** None.
- **Why it reduces recomposition:** N/A.
- **Risk level:** Low.
- **Validation:** Heuristic only.

### Rule D — Defer Hot Reads to Layout/Draw

- **File:** app/src/commonMain/kotlin/com/edufelip/livechat/ui/features/settings/notifications/NotificationSettingsScreen.kt
- **Composables:** NotificationSettingsScreen
- **Pattern observed:** No hot layout/draw reads added.
- **Fix applied:** None.
- **Why it reduces recomposition:** N/A.
- **Risk level:** Low.
- **Validation:** Heuristic only.

### Rule E — Add Stable Keys in Lists

- **File:** app/src/commonMain/kotlin/com/edufelip/livechat/ui/features/settings/notifications/NotificationSettingsScreen.kt
- **Composables:** NotificationSettingsScreen
- **Pattern observed:** No list rendering added.
- **Fix applied:** None.
- **Why it reduces recomposition:** N/A.
- **Risk level:** Low.
- **Validation:** Heuristic only.

### Rule F — Improve Parameter Stability

- **File:** app/src/commonMain/kotlin/com/edufelip/livechat/ui/features/settings/notifications/NotificationSettingsScreen.kt
- **Composables / Models:** NotificationSettingsScreen
- **Pattern observed (compiler report or heuristic):** New callback passed to CTA uses `rememberStableAction` to keep lambda stable.
- **Fix applied:** Reused existing stable-action helpers.
- **Why it reduces recomposition:** Avoids callback identity changes across recompositions.
- **Risk level:** Low.
- **Validation:** Heuristic only.

### Rule G — Strong Skipping Mode Awareness

- **Module:** app
- **Current state:** Not evaluated in this change.
- **Suggestion or change:** None.
- **Risk level:** Low.
- **Validation:** Heuristic only.

## Compiler Reports Summary (If Available)

- Restartable but not skippable (before → after): Not measured.
- Notable unstable parameters (before → after): Not measured.
- Strong skipping config changes: None.

## Recommended Follow-Ups

- [ ] Layout Inspector checks to run (screens + expected counters): NotificationSettingsScreen while toggling push and opening settings CTA.
- [ ] Tests to run (unit/UI/benchmark): None required beyond existing UI checks.
- [ ] Traces to capture: None.
