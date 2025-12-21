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
        let environment = ProcessInfo.processInfo.environment
        let arguments = ProcessInfo.processInfo.arguments
        let isUiTest =
            environment["UITEST_MODE"] == "1" ||
            environment["XCTestConfigurationFilePath"] != nil ||
            arguments.contains("-ui-testing")
        UserDefaults.standard.set(isUiTest, forKey: "UITEST_MODE")
        if let uiTestPhone = environment["UITEST_PHONE"] {
            UserDefaults.standard.set(uiTestPhone, forKey: "UITEST_PHONE")
        }
        if let uiTestOtp = environment["UITEST_OTP"] {
            UserDefaults.standard.set(uiTestOtp, forKey: "UITEST_OTP")
        }
        return true
    }
}
