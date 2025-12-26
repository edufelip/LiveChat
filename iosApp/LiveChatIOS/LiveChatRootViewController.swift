import UIKit
import LiveChatCompose

/// Wraps the Kotlin-provided UIViewController and pins it to the full window bounds.
final class LiveChatRootViewController: UIViewController {
    private let composeViewController: UIViewController

    init(
        config: FirebaseRestConfig,
        userId: String,
        idToken: String? = nil
    ) {
        let isUiTest = isUiTestModeEnabled()
        composeViewController = MainViewControllerKt.MainViewController(
            config: config,
            bridgeBundle: LiveChatBridgeFactory.make(config: config),
            userId: userId,
            idToken: idToken,
            phoneContactsProvider: {
                if isUiTest && isContactsFlowEnabled() {
                    return UiTestContactsKt.uiTestContacts()
                }
                return IosContactsProvider.fetchContacts()
            }
        )
        super.init(nibName: nil, bundle: nil)
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        // Fill the whole window; we'll handle padding via Compose insets.
        view.insetsLayoutMarginsFromSafeArea = false
        view.backgroundColor = liveChatBackgroundColor()
        view.accessibilityIdentifier = "live_chat_root"

        addChild(composeViewController)
        let childView = composeViewController.view!
        childView.backgroundColor = liveChatBackgroundColor()
        childView.accessibilityIdentifier = "live_chat_compose_root"
        childView.translatesAutoresizingMaskIntoConstraints = false
        let backgroundView = UIView(frame: .zero)
        backgroundView.translatesAutoresizingMaskIntoConstraints = false
        backgroundView.backgroundColor = liveChatBackgroundColor()
        view.addSubview(backgroundView)
        view.addSubview(childView)

        NSLayoutConstraint.activate([
            // Background fills whole window (including status bar area).
            backgroundView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            backgroundView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            backgroundView.topAnchor.constraint(equalTo: view.topAnchor),
            backgroundView.bottomAnchor.constraint(equalTo: view.bottomAnchor),

            childView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            childView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            childView.topAnchor.constraint(equalTo: view.topAnchor),
            childView.bottomAnchor.constraint(equalTo: view.bottomAnchor),
        ])
        composeViewController.didMove(toParent: self)
    }
}

private func isUiTestModeEnabled() -> Bool {
    let environment = ProcessInfo.processInfo.environment
    let arguments = ProcessInfo.processInfo.arguments
    if environment["E2E_MODE"] == "1" || environment["E2E_MODE"] == "true" || arguments.contains("-e2e-testing") {
        return false
    }
    return environment["UITEST_MODE"] == "1" ||
        environment["XCTestConfigurationFilePath"] != nil ||
        arguments.contains("-ui-testing")
}

private func isContactsFlowEnabled() -> Bool {
    let environment = ProcessInfo.processInfo.environment
    let storedValue = UserDefaults.standard.string(forKey: "UITEST_CONTACTS_FLOW")
    let envValue = environment["UITEST_CONTACTS_FLOW"]
    return envValue == "1" || envValue == "true" || storedValue == "1" || storedValue == "true"
}

private func liveChatBackgroundColor() -> UIColor {
    UIColor { trait in
        if trait.userInterfaceStyle == .dark {
            return UIColor(red: 14.0 / 255.0, green: 21.0 / 255.0, blue: 19.0 / 255.0, alpha: 1.0)
        } else {
            return UIColor(red: 244.0 / 255.0, green: 251.0 / 255.0, blue: 248.0 / 255.0, alpha: 1.0)
        }
    }
}
