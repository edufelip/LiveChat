import UIKit
import LiveChatCompose
#if canImport(FirebaseCore)
import FirebaseCore
#endif

@main
class AppDelegate: UIResponder, UIApplicationDelegate {
    // Scene-based lifecycle; window setup lives in SceneDelegate
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        #if canImport(FirebaseCore)
        if FirebaseApp.app() == nil {
            FirebaseApp.configure()
        }
        #endif
        return true
    }
}
