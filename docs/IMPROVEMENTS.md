1. Chats Screen
[x] Circle with recipient initials is too large, decrease its size by 4dp
[x] When last message was from the own user, in chats lists screen that message show be displayed with an indicative icon, see what's possible doing but my goal was having a 'single arrow check for sent', 'double arrow check for delivered' and then 'double arrow check in green for visualized'
[x] When selecting between 'all', 'unread', 'pinned' and 'archived' there should be a nice fade animation for transitioning between items

2. Settings Screen
[x] There is an animation when navigating to Privacy Screen. Navigation from Privacy screen to Blocked contacts screen should have the same slide animation
[x] For every submenu in this screen we should have a back navigation gesture for navigation back to Settings screen
[x] Remove top row from that screen, the one with the mocked hour time, wifi and battery icons, get rid of it all for good
[x] There is a search option in this screen which is just filtering options, but we should allow user seeing submenus when searching using this feature

2.1 Account Settings
[x] Tapped on 'Display name', bottom sheet opened, I put a new name but it seems saving is not implemented as tapping on save button just dismissed the bottom sheet
[x] Same goes for 'Status Message'
[x] Below 'Linked Phone Number' we should be the phone user used to log in, but right now shows 'No phone linked'
[x] 'Edit email address' flows is supposed to send an email to user, I'm seeing no email being sent, and that should be the rule for 'resend email': after sending e-mail user is supposed to wait for 5 minutes before requesting again for another email, so user should wee 'you'll be able to request again in x seconds' and the button sits disabled, just being enabled when countdown finishes
[x] Delete Account button triggers a 15 sec countdown in order to let the user delete account definitively, but after that time passes and user taps on it, button does nothing (Fixed by ensuring Firestore document is marked deleted before Auth user removal and adding loading feedback)

2.2 Notifications Settings
[x] When toggling 'push notifications' I see an error pop up - check if the implementation is correct (Fixed with upsert logic in remote data)
[x] In 'Notification sound' bottom sheet, after tapping on another option we should trigger that sound so user can listen to it before setting it (Implemented SoundPlayer with preview logic)
[x] Investigate if the 'Scheduled Quiet Mode' is actually implemented (Logic for checking quiet hours is implemented in `IsQuietModeActiveUseCase`, but full enforcement awaits notification infrastructure)
[x] Same goes for 'in-app vibration' and 'show message preview' (Settings are persisted but functional implementation is missing; no `Vibrator` usage and no notification display logic found)
[x] We should have a reference of the base notifications profile should look like, so when user taps on 'reset notifications' we can set the user notifications state to base state (Implemented using `NotificationSettings()` default constructor which serves as the single source of truth for defaults)

2.3 Appearance Settings
[x] Tapping on different themes should trigger color/theme change (Fixed by making repositories reactive at the root level)
[x] Typography scale - We should persist value user sets, and that value shall be reused in ConversationDetailScreen for displaying the messages (Fixed by making repositories reactive; typography scale is applied globally via MaterialTheme)
[x] We should plan how to implement 'reduce motion' and 'high contrast' (Plan created in `docs/solution-plans/ACCESSIBILITY_IMPROVEMENTS.md`)

2.4 Privacy Settings
[x] Check if 'Blocked Contacts' is actually implemented (Fully implemented: UI, data persistence, and message filtering are in place)
[x] Check if 'Last Seen & Online' is implemented (Implemented: Logic exists for updating presence and checking audience, though backend enforcement of 'Contacts' audience relies on client-side logic)
[x] Check if 'Read receipts' setting is implemented (Implemented: Logic exists to skip sending receipts and filtering incoming receipts if disabled)
[x] Check how we can implement 'Share usage data' (Setting is persisted, but `FirebaseAnalytics.setAnalyticsCollectionEnabled` is not called. Needs `AnalyticsService` to observe setting and toggle collection.)

