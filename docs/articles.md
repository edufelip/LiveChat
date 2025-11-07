# Content Ideas for LiveChat KMM

## 1. UI-Focused Topics
- **Edge-to-edge Compose scaffolding on Android** – Walk through how `HomeScreen` and the navigation bar are padded via `WindowInsets` while still drawing behind system bars (`composeApp/src/commonMain/kotlin/com/project/livechat/composeapp/ui/features/home/view/HomeScreen.kt`). Showcase before/after screenshots and the `NavigationBar` color blending trick.
- **Compose Multiplatform theming strategy** – Explain how `LiveChatTheme` unifies Android/iOS palettes, typography, and semantics, highlighting pastel tonality and semantics usage across components (theme package under `composeApp/.../ui/theme`).
- **Contacts screen UX polish** – Deep dive into pull-to-refresh, invite dialogs, and state banners added in `ContactsRoute` + `ContactsScreen`, including how permission prompts hide/show the “Sync contacts” CTA.
- **Conversation list filters & chips** – Cover the new filter enum, chip row, and scroll behavior implemented in the conversation list screens, noting how `ConversationListPresenter` drives selection state.

## 2. Business / Product Topics
- **Onboarding flow with phone verification** – Narrate the end-to-end experience from `PhoneAuthPresenter` through `OnboardingFlowScreen`, including how `AppPresenter` gates Home vs Onboarding state via Room-backed snapshots.
- **Contact sync & dedupe rules** – Explain the permission prompts, local diffing logic, and remote checks handled by `ContactsPresenter` and `CheckRegisteredContactsUseCase`, along with UX decisions like hiding the button once synced.
- **Invite sharing across channels** – Show the new `InviteShareRequest` plumbing from UI to Android/iOS share sheets, and discuss future monetization (referrals, cross-platform invites).
- **Participant-based conversation actions** – Highlight how mute/archive/pin actions flow through `ConversationParticipantsRepository` to keep business rules consistent between list/detail screens.

## 3. Data / Persistence Topics
- **Migrating from SQLDelight to Room KMP** – Step-by-step guide covering plugin changes, entity/DAO definitions (`shared/data/.../database`), schema exports, and why Room’s bundled driver is beneficial for parity.
- **Room-powered onboarding status** – Detail how `RoomOnboardingStatusRepository` replaces DataStore/NSUserDefaults, enabling synchronous snapshots for `AppPresenter` and consistent observation across platforms.
- **Participant state modeling** – Discuss the `conversation_state` entity, how metadata blobs store settings, and how flows are exposed via `ConversationStateDao` for delivery receipts/mute logic.
- **Attachments & ciphertext serialization** – Explain the JSON adapters inside `MessageMappers`, why metadata/attachments are stored as strings, and how this unlocks future encrypted messaging features.

## 4. Infrastructure & Architecture Topics
- **Koin bootstrapping across Android/iOS** – Outline how `startKoinForAndroid`/`startKoinForiOS` share modules while injecting platform-specific dependencies (Firebase, Room builders, session providers).
- **KSP + Room configuration in a KMM module** – Share tips on wiring the Room Gradle plugin, schema directories, and multi-target KSP blocks (`shared/data/build.gradle.kts`), including CI implications.
- **Testing strategy for multiplatform persistence** – Compare Robolectric-backed Android unit tests with the new iOS Swift/Native tests (`shared/data/src/androidUnitTest` vs `shared/data/src/iosTest`), showing how both validate the same APIs.
- **Edge-to-edge infrastructure playbook** – Document the “KMM Edge-to-Edge Playbook” steps already applied: `WindowCompat` usage, transparent system bars, SwiftUI `.ignoresSafeArea`, and shared inset handling.

## 5. Other / Miscellaneous Topics
- **Firestore security rule hardening** – Use the cheatsheet at the bottom of `README.md` to craft a tutorial on conversation/participant rule patterns.
- **Roadmap brainstorms (Filters, Settings, Invites)** – Summarize the multi-phase plan already noted in `plan.md`/summaries and turn it into a public “LiveChat Roadmap” update.
- **Observability & analytics hooks** – Propose how to instrument presenters (coroutines, flows) for analytics without leaking platform concerns.
- **KMM contact permission parity** – Discuss challenges ensuring UX parity between Android’s runtime permissions and iOS’s static behavior, with tips for mocking permissions in shared presenters.
