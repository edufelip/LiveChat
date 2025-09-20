import Foundation
import LiveChatShared

final class KoinHolder {
    static let shared = KoinHolder()

    private var koinApplication: Koin_coreKoinApplication?

    func startIfNeeded(config: FirebaseRestConfig) {
        guard koinApplication == nil else { return }
        koinApplication = KoinInitializerIosKt.startKoinForiOS(config: config)
    }

    func stop() {
        koinApplication?.close()
        koinApplication = nil
    }

    func updateSession(userId: String, idToken: String? = nil) {
        let session = UserSession(
            userId: userId,
            idToken: idToken ?? "",
            refreshToken: nil,
            expiresAtEpochMillis: nil
        )
        IosKoinBridge.shared.sessionProvider().setSession(session: session)
    }
}
