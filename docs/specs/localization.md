# Localization & String Management

LiveChat uses a robust localization system to ensure the app is accessible in multiple languages and consistent across platforms.

## Multi-platform String Handling

The project utilizes **Compose Resources** to manage strings in a KMP-friendly way.

1.  **Resource Files**: XML string resources are located in `app/src/commonMain/composeResources/values/strings.xml`.
2.  **Generated Accessors**: The build process generates a `Res` object (e.g., `Res.string.welcome_title`) used to access these strings in Kotlin code.

## `LiveChatStrings` Wrapper

To simplify usage and provide default values (especially for previews), a wrapper system is used:

- **File**: `app/src/commonMain/kotlin/com/edufelip/livechat/ui/resources/LiveChatStrings.kt`
- **Structure**:
    - Data classes for each feature (e.g., `ContactsStrings`, `SettingsStrings`).
    - A `liveChatStrings()` Composable function that retrieves the current strings from a `CompositionLocal`.
    - `rememberLiveChatStrings()`: A helper that maps the raw `stringResource(Res.string...)` calls to the structured data classes.

## Features

- **Interpolation**: Supports formatted strings with placeholders (e.g., `Version %1$s (%2$s)`).
- **Preview Support**: Provides hardcoded default strings for `@Preview` environments where `stringResource` might not be available or needed.
- **Dynamic Content**: Some strings are computed dynamically (e.g., `resendCountdownLabel` returns a string based on the current seconds remaining).

## Adding New Strings

1.  Add the string entry to `strings.xml`.
2.  Update the corresponding data class in `LiveChatStrings.kt`.
3.  Add the mapping in `rememberLiveChatStrings()` to connect the XML resource to the data class property.
