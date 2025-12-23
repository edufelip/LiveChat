import Foundation
import FirebaseAuth
import FirebaseCore
import LiveChatCompose

final class FirebasePhoneAuthBridge: NSObject, PhoneAuthBridge {
    private func ensureFirebaseConfigured() -> PhoneAuthBridgeError? {
        if FirebaseApp.app() == nil {
            if let options = FirebasePhoneAuthBridge.loadOptions() {
                FirebaseApp.configure(options: options)
            } else {
                FirebaseApp.configure()
            }
        }
        if FirebaseApp.app() == nil {
            return PhoneAuthBridgeError(
                domain: "FirebaseAuth",
                code: KotlinLong(value: -1),
                message: "FirebaseApp.configure() did not initialize a default app. GoogleService-Info.plist missing or bundle id mismatch."
            )
        }
        return nil
    }

    private func configureTestingIfNeeded() {
        let environment = ProcessInfo.processInfo.environment
        let isE2e = environment["E2E_MODE"] == "1" || environment["E2E_MODE"] == "true"
        if isE2e {
            Auth.auth().settings?.isAppVerificationDisabledForTesting = true
        }
    }

    func sendCode(
        phoneE164: String,
        completionHandler: @escaping (PhoneAuthBridgeResult?, Error?) -> Void
    ) {
        if let error = ensureFirebaseConfigured() {
            completionHandler(PhoneAuthBridgeResult(verificationId: nil, error: error), nil)
            return
        }
        configureTestingIfNeeded()
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
        if let error = ensureFirebaseConfigured() {
            completionHandler(error, nil)
            return
        }
        configureTestingIfNeeded()
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

private extension FirebasePhoneAuthBridge {
    static func loadOptions() -> FirebaseOptions? {
        guard let path = Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist") else {
            return nil
        }
        return FirebaseOptions(contentsOfFile: path)
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
