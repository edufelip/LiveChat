import Foundation
import LiveChatCompose
#if canImport(FirebaseCore)
import FirebaseCore
#endif
#if canImport(FirebaseAuth)
import FirebaseAuth
#endif

enum LiveChatBridgeFactory {
    static func make(config: FirebaseRestConfig) -> IosBridgeBundle {
        #if canImport(FirebaseCore)
        if FirebaseApp.app() == nil {
            FirebaseApp.configure()
        }
        #endif
        syncSessionFromFirebaseAuth()
        if shouldUseTestBundle() {
            return makeTestBundle()
        }
        let messages = FirebaseMessagesBridge(config: config)
        let contacts = FirebaseContactsBridge(config: config)
        let storage = FirebaseStorageBridge()
        let phoneAuth = FirebasePhoneAuthBridge()
        return IosBridgeBundle(
            messagesBridge: messages,
            contactsBridge: contacts,
            storageBridge: storage,
            phoneAuthBridge: phoneAuth
        )
    }

    private static func makeTestBundle() -> IosBridgeBundle {
        let defaults = IosBridgeDefaults.shared.empty()
        let phoneAuth = UITestPhoneAuthBridge()
        return defaults.doCopy(
            messagesBridge: defaults.messagesBridge,
            contactsBridge: defaults.contactsBridge,
            storageBridge: defaults.storageBridge,
            phoneAuthBridge: phoneAuth
        )
    }

    private static func shouldUseTestBundle() -> Bool {
        let environment = ProcessInfo.processInfo.environment
        let arguments = ProcessInfo.processInfo.arguments
        let e2eFlag = environment["E2E_MODE"] == "1" || environment["E2E_MODE"] == "true"
        let hasE2eArg = arguments.contains("-e2e-testing")
        if e2eFlag || hasE2eArg {
            return false
        }
        let hasUiTestArg = arguments.contains("-ui-testing")
        let isUiTest =
            environment["UITEST_MODE"] == "1" ||
            environment["XCTestConfigurationFilePath"] != nil ||
            hasUiTestArg
        if isUiTest {
            NSLog("LiveChatBridgeFactory: using UI test bridge bundle")
        }
        return isUiTest
    }

    private static func syncSessionFromFirebaseAuth() {
        #if canImport(FirebaseAuth)
        guard let user = Auth.auth().currentUser else {
            return
        }
        user.getIDTokenForcingRefresh(false) { token, _ in
            MainViewControllerKt.updateLiveChatSession(userId: user.uid, idToken: token)
        }
        #endif
    }
}
