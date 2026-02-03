import UIKit
import LiveChatCompose
#if canImport(FirebaseCore)
import FirebaseCore
#endif

class SceneDelegate: UIResponder, UIWindowSceneDelegate {
    var window: UIWindow?

    func scene(
        _ scene: UIScene,
        willConnectTo session: UISceneSession,
        options connectionOptions: UIScene.ConnectionOptions
    ) {
        #if canImport(FirebaseCore)
        _ = FirebaseConfig.configureIfNeeded()
        #endif
        guard let windowScene = scene as? UIWindowScene else { return }

        let window = UIWindow(windowScene: windowScene)
        let config = makeFirebaseRestConfig()

        let root = LiveChatRootViewController(
            config: config,
            userId: "",
            idToken: nil
        )
        root.modalPresentationStyle = .fullScreen

        // Ensure areas outside the safe area inherit our app background (not black).
        window.backgroundColor = liveChatBackgroundColor()
        // Ensure the root fills the window even if iOS chooses a sheet-style scene.
        window.frame = windowScene.coordinateSpace.bounds
        window.rootViewController = root
        window.makeKeyAndVisible()
        self.window = window
    }
}

private func makeFirebaseRestConfig() -> FirebaseRestConfig {
    #if canImport(FirebaseCore)
    if let app = FirebaseApp.app() {
        NSLog("🔧 iOS SceneDelegate: Creating FirebaseRestConfig from FirebaseApp")
        NSLog("  - ProjectId: \(app.options.projectID ?? "❌ MISSING")")
        NSLog("  - ApiKey: \(app.options.apiKey?.prefix(10) ?? "❌ MISSING")...")
        
        let emulator = MainViewControllerKt.iosEmulatorOverrides()
        return FirebaseRestConfig(
            projectId: app.options.projectID ?? "YOUR_FIREBASE_PROJECT",
            apiKey: app.options.apiKey ?? "YOUR_FIREBASE_API_KEY",
            emulatorHost: emulator?.first as? String,
            emulatorPort: emulator?.second as? KotlinInt,
            usersCollection: "users",
            messagesCollection: "items",
            conversationsCollection: "inboxes",
            presenceCollection: "presence",
            invitesCollection: "invites",
            websocketEndpoint: "",
            pollingIntervalMs: 5000,
            defaultRegionIso: nil
        )
    }
    #endif
    
    NSLog("⚠️ iOS SceneDelegate: FirebaseApp not configured, using default config")
    return MainViewControllerKt.defaultFirebaseConfig()
}

private func liveChatBackgroundColor() -> UIColor {
    UIColor { trait in
        if trait.userInterfaceStyle == .dark {
            return UIColor(red: 14.0 / 255.0, green: 21.0 / 255.0, blue: 19.0 / 255.0, alpha: 1.0)
        } else {
            return UIColor(red: 244.0 / 255.0, green: 251.0 / 255.0, blue: 248.0 / 255.0, alpha: 1.0)
        }
    }
}
