import UIKit
import LiveChatCompose
#if canImport(FirebaseCore)
import FirebaseCore
#endif
#if canImport(FirebaseAuth)
import FirebaseAuth
#endif

@main
class AppDelegate: UIResponder, UIApplicationDelegate {
    // Scene-based lifecycle; window setup lives in SceneDelegate
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        #if canImport(FirebaseCore)
        _ = FirebaseConfig.configureIfNeeded()
        #endif
        let environment = ProcessInfo.processInfo.environment
        let arguments = ProcessInfo.processInfo.arguments
        let isE2e =
            environment["E2E_MODE"] == "1" ||
            environment["E2E_MODE"] == "true" ||
            arguments.contains("-e2e-testing")
        let isUiTest =
            environment["UITEST_MODE"] == "1" ||
            environment["XCTestConfigurationFilePath"] != nil ||
            arguments.contains("-ui-testing")
        if isUiTest {
            UserDefaults.standard.set(true, forKey: "UITEST_MODE")
        } else if !isE2e {
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
        #if canImport(FirebaseAuth)
        if isE2e {
            Auth.auth().settings?.isAppVerificationDisabledForTesting = true
        }
        #endif
        return true
    }
}
