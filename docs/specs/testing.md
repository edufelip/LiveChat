# Testing Strategy

LiveChat follows a rigorous testing strategy combining unit, integration, and UI tests across platforms.

## Unit Testing (`commonTest`)

- **Domain Presenters**: Each presenter (e.g., `AppPresenter`, `ContactsPresenter`, `ConversationPresenter`) has a corresponding unit test file. These tests verify the UI state transitions based on simulated user actions and mocked use cases.
- **Use Cases**: Individual business logic units are tested in isolation using `kotlinx-coroutines-test`.
- **Mappers**: Data transformation logic (e.g., `ContactMappers`) is verified to ensure data integrity between the network and local database layers.

## Integration Testing

- **Data Sources**: `FirebaseRestContactsRemoteDataTest` verifies integration with the networking layer using Ktor's `MockEngine`.
- **Database**: Room database operations are tested (typically on Android/JVM) to ensure schema validity and query correctness.

## UI & Snapshot Testing (`androidDeviceTest`)

LiveChat uses **Screenshot Testing** to prevent visual regressions.

- **Golden Images**: The project maintains a set of "golden" reference images in `assets/goldens`.
- **Automated Comparisons**: During UI tests, the app captures current screens and compares them against the goldens.
- **Screen Coverage**: Key flows like Onboarding, Contact List, and Conversation Detail are covered by these tests.

## Running Tests

### Unit Tests
```bash
./gradlew :shared:domain:test
./gradlew :shared:data:test
```

### UI Tests (Android)
```bash
./gradlew :app:connectedDebugAndroidTest
```

## Tools Used
- **Mocking**: KMP-compatible mocking strategies or manual fakes.
- **Assertions**: Standard Kotlin test assertions.
- **Robolectric**: Used for running Android-specific tests on the JVM.
