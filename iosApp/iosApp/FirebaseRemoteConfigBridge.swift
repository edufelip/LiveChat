import Foundation
import LiveChatCompose
#if canImport(FirebaseRemoteConfig)
import FirebaseRemoteConfig
#endif

final class FirebaseRemoteConfigBridge: NSObject, RemoteConfigBridge {
    #if canImport(FirebaseRemoteConfig)
    private let remoteConfig: RemoteConfig

    override init() {
        _ = FirebaseConfig.configureIfNeeded()
        remoteConfig = RemoteConfig.remoteConfig()
        let settings = RemoteConfigSettings()
        #if DEBUG
        settings.minimumFetchInterval = 0
        #endif
        remoteConfig.configSettings = settings
        remoteConfig.setDefaults([
            "privacy_policy_url": "https://www.portfolio.eduwaldo.com/projects/live-chat/privacy_policy" as NSString
        ])
        super.init()
    }
    #else
    override init() {
        super.init()
    }
    #endif

    func fetchAndActivate(
        completionHandler: @escaping (RemoteConfigBridgeResult?, Error?) -> Void
    ) {
        #if canImport(FirebaseRemoteConfig)
        remoteConfig.fetchAndActivate { status, error in
            if let error = error {
                completionHandler(
                    RemoteConfigBridgeResult(
                        activated: false,
                        error: error.toBridgeError()
                    ),
                    nil
                )
                return
            }
            let activated = status == .successFetchedFromRemote || status == .successUsingPreFetchedData
            completionHandler(
                RemoteConfigBridgeResult(
                    activated: activated,
                    error: nil
                ),
                nil
            )
        }
        #else
        completionHandler(
            RemoteConfigBridgeResult(
                activated: false,
                error: RemoteConfigBridgeError(
                    domain: "FirebaseRemoteConfig",
                    code: KotlinLong(value: -1),
                    message: "FirebaseRemoteConfig not available"
                )
            ),
            nil
        )
        #endif
    }

    func getString(key: String) -> String {
        #if canImport(FirebaseRemoteConfig)
        _ = FirebaseConfig.configureIfNeeded()
        return remoteConfig.configValue(forKey: key).stringValue ?? ""
        #else
        return ""
        #endif
    }
}

private extension Error {
    func toBridgeError() -> RemoteConfigBridgeError {
        let nsError = self as NSError
        let domain = nsError.domain
        let code = KotlinLong(value: Int64(nsError.code))
        return RemoteConfigBridgeError(domain: domain, code: code, message: nsError.localizedDescription)
    }
}
