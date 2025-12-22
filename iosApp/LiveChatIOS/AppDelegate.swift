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
        if isUiTest {
            UserDefaults.standard.set(true, forKey: "UITEST_MODE")
        } else {
            UserDefaults.standard.removeObject(forKey: "UITEST_MODE")
            UserDefaults.standard.removeObject(forKey: "UITEST_PHONE")
            UserDefaults.standard.removeObject(forKey: "UITEST_OTP")
            UserDefaults.standard.removeObject(forKey: "UITEST_RESET_ONBOARDING")
            UserDefaults.standard.removeObject(forKey: "UITEST_CONTACTS_FLOW")
            UserDefaults.standard.removeObject(forKey: "UITEST_CONTACTS_DENY")
        }
        if let uiTestPhone = environment["UITEST_PHONE"] {
            UserDefaults.standard.set(uiTestPhone, forKey: "UITEST_PHONE")
        }
        if let uiTestOtp = environment["UITEST_OTP"] {
            UserDefaults.standard.set(uiTestOtp, forKey: "UITEST_OTP")
        }
        if let uiTestReset = environment["UITEST_RESET_ONBOARDING"] {
            UserDefaults.standard.set(uiTestReset, forKey: "UITEST_RESET_ONBOARDING")
        }
        if let uiTestContactsFlow = environment["UITEST_CONTACTS_FLOW"] {
            UserDefaults.standard.set(uiTestContactsFlow, forKey: "UITEST_CONTACTS_FLOW")
        }
        if let uiTestContactsDeny = environment["UITEST_CONTACTS_DENY"] {
            UserDefaults.standard.set(uiTestContactsDeny, forKey: "UITEST_CONTACTS_DENY")
        }
        return true
    }
}
