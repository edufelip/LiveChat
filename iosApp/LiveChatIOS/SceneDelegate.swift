import UIKit
import LiveChatCompose

class SceneDelegate: UIResponder, UIWindowSceneDelegate {
    var window: UIWindow?

    func scene(
        _ scene: UIScene,
        willConnectTo session: UISceneSession,
        options connectionOptions: UIScene.ConnectionOptions
    ) {
        guard let windowScene = scene as? UIWindowScene else { return }

        let window = UIWindow(windowScene: windowScene)
        let config = MainViewControllerKt.defaultFirebaseConfig()

        let root = LiveChatRootViewController(
            config: config,
            userId: "demo-user",
            idToken: nil
        )
        root.modalPresentationStyle = .fullScreen

        // Ensure the root fills the window even if iOS chooses a sheet-style scene.
        window.frame = windowScene.coordinateSpace.bounds
        window.rootViewController = root
        window.makeKeyAndVisible()
        self.window = window
    }
}
