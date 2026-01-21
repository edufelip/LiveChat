# Infrastructure

LiveChat is built on a modern, cross-platform architecture using Kotlin Multiplatform (KMP).

## Architecture Layers

1.  **Domain Layer (`shared/domain`)**:
    - Contains business logic, Use Cases, and Presenters.
    - Pure Kotlin code with no platform dependencies.
    - Defines repository interfaces (`IRepository`).
2.  **Data Layer (`shared/data`)**:
    - Implements repository interfaces.
    - Handles networking (Firebase REST API), local persistence (Room/SQLDelight), and shared preferences.
    - **Dependency Injection**: Uses **Koin** for managing dependencies across modules.
3.  **UI Layer (`app/src/commonMain`, `iosApp`)**:
    - **Compose Multiplatform**: Used for most of the UI on both Android and iOS.
    - **Platform Bridges**: Swift/Kotlin bridges for features like contacts access, photo library, and audio recording.

## Dependency Injection

- **Koin** is initialized in `KoinInitialization.kt`.
- Shared modules: `SharedDomainModule.kt` and `SharedDataModule.kt`.
- Presenters are injected using `koinInject()` or similar platform-specific mechanisms.

## Database & Persistence

- **Room**: Used for local caching of messages, contacts, and onboarding status.
- **DataSources**:
    - `MessagesLocalDataSource.kt`: CRUD operations for message entities.
    - `ContactsLocalDataSource.kt`: Caching of synced contacts.
- **Migrations**: Database schema versioning is managed via Room's migration system.

## Error Handling

- **Domain Errors**: Custom exceptions like `RecentLoginRequiredException`.
- **UI State**: Most Presenters expose a `StateUI` or similar wrapper containing an `errorMessage` field for reactive error reporting.
