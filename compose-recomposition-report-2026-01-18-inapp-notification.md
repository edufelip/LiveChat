# Compose Recomposition Optimization Report

## Summary

- Goal: Add in-app notification banner host driven by a shared event flow.
- Measurement artifacts used (Layout Inspector / compiler reports / benchmarks / traces): None (heuristic review only).
- Scope (modules/screens): App root and in-app notification host.

## Files Changed

- [x] app/src/commonMain/kotlin/com/edufelip/livechat/ui/app/LiveChatApp.kt
- [x] app/src/commonMain/kotlin/com/edufelip/livechat/ui/components/molecules/InAppNotificationHost.kt

## Changes by Rule

### Rule C — Use snapshotFlow for Side Effects

- **File:** app/src/commonMain/kotlin/com/edufelip/livechat/ui/components/molecules/InAppNotificationHost.kt
- **Composables:** InAppNotificationHost
- **Pattern observed:** Event-driven UI that should not trigger recomposition outside notifications.
- **Fix applied:** Collected notification events in `LaunchedEffect(Unit)` with `collectLatest` and timed dismissal.
- **Why it reduces recomposition:** Limits updates to the active banner state instead of re-rendering unrelated UI.
- **Risk level:** Low
- **Validation:** Not measured; manual check recommended.

### Rule A — Read State in the Narrowest Scope

- **File:** app/src/commonMain/kotlin/com/edufelip/livechat/ui/app/LiveChatApp.kt
- **Composables:** LiveChatApp
- **Pattern observed:** Global side effects driven by privacy settings and navigation events.
- **Fix applied:** Scoped analytics and notification navigation into `LaunchedEffect` blocks.
- **Why it reduces recomposition:** Keeps side effects out of composition and prevents unnecessary recompositions.
- **Risk level:** Low
- **Validation:** Not measured; manual functional validation recommended.

### Rule B/D/E/F/G

- No changes applied.

## Compiler Reports Summary (If Available)

- Restartable but not skippable (before → after): Not measured.
- Notable unstable parameters (before → after): Not measured.
- Strong skipping config changes: None.

## Recommended Follow-Ups

- [ ] Manual: verify banner shows in foreground and dismisses after timeout.
- [ ] Manual: tap banner to open conversation.
