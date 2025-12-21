import XCTest

final class LiveChatIOSUITests: XCTestCase {
    private enum OnboardingTags {
        static let phoneStep = "phone_step"
        static let phoneInput = "phone_input"
        static let phoneContinue = "phone_continue_button"
        static let phoneError = "phone_error"
        static let otpStep = "otp_step"
        static let otpInput = "otp_input"
        static let otpVerify = "otp_verify_button"
        static let otpError = "otp_error"
        static let uiTestMode = "ui_test_mode"
        static let successStep = "success_step"
        static let successButton = "success_button"
    }

    override func setUp() {
        continueAfterFailure = false
    }

    func testAppLaunches() {
        let app = launchApp()
        XCTAssertTrue(app.exists)
    }

    func testOnboardingHappyPath() {
        let app = launchApp(phoneOverride: "6505553434", otpOverride: "123123")
        let uiTestMode = element(in: app, id: OnboardingTags.uiTestMode)
        XCTAssertTrue(uiTestMode.waitForExistence(timeout: 5))
        let phoneStep = element(in: app, id: OnboardingTags.phoneStep)
        XCTAssertTrue(phoneStep.waitForExistence(timeout: 8))

        let phoneInput = element(in: app, id: OnboardingTags.phoneInput)
        XCTAssertTrue(phoneInput.waitForExistence(timeout: 5))
        enterText("6505553434", into: phoneInput, app: app)

        let continueButton = element(in: app, id: OnboardingTags.phoneContinue)
        XCTAssertTrue(continueButton.waitForExistence(timeout: 5))
        XCTAssertTrue(waitForEnabled(continueButton, timeout: 5))
        tapElement(continueButton)

        let otpStep = element(in: app, id: OnboardingTags.otpStep)
        XCTAssertTrue(otpStep.waitForExistence(timeout: 8))

        let otpInput = element(in: app, id: OnboardingTags.otpInput)
        XCTAssertTrue(otpInput.waitForExistence(timeout: 5))
        enterText("123123", into: otpInput, app: app)

        let verifyButton = element(in: app, id: OnboardingTags.otpVerify)
        XCTAssertTrue(verifyButton.waitForExistence(timeout: 5))
        XCTAssertTrue(waitForEnabled(verifyButton, timeout: 5))
        tapElement(verifyButton)

        let successStep = element(in: app, id: OnboardingTags.successStep)
        XCTAssertTrue(successStep.waitForExistence(timeout: 8))
    }

    func testPhoneValidationError() {
        let app = launchApp()
        let uiTestMode = element(in: app, id: OnboardingTags.uiTestMode)
        XCTAssertTrue(uiTestMode.waitForExistence(timeout: 5))
        let phoneInput = element(in: app, id: OnboardingTags.phoneInput)
        XCTAssertTrue(phoneInput.waitForExistence(timeout: 5))
        enterText("123", into: phoneInput, app: app)

        let continueButton = element(in: app, id: OnboardingTags.phoneContinue)
        XCTAssertTrue(continueButton.waitForExistence(timeout: 5))
        XCTAssertTrue(waitForEnabled(continueButton, timeout: 5))
        tapElement(continueButton)

        let errorText = element(in: app, id: OnboardingTags.phoneError)
        XCTAssertTrue(errorText.waitForExistence(timeout: 5))
    }

    private func launchApp(
        phoneOverride: String? = nil,
        otpOverride: String? = nil
    ) -> XCUIApplication {
        let app = XCUIApplication()
        app.launchEnvironment["UITEST_MODE"] = "1"
        if let phoneOverride {
            app.launchEnvironment["UITEST_PHONE"] = phoneOverride
        }
        if let otpOverride {
            app.launchEnvironment["UITEST_OTP"] = otpOverride
        }
        app.launchArguments.append("-ui-testing")
        app.launch()
        return app
    }

    private func element(in app: XCUIApplication, id: String) -> XCUIElement {
        app.descendants(matching: .any).matching(identifier: id).firstMatch
    }

    private func enterText(
        _ text: String,
        into element: XCUIElement,
        app: XCUIApplication
    ) {
        let target = resolveTextInput(from: element, app: app)
        target.tap()
        let keyboard = app.keyboards.firstMatch
        XCTAssertTrue(keyboard.waitForExistence(timeout: 2))
        if let existing = target.value as? String, !existing.isEmpty {
            let deleteString = String(repeating: XCUIKeyboardKey.delete.rawValue, count: existing.count)
            target.typeText(deleteString)
        }
        target.typeText(text)
    }

    private func resolveTextInput(
        from element: XCUIElement,
        app: XCUIApplication
    ) -> XCUIElement {
        if element.elementType == .textField || element.elementType == .textView {
            return element
        }
        let descendantTextField = element.descendants(matching: .textField).firstMatch
        if descendantTextField.exists {
            return descendantTextField
        }
        let descendantTextView = element.descendants(matching: .textView).firstMatch
        if descendantTextView.exists {
            return descendantTextView
        }
        let appTextField = app.textFields.firstMatch
        if appTextField.exists {
            return appTextField
        }
        let appTextView = app.textViews.firstMatch
        if appTextView.exists {
            return appTextView
        }
        return element
    }

    private func waitForEnabled(
        _ element: XCUIElement,
        timeout: TimeInterval
    ) -> Bool {
        let predicate = NSPredicate(format: "enabled == true")
        let expectation = XCTNSPredicateExpectation(predicate: predicate, object: element)
        return XCTWaiter().wait(for: [expectation], timeout: timeout) == .completed
    }

    private func tapElement(_ element: XCUIElement) {
        let coordinate = element.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.5))
        coordinate.tap()
    }

}
