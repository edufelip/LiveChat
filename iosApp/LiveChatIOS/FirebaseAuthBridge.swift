import Foundation
import FirebaseAuth
import LiveChatCompose

final class FirebaseAuthBridge: NSObject, AuthBridge {
    func signOut() {
        FirebaseConfig.configureIfNeeded()
        do {
            try Auth.auth().signOut()
        } catch {
            #if DEBUG
            NSLog("FirebaseAuthBridge.signOut error=%@", error.localizedDescription)
            #endif
        }
    }

    func sendEmailVerification(
        email: String,
        completionHandler: @escaping (AuthBridgeError?, Error?) -> Void
    ) {
        FirebaseConfig.configureIfNeeded()
        guard let user = Auth.auth().currentUser else {
            completionHandler(
                AuthBridgeError(
                    domain: "FirebaseAuth",
                    code: KotlinLong(value: -1),
                    message: "No authenticated user"
                ),
                nil
            )
            return
        }
        user.sendEmailVerification(beforeUpdatingEmail: email) { error in
            if let error = error {
                completionHandler(error.toBridgeError(), nil)
                return
            }
            completionHandler(nil, nil)
        }
    }

    func reloadCurrentUser(
        completionHandler: @escaping (AuthBridgeUserState?, Error?) -> Void
    ) {
        FirebaseConfig.configureIfNeeded()
        guard let user = Auth.auth().currentUser else {
            completionHandler(
                AuthBridgeUserState(
                    email: nil,
                    isEmailVerified: false,
                    error: AuthBridgeError(
                        domain: "FirebaseAuth",
                        code: KotlinLong(value: -2),
                        message: "No authenticated user"
                    )
                ),
                nil
            )
            return
        }
        user.reload { error in
            if let error = error {
                completionHandler(
                    AuthBridgeUserState(
                        email: user.email,
                        isEmailVerified: user.isEmailVerified,
                        error: error.toBridgeError()
                    ),
                    nil
                )
                return
            }
            completionHandler(
                AuthBridgeUserState(
                    email: user.email,
                    isEmailVerified: user.isEmailVerified,
                    error: nil
                ),
                nil
            )
        }
    }
}

private extension Error {
    func toBridgeError() -> AuthBridgeError {
        let nsError = self as NSError
        let domain = nsError.domain
        let code = KotlinLong(value: Int64(nsError.code))
        return AuthBridgeError(domain: domain, code: code, message: nsError.localizedDescription)
    }
}
