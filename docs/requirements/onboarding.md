# Onboarding Functional Requirements

## R-001: Branding Presentation
The app shall display a welcome screen with the LiveChat logo and a brief value proposition.

## R-002: Terms and Privacy Acceptance
The app shall provide links to the Terms of Service and Privacy Policy, and state that proceeding implies acceptance.

## R-003: Country Dialing Code Selection
The system shall provide a searchable list of countries with their corresponding ISO codes and international dialing prefixes.

## R-004: Phone Number Formatting
The system shall automatically format the phone number input based on the selected country's standards.

## R-005: Phone Number Validation
The system shall validate the phone number length and format before allowing the user to request a verification code.

## R-006: Session Persistence
The app shall check for an active Firebase user session on startup and bypass the welcome/onboarding screens if a valid session exists.
