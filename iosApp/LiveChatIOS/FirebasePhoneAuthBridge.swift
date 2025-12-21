import Foundation
import FirebaseAuth
import LiveChatCompose

final class FirebasePhoneAuthBridge: NSObject, PhoneAuthBridge {
    private let auth = Auth.auth()
    private let provider = PhoneAuthProvider.provider()

    func sendCode(
        phoneE164: String,
        completionHandler: @escaping (PhoneAuthBridgeResult?, Error?) -> Void
    ) {
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
        let credential = provider.credential(withVerificationID: verificationId, verificationCode: code)
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
