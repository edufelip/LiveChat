# Compose Recomposition Optimization Report

## Summary

- Goal: Prevent filter transition animations from running on every list mutation while preserving transitions between filters.
- Measurement artifacts used (Layout Inspector / compiler reports / benchmarks / traces): None (heuristic review only).
- Scope (modules/screens): Conversation list screen.

## Files Changed

- [x] app/src/commonMain/kotlin/com/edufelip/livechat/ui/features/conversations/list/screens/ConversationListScreen.kt

## Changes by Rule

### Rule E — Add Stable Keys in Lists

- **File:** app/src/commonMain/kotlin/com/edufelip/livechat/ui/features/conversations/list/screens/ConversationListScreen.kt
- **Composables:** ConversationListScreen
- **Pattern observed:** AnimatedContent target state included the full conversations list, so any list update triggered a fade transition.
- **Fix applied:** Keep target state as (filter, list) to preserve before/after content, but gate transitions to only animate when filter changes.
- **Why it reduces recomposition:** Prevents animation work on list updates while preserving the intended filter transition.
- **Risk level:** Low
- **Validation:** Not measured; recommend Layout Inspector recomposition counters during incoming message updates.

### Rule A/B/C/D/F/G

- No changes applied.

## Compiler Reports Summary (If Available)

- Restartable but not skippable (before → after): Not measured.
- Notable unstable parameters (before → after): Not measured.
- Strong skipping config changes: None.

## Recommended Follow-Ups

- [ ] Layout Inspector: verify filter transitions only on chip selection, not on new messages.
- [ ] Manual: confirm list updates do not fade on new message arrivals.
