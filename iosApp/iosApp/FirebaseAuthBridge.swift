import Foundation
import FirebaseAuth
import LiveChatCompose

final class FirebaseAuthBridge: NSObject, AuthBridge {
    func signOut() {
        _ = FirebaseConfig.configureIfNeeded()
        do {
            try Auth.auth().signOut()
        } catch {
            #if DEBUG
            NSLog("FirebaseAuthBridge.signOut error=%@", error.localizedDescription)
            #endif
        }
    }
}
