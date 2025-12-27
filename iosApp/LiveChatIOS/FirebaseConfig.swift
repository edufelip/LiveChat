import Foundation
import FirebaseCore

enum FirebaseConfig {
    private static var didConfigure = false

    static func configureIfNeeded() -> Bool {
        if didConfigure {
            return true
        }
        if let options = loadOptions() {
            NSLog("FirebaseConfig: configuring with GoogleService-Info.plist")
            FirebaseApp.configure(options: options)
        } else {
            NSLog("FirebaseConfig: configuring with default options (plist missing or unreadable)")
            FirebaseApp.configure()
        }
        didConfigure = true

        if let missing = missingRequiredOptions() {
            NSLog("FirebaseConfig: missing required options: %@", missing)
        }

        return true
    }

    static func missingRequiredOptions() -> String? {
        guard let path = Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist") else {
            return "GoogleService-Info.plist not found in app bundle."
        }
        guard let dict = NSDictionary(contentsOfFile: path) as? [String: Any] else {
            return "GoogleService-Info.plist could not be read."
        }
        let requiredKeys = ["API_KEY", "PROJECT_ID", "GOOGLE_APP_ID", "CLIENT_ID", "REVERSED_CLIENT_ID"]
        let missing = requiredKeys.filter { key in
            guard let value = dict[key] as? String else { return true }
            return value.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
        }
        if missing.isEmpty {
            return nil
        }
        return "GoogleService-Info.plist missing keys: \(missing.joined(separator: ", ")). Download the full file from Firebase."
    }

    private static func loadOptions() -> FirebaseOptions? {
        guard let path = Bundle.main.path(forResource: "GoogleService-Info", ofType: "plist") else {
            return nil
        }
        return FirebaseOptions(contentsOfFile: path)
    }

    static func ensureConfiguredForBridge(name: String) {
        _ = configureIfNeeded()
        if !didConfigure {
            NSLog("FirebaseConfig: %@ created before FirebaseApp was configured", name)
        }
    }
}
