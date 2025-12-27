import Foundation
import FirebaseAuth
import LiveChatCompose

final class FirebasePhoneAuthBridge: NSObject, PhoneAuthBridge {
    private func ensureFirebaseConfigured() -> PhoneAuthBridgeError? {
        if FirebaseConfig.configureIfNeeded() == false {
            return PhoneAuthBridgeError(
                domain: "FirebaseAuth",
                code: KotlinLong(value: -1),
                message: "FirebaseApp.configure() did not initialize a default app. GoogleService-Info.plist missing or bundle id mismatch."
            )
        }
        if let missing = FirebaseConfig.missingRequiredOptions() {
            return PhoneAuthBridgeError(
                domain: "FirebaseAuth",
                code: KotlinLong(value: -2),
                message: missing
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
        #if DEBUG
        let disabled = Auth.auth().settings?.isAppVerificationDisabledForTesting ?? false
        NSLog("FirebasePhoneAuthBridge.sendCode phone=%@ disabled=%@", phoneE164, String(disabled))
        #endif
        let provider = PhoneAuthProvider.provider()
        provider.verifyPhoneNumber(phoneE164, uiDelegate: nil) { verificationId, error in
            #if DEBUG
            if let error = error {
                NSLog("FirebasePhoneAuthBridge.sendCode error=%@", error.localizedDescription)
            } else {
                NSLog("FirebasePhoneAuthBridge.sendCode success verificationId=%@", verificationId ?? "nil")
            }
            #endif
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
                self.updateSessionFromFirebaseAuth()
                completionHandler(nil, nil)
            }
        }
    }
}

private extension FirebasePhoneAuthBridge {
    func updateSessionFromFirebaseAuth() {
        guard let user = Auth.auth().currentUser else { return }
        user.getIDTokenForcingRefresh(false) { token, _ in
            MainViewControllerKt.updateLiveChatSession(userId: user.uid, idToken: token)
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
