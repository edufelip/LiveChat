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

    func testOtpErrorThenSuccessNavigatesHome() {
        let app = launchApp(phoneOverride: "6505553434")
        reachOtpStep(in: app, phone: "6505553434")

        var otpInput = element(in: app, id: OnboardingTags.otpInput)
        XCTAssertTrue(otpInput.waitForExistence(timeout: 5))
        enterText("000000", into: otpInput, app: app)

        let verifyButton = element(in: app, id: OnboardingTags.otpVerify)
        XCTAssertTrue(verifyButton.waitForExistence(timeout: 5))
        XCTAssertTrue(waitForEnabled(verifyButton, timeout: 5))
        tapElement(verifyButton)

        let otpError = element(in: app, id: OnboardingTags.otpError)
        XCTAssertTrue(otpError.waitForExistence(timeout: 5))

        otpInput = element(in: app, id: OnboardingTags.otpInput)
        enterText("123123", into: otpInput, app: app)
        tapElement(verifyButton)

        let successStep = element(in: app, id: OnboardingTags.successStep)
        XCTAssertTrue(successStep.waitForExistence(timeout: 8))

        let successButton = element(in: app, id: OnboardingTags.successButton)
        XCTAssertTrue(successButton.waitForExistence(timeout: 5))
        tapElement(successButton)

        let contactsTab = app.buttons["Contacts"]
        XCTAssertTrue(contactsTab.waitForExistence(timeout: 8))
    }

    func testContactsPermissionFlow() {
        let app = launchApp(phoneOverride: "6505553434", otpOverride: "123123", contactsFlow: true)
        completeOnboarding(in: app, phone: "6505553434", otp: "123123")
        let successButton = element(in: app, id: OnboardingTags.successButton)
        XCTAssertTrue(successButton.waitForExistence(timeout: 8))
        tapElement(successButton)

        let contactsTab = app.buttons["Contacts"]
        XCTAssertTrue(contactsTab.waitForExistence(timeout: 8))
        tapElement(contactsTab)

        let syncButton = app.buttons["Sync Contacts"]
        XCTAssertTrue(syncButton.waitForExistence(timeout: 8))
        tapElement(syncButton)

        let syncComplete = element(in: app, id: "contacts_sync_complete")
        XCTAssertTrue(syncComplete.waitForExistence(timeout: 8))
    }

    func testContactsPermissionDeniedShowsBanner() {
        let app = launchApp(phoneOverride: "6505553434", otpOverride: "123123", contactsFlow: true, contactsDeny: true)
        completeOnboarding(in: app, phone: "6505553434", otp: "123123")
        let successButton = element(in: app, id: OnboardingTags.successButton)
        XCTAssertTrue(successButton.waitForExistence(timeout: 8))
        tapElement(successButton)

        let contactsTab = app.buttons["Contacts"]
        XCTAssertTrue(contactsTab.waitForExistence(timeout: 8))
        tapElement(contactsTab)

        let syncButton = app.buttons["Sync Contacts"]
        XCTAssertTrue(syncButton.waitForExistence(timeout: 8))
        tapElement(syncButton)

        let deniedMessage = app.staticTexts["Enable contacts permission to sync your phonebook."]
        XCTAssertTrue(deniedMessage.waitForExistence(timeout: 8))
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

    func testE2EOnboardingWithFirebase() {
        let phone = ProcessInfo.processInfo.environment["E2E_PHONE"] ?? "6505553434"
        let otp = ProcessInfo.processInfo.environment["E2E_OTP"] ?? "123123"
        let app = launchE2EApp(resetOnboarding: true)

        let phoneStep = element(in: app, id: OnboardingTags.phoneStep)
        XCTAssertTrue(phoneStep.waitForExistence(timeout: 8))

        let phoneInput = element(in: app, id: OnboardingTags.phoneInput)
        XCTAssertTrue(phoneInput.waitForExistence(timeout: 5))
        enterText(phone, into: phoneInput, app: app)

        let continueButton = element(in: app, id: OnboardingTags.phoneContinue)
        XCTAssertTrue(continueButton.waitForExistence(timeout: 5))
        XCTAssertTrue(waitForEnabled(continueButton, timeout: 5))
        tapElement(continueButton)

        let otpStep = element(in: app, id: OnboardingTags.otpStep)
        XCTAssertTrue(otpStep.waitForExistence(timeout: 15))

        let otpInput = element(in: app, id: OnboardingTags.otpInput)
        XCTAssertTrue(otpInput.waitForExistence(timeout: 5))
        enterText(otp, into: otpInput, app: app)

        let verifyButton = element(in: app, id: OnboardingTags.otpVerify)
        XCTAssertTrue(verifyButton.waitForExistence(timeout: 5))
        XCTAssertTrue(waitForEnabled(verifyButton, timeout: 5))
        tapElement(verifyButton)

        let successStep = element(in: app, id: OnboardingTags.successStep)
        XCTAssertTrue(successStep.waitForExistence(timeout: 15))
    }

    private func launchApp(
        phoneOverride: String? = nil,
        otpOverride: String? = nil,
        contactsFlow: Bool = false,
        contactsDeny: Bool = false
    ) -> XCUIApplication {
        let app = XCUIApplication()
        app.launchEnvironment["UITEST_MODE"] = "1"
        app.launchEnvironment["UITEST_RESET_ONBOARDING"] = "1"
        app.launchEnvironment["UITEST_PHONE"] = phoneOverride ?? ""
        app.launchEnvironment["UITEST_OTP"] = otpOverride ?? ""
        app.launchEnvironment["UITEST_CONTACTS_FLOW"] = contactsFlow ? "1" : "0"
        app.launchEnvironment["UITEST_CONTACTS_DENY"] = contactsDeny ? "1" : "0"
        app.launchArguments.append("-ui-testing")
        app.launch()
        return app
    }

    private func launchE2EApp(
        resetOnboarding: Bool
    ) -> XCUIApplication {
        let app = XCUIApplication()
        app.launchEnvironment["E2E_MODE"] = "1"
        if resetOnboarding {
            app.launchEnvironment["UITEST_RESET_ONBOARDING"] = "1"
        }
        app.launchArguments.append("-e2e-testing")
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
        target.press(forDuration: 1.0)
        let selectAll = app.menuItems["Select All"]
        if selectAll.waitForExistence(timeout: 1) {
            selectAll.tap()
        } else {
            let deleteString = String(repeating: XCUIKeyboardKey.delete.rawValue, count: 16)
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

    private func reachOtpStep(in app: XCUIApplication, phone: String) {
        let uiTestMode = element(in: app, id: OnboardingTags.uiTestMode)
        XCTAssertTrue(uiTestMode.waitForExistence(timeout: 5))

        let phoneInput = element(in: app, id: OnboardingTags.phoneInput)
        XCTAssertTrue(phoneInput.waitForExistence(timeout: 5))
        enterText(phone, into: phoneInput, app: app)

        let continueButton = element(in: app, id: OnboardingTags.phoneContinue)
        XCTAssertTrue(continueButton.waitForExistence(timeout: 5))
        XCTAssertTrue(waitForEnabled(continueButton, timeout: 5))
        tapElement(continueButton)

        let otpStep = element(in: app, id: OnboardingTags.otpStep)
        XCTAssertTrue(otpStep.waitForExistence(timeout: 8))
    }

    private func completeOnboarding(in app: XCUIApplication, phone: String, otp: String) {
        reachOtpStep(in: app, phone: phone)

        let otpInput = element(in: app, id: OnboardingTags.otpInput)
        XCTAssertTrue(otpInput.waitForExistence(timeout: 5))
        enterText(otp, into: otpInput, app: app)

        let verifyButton = element(in: app, id: OnboardingTags.otpVerify)
        XCTAssertTrue(verifyButton.waitForExistence(timeout: 5))
        XCTAssertTrue(waitForEnabled(verifyButton, timeout: 5))
        tapElement(verifyButton)

        let successStep = element(in: app, id: OnboardingTags.successStep)
        XCTAssertTrue(successStep.waitForExistence(timeout: 8))
    }

}
