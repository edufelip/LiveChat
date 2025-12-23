import Foundation
import LiveChatCompose
#if canImport(FirebaseCore)
import FirebaseCore
#endif

enum LiveChatBridgeFactory {
    static func make(config: FirebaseRestConfig) -> IosBridgeBundle {
        #if canImport(FirebaseCore)
        if FirebaseApp.app() == nil {
            FirebaseApp.configure()
        }
        #endif
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
}
