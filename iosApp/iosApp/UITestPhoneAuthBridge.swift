import Foundation
import LiveChatCompose

final class UITestPhoneAuthBridge: NSObject, PhoneAuthBridge {
    private let verificationId = "ui-test-verification-id"
    private let validCode = "123123"

    func sendCode(
        phoneE164: String,
        completionHandler: @escaping (PhoneAuthBridgeResult?, Error?) -> Void
    ) {
        NSLog("UITestPhoneAuthBridge.sendCode called for %@", phoneE164)
        let result = PhoneAuthBridgeResult(verificationId: verificationId, error: nil)
        completionHandler(result, nil)
    }

    func verifyCode(
        verificationId: String,
        code: String,
        completionHandler: @escaping (PhoneAuthBridgeError?, Error?) -> Void
    ) {
        NSLog("UITestPhoneAuthBridge.verifyCode called for %@", verificationId)
        if code == validCode {
            completionHandler(nil, nil)
        } else {
            let error = PhoneAuthBridgeError(
                domain: "ui-test",
                code: KotlinLong(value: 17044),
                message: "Invalid verification code"
            )
            completionHandler(error, nil)
        }
    }
}
