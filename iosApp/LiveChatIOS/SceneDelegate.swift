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
        if FirebaseApp.app() == nil {
            FirebaseApp.configure()
        }
        #endif
        guard let windowScene = scene as? UIWindowScene else { return }

        let window = UIWindow(windowScene: windowScene)
        let config = MainViewControllerKt.defaultFirebaseConfig()

        let root = LiveChatRootViewController(
            config: config,
            userId: "demo-user",
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

private func liveChatBackgroundColor() -> UIColor {
    UIColor { trait in
        if trait.userInterfaceStyle == .dark {
            return UIColor(red: 14.0 / 255.0, green: 21.0 / 255.0, blue: 19.0 / 255.0, alpha: 1.0)
        } else {
            return UIColor(red: 244.0 / 255.0, green: 251.0 / 255.0, blue: 248.0 / 255.0, alpha: 1.0)
        }
    }
}
