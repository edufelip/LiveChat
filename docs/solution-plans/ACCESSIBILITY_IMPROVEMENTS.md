# Accessibility Improvements Plan

## 1. Reduce Motion

### Goal
Allow users to minimize non-essential animations, adhering to system preferences or an in-app override.

### Implementation Strategy

1.  **Domain/Platform Layer:**
    *   Create a `AccessibilityRepository` interface.
    *   Implement `observeReduceMotionEnabled()`: `Flow<Boolean>`.
    *   **Android:** Use `AccessibilityManager.getRecommendedTimeoutMillis` (indirect) or check `Settings.Global.TRANSITION_ANIMATION_SCALE`. Ideally, use `AccessibilityManager` to listen for changes.
    *   **iOS:** Use `UIAccessibility.isReduceMotionEnabled` and listen for `UIAccessibility.reduceMotionStatusDidChangeNotification`.

2.  **UI Layer (Compose):**
    *   Expose `LocalReduceMotion` composition local.
    *   Create helper functions for animations that switch between "normal" and "snap/instant" specs based on this value.
    *   Example:
        ```kotlin
        @Composable
        fun <T> updateTransitionSafe(targetState: T, label: String? = null): Transition<T> {
            val reduceMotion = LocalReduceMotion.current
            // If reduce motion, usage might need to be adjusted or specs changed
        }
        ```
    *   Or simpler: apply `if (reduceMotion) 0 else 300` for durations.

## 2. High Contrast

### Goal
Increase contrast of UI elements for better readability.

### Implementation Strategy

1.  **Theming:**
    *   Define a `HighContrastColorPalette` in `Color.kt` / `Theme.kt`.
    *   This palette should use strictly black/white for text/backgrounds where possible, and high-contrast primary colors.

2.  **Detection:**
    *   **Android:** Check `UiModeManager` or Accessibility settings.
    *   **iOS:** Check `UIAccessibility.isDarkerSystemColorsEnabled` or `UIAccessibility.isHighContrastEnabled` (if available in bindings).

3.  **Integration:**
    *   In `LiveChatTheme`, read the high contrast state.
    *   Select the appropriate color scheme (`Light`, `Dark`, `HighContrastLight`, `HighContrastDark`).

## 3. Settings

*   Add "Accessibility" section to Settings screen (or under Appearance).
*   Allow overriding system defaults if desired (optional, but good for testing).
