import Foundation
import FirebaseAuth
import FirebaseCore
import LiveChatCompose

final class FirebasePhoneAuthBridge: NSObject, PhoneAuthBridge {
    private func ensureFirebaseConfigured() {
        if FirebaseApp.app() == nil {
            FirebaseApp.configure()
        }
    }

    func sendCode(
        phoneE164: String,
        completionHandler: @escaping (PhoneAuthBridgeResult?, Error?) -> Void
    ) {
        ensureFirebaseConfigured()
        let provider = PhoneAuthProvider.provider()
        provider.verifyPhoneNumber(phoneE164, uiDelegate: nil) { verificationId, error in
            if let error = error {
                let bridgeError = error.toBridgeError()
                completionHandler(PhoneAuthBridgeResult(verificationId: nil, error: bridgeError), nil)
                return
            }
            completionHandler(PhoneAuthBridgeResult(verificationId: verificationId, error: nil), nil)
        }
    }

    func verifyCode(
        verificationId: String,
        code: String,
        completionHandler: @escaping (PhoneAuthBridgeError?, Error?) -> Void
    ) {
        ensureFirebaseConfigured()
        let provider = PhoneAuthProvider.provider()
        let credential = provider.credential(withVerificationID: verificationId, verificationCode: code)
        let auth = Auth.auth()
        auth.signIn(with: credential) { _, error in
            if let error = error {
                completionHandler(error.toBridgeError(), nil)
            } else {
                completionHandler(nil, nil)
            }
        }
    }
}

private extension Error {
    func toBridgeError() -> PhoneAuthBridgeError {
        let nsError = self as NSError
        let domain = nsError.domain
        let code = KotlinLong(value: Int64(nsError.code))
        return PhoneAuthBridgeError(domain: domain, code: code, message: nsError.localizedDescription)
    }
}
