import UIKit
import LiveChatCompose
#if canImport(FirebaseCore)
import FirebaseCore
#endif

@main
class AppDelegate: UIResponder, UIApplicationDelegate {
    var window: UIWindow?

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        configureFirebaseIfAvailable()
        let window = UIWindow(frame: UIScreen.main.bounds)
        let config = MainViewControllerKt.defaultFirebaseConfig()
        let controller = MainViewControllerKt.MainViewController(
            config: config,
            userId: "demo-user",
            idToken: nil,
            phoneContactsProvider: { [] as [DomainContact] }
        )
        window.rootViewController = controller
        window.makeKeyAndVisible()
        self.window = window
        return true
    }

    private func configureFirebaseIfAvailable() {
#if canImport(FirebaseCore)
        if FirebaseApp.app() == nil {
            FirebaseApp.configure()
        }
#endif
    }
}
