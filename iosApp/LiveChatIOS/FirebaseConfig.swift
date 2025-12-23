import Foundation
import FirebaseCore

enum FirebaseConfig {
    static func configureIfNeeded() -> Bool {
        if FirebaseApp.app() == nil {
            if let options = loadOptions() {
                FirebaseApp.configure(options: options)
            } else {
                FirebaseApp.configure()
            }
        }
        return FirebaseApp.app() != nil
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
}
