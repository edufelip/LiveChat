# Compose Recomposition Optimization Report

## Summary

- Goal: Avoid triggering filter transition animations on every list mutation.
- Measurement artifacts used (Layout Inspector / compiler reports / benchmarks / traces): None (heuristic review only).
- Scope (modules/screens): Conversation list screen.

## Files Changed

- [x] app/src/commonMain/kotlin/com/edufelip/livechat/ui/features/conversations/list/screens/ConversationListScreen.kt

## Changes by Rule

### Rule B — Use derivedStateOf for Fast-Changing Inputs

- No issues observed; no changes applied.

### Rule E — Add Stable Keys in Lists

- **File:** app/src/commonMain/kotlin/com/edufelip/livechat/ui/features/conversations/list/screens/ConversationListScreen.kt
- **Composables:** ConversationListScreen
- **Pattern observed:** AnimatedContent `targetState` included the full conversations list, causing transitions on any list change.
- **Fix applied:** Target the filter only and keep list rendering inside content block.
- **Why it reduces recomposition:** Prevents animation restart on list updates, reducing unnecessary transitions.
- **Risk level:** Low
- **Validation:** Not measured; recommend Layout Inspector recomposition counters during incoming message updates.

### Rule A/C/D/F/G

- No changes applied.

## Compiler Reports Summary (If Available)

- Restartable but not skippable (before → after): Not measured.
- Notable unstable parameters (before → after): Not measured.
- Strong skipping config changes: None.

## Recommended Follow-Ups

- [ ] Layout Inspector: verify filter transitions only on chip selection, not on new messages.
- [ ] Manual: confirm list updates do not fade on new message arrivals.
