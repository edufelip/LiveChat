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
        composeViewController = MainViewControllerKt.MainViewController(
            config: config,
            userId: userId,
            idToken: idToken,
            phoneContactsProvider: { [] }
        )
        super.init(nibName: nil, bundle: nil)
    }

    @available(*, unavailable)
    required init?(coder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        addChild(composeViewController)
        let childView = composeViewController.view!
        childView.translatesAutoresizingMaskIntoConstraints = false
        view.addSubview(childView)
        NSLayoutConstraint.activate([
            childView.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            childView.trailingAnchor.constraint(equalTo: view.trailingAnchor),
            childView.topAnchor.constraint(equalTo: view.topAnchor),
            childView.bottomAnchor.constraint(equalTo: view.bottomAnchor),
        ])
        composeViewController.didMove(toParent: self)
    }
}
