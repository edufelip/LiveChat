import SwiftUI
import Foundation

import CountryPicker
#if canImport(PhoneNumberKit)
import PhoneNumberKit
#endif

private struct CountryOption: Identifiable, Equatable {
    var id: String { isoCode }
    let flag: String
    let name: String
    let dialCode: String
    let isoCode: String

    var formattedDialCode: String {
        "+\(dialCode)"
    }

    init(flag: String, name: String, dialCode: String, isoCode: String) {
        self.flag = flag
        self.name = name
        self.dialCode = dialCode
        self.isoCode = isoCode
    }

    init(country: Country) {
        self.flag = country.isoCode.getFlag()
        self.name = country.localizedName
        self.dialCode = country.phoneCode.trimmingCharacters(in: CharacterSet(charactersIn: "+"))
        self.isoCode = country.isoCode
    }

    static func defaultOption() -> CountryOption {
        let countries = CountryManager.shared.getCountries()
        let localeIso = Locale.current.regionCode?.uppercased() ?? "US"
        if let match = countries.first(where: { $0.isoCode.uppercased() == localeIso }) {
            return CountryOption(country: match)
        }
        if let fallback = countries.first(where: { $0.isoCode.uppercased() == "US" }) {
            return CountryOption(country: fallback)
        }
        return countries.first.map(CountryOption.init(country:)) ??
            CountryOption(flag: "🇺🇸", name: "United States", dialCode: "1", isoCode: "US")
    }
}

private struct ThemedColors {
    let gradient: [Color]
    let accent: Color
    let textPrimary: Color
    let textSecondary: Color
    let inputBackground: Color
    let gold: Color = Color(hex: 0xFFD700)
    let goldSoft: Color = Color(hex: 0xFFFACD)

    static func make(for scheme: ColorScheme) -> ThemedColors {
        if scheme == .dark {
            return ThemedColors(
                gradient: [Color(hex: 0x1A2A2A), Color(hex: 0x2A403F)],
                accent: Color(hex: 0x80CBC4),
                textPrimary: Color(hex: 0xD1E0DD),
                textSecondary: Color(hex: 0xD1E0DD).opacity(0.85),
                inputBackground: Color(hex: 0x2F4341)
            )
        } else {
            return ThemedColors(
                gradient: [Color(hex: 0xF0FDFA), Color(hex: 0xCFE8E6)],
                accent: Color(hex: 0xB2DFDB),
                textPrimary: Color(hex: 0x3F5A57),
                textSecondary: Color(hex: 0x3F5A57).opacity(0.8),
                inputBackground: Color(hex: 0xE0F2F1)
            )
        }
    }
}

#if canImport(PhoneNumberKit)
private enum PhoneNumberHelper {
    static let kit = PhoneNumberKit()
}
#endif

struct OnboardingFlowView: View {
    @Environment(\.colorScheme) private var colorScheme

    @State private var currentStep = 0
    @State private var selectedCountry = CountryOption.defaultOption()
    @State private var phoneNumber: String = ""
    @State private var phoneError: String?
    @State private var otp: String = ""
    @State private var countdown = 60
    @State private var timerActive = false

    let onFinished: () -> Void

    private let totalSteps = 3
    var body: some View {
        let colors = ThemedColors.make(for: colorScheme)

        ZStack {
            LinearGradient(
                colors: colors.gradient,
                startPoint: .topLeading,
                endPoint: .bottomTrailing
            )
            .ignoresSafeArea()

            Group {
                switch currentStep {
                case 0:
                    PhoneStep(
                        colors: colors,
                        selectedCountry: $selectedCountry,
                        phoneNumber: $phoneNumber,
                        phoneError: phoneError
                    )
                case 1:
                    OTPStep(
                        colors: colors,
                        otp: $otp,
                        countdown: countdown,
                        timerActive: timerActive,
                        onResend: handleResend,
                        onVerify: handleVerify,
                        canVerify: otp.count == 6
                    )
                default:
                    SuccessStep(
                        colors: colors,
                        onStartChatting: onFinished
                    )
                }
            }
            .animation(.easeInOut, value: currentStep)
        }
        .overlay(alignment: .bottom) {
            StepDots(colors: colors, total: totalSteps, current: currentStep)
                .padding(.bottom, 110)
        }
        .overlay(alignment: .bottom) {
            PrimaryButton(
                colors: colors,
                title: currentStep == 2 ? "Start Chatting" : (currentStep == 1 ? "Verify" : "Continue"),
                enabled: currentStep == 2 || (currentStep == 1 ? otp.count == 6 : isValidPhone),
                action: {
                    switch currentStep {
                    case 0: handleSendCode()
                    case 1: handleVerify()
                    default: onFinished()
                    }
                }
            )
            .padding(.horizontal, 24)
            .padding(.bottom, 40)
        }
        .onChange(of: timerActive) { active in
            if active {
                startTimer()
            }
        }
        .onChange(of: phoneNumber) { _ in
            phoneError = nil
        }
        .onChange(of: selectedCountry) { _ in
            phoneError = nil
        }
    }

    private var isValidPhone: Bool {
        isValidPhoneNumber(digits: phoneNumber, country: selectedCountry)
    }

    private func handleSendCode() {
        guard isValidPhone else {
            phoneError = "Please enter a valid phone number"
            return
        }
        phoneError = nil
        otp = ""
        countdown = 60
        timerActive = true
        currentStep = 1
    }

    private func handleResend() {
        countdown = 60
        timerActive = true
    }

    private func handleVerify() {
        guard otp.count == 6 else { return }
        timerActive = false
        currentStep = 2
    }

    private func startTimer() {
        Timer.scheduledTimer(withTimeInterval: 1, repeats: true) { timer in
            if !timerActive || countdown <= 0 {
                timer.invalidate()
                timerActive = false
            } else {
                countdown -= 1
            }
        }
    }
}

// MARK: - Steps

private struct PhoneStep: View {
    let colors: ThemedColors
    @Binding var selectedCountry: CountryOption
    @Binding var phoneNumber: String
    let phoneError: String?

    var body: some View {
        VStack {
            Spacer().frame(height: 40)
            Text("Enter your phone number")
                .font(.system(size: 34, weight: .bold, design: .serif))
                .foregroundColor(colors.textPrimary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 28)

            Text("We'll send you a code to verify your number.")
                .font(.system(size: 17, weight: .light, design: .rounded))
                .foregroundColor(colors.textSecondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)
                .padding(.top, 8)

            PhoneInputField(
                colors: colors,
                selectedCountry: $selectedCountry,
                phoneNumber: $phoneNumber
            )
            .padding(.top, 36)

            if let phoneError {
                Text(phoneError)
                    .font(.footnote)
                    .foregroundColor(.red)
                    .padding(.top, 10)
            }

            Spacer()
        }
        .padding(.horizontal, 24)
    }
}

private struct OTPStep: View {
    let colors: ThemedColors
    @Binding var otp: String
    let countdown: Int
    let timerActive: Bool
    let onResend: () -> Void
    let onVerify: () -> Void
    let canVerify: Bool

    var body: some View {
        VStack {
            Spacer().frame(height: 40)
            Text("Enter OTP")
                .font(.system(size: 34, weight: .bold, design: .serif))
                .foregroundColor(colors.textPrimary)

            Text("A 6-digit code has been sent to your phone number.")
                .font(.system(size: 17, weight: .light, design: .rounded))
                .foregroundColor(colors.textSecondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)
                .padding(.top, 8)

            OTPField(colors: colors, otp: $otp)
                .padding(.top, 36)

            if timerActive {
                Text("Resend code in \(formatted(countdown))")
                    .font(.system(size: 15, weight: .medium, design: .rounded))
                    .foregroundColor(colors.textSecondary)
                    .padding(.top, 16)
            } else {
                Button("Resend code", action: onResend)
                    .font(.system(size: 15, weight: .medium, design: .rounded))
                    .foregroundColor(colors.accent)
                    .padding(.top, 16)
            }

            Spacer()
        }
        .padding(.horizontal, 24)
    }

    private func formatted(_ seconds: Int) -> String {
        let clamped = max(seconds, 0)
        return String(format: "00:%02d", clamped)
    }
}

private struct SuccessStep: View {
    let colors: ThemedColors
    let onStartChatting: () -> Void

    var body: some View {
        VStack {
            Spacer()
            SuccessIllustration(colors: colors)
                .padding(.bottom, 28)

            Text("You're all set!")
                .font(.system(size: 34, weight: .bold, design: .serif))
                .foregroundColor(colors.textPrimary)

            Text("Your account has been created successfully.")
                .font(.system(size: 18, weight: .light, design: .rounded))
                .foregroundColor(colors.textSecondary)
                .multilineTextAlignment(.center)
                .padding(.horizontal, 32)
                .padding(.top, 8)

            Spacer()
        }
        .padding(.horizontal, 24)
    }
}

// MARK: - Components

private struct PhoneInputField: View {
    let colors: ThemedColors
    @Binding var selectedCountry: CountryOption
    @Binding var phoneNumber: String
    @State private var showPicker = false

    var body: some View {
        HStack(spacing: 16) {
            Button {
                showPicker.toggle()
            } label: {
                HStack {
                    Text("\(selectedCountry.flag) \(selectedCountry.formattedDialCode)")
                        .font(.system(size: 17, weight: .medium, design: .rounded))
                        .foregroundColor(colors.textPrimary)
                    Image(systemName: "chevron.down")
                        .font(.system(size: 12, weight: .semibold))
                        .foregroundColor(colors.textSecondary)
                }
                .frame(maxWidth: .infinity)
                .padding(.vertical, 14)
                .background(colors.inputBackground.opacity(0.9))
                .clipShape(RoundedRectangle(cornerRadius: 18))
            }
            .frame(width: 140)
            .sheet(isPresented: $showPicker) {
                CountryPickerSheet(
                    colors: colors,
                    selectedCountry: $selectedCountry
                )
                .presentationDetents([.medium, .large])
            }

            TextField(
                "Phone number",
                text: Binding(
                    get: {
                        formattedPhoneNumber(for: phoneNumber, isoCode: selectedCountry.isoCode)
                    },
                    set: { newValue in
                        let digits = newValue.filter { $0.isNumber }.prefix(20)
                        phoneNumber = String(digits)
                    }
                )
            )
            .keyboardType(.phonePad)
            .textContentType(.telephoneNumber)
            .disableAutocorrection(true)
            .font(.system(size: 17, weight: .medium, design: .rounded))
            .padding(.vertical, 14)
            .padding(.horizontal, 12)
            .background(colors.inputBackground.opacity(0.9))
            .clipShape(RoundedRectangle(cornerRadius: 18))
            .foregroundColor(colors.textPrimary)
        }
    }
}

private struct CountryPickerSheet: View {
    let colors: ThemedColors
    @Binding var selectedCountry: CountryOption
    @Environment(\.dismiss) private var dismiss
    @State private var searchText = ""
    private let countries: [CountryOption]

    init(colors: ThemedColors, selectedCountry: Binding<CountryOption>) {
        self.colors = colors
        self._selectedCountry = selectedCountry
        let resolvedCountries = CountryManager.shared.getCountries()
            .map(CountryOption.init(country:))
            .sorted { $0.name < $1.name }
        self.countries = resolvedCountries
    }

    private var filteredCountries: [CountryOption] {
        let trimmed = searchText.trimmingCharacters(in: .whitespacesAndNewlines)
        guard trimmed.isEmpty == false else { return countries }
        let numericQuery = trimmed.filter(\.isNumber)
        return countries.filter { option in
            option.name.localizedCaseInsensitiveContains(trimmed)
                || option.isoCode.localizedCaseInsensitiveContains(trimmed)
                || (!numericQuery.isEmpty && option.dialCode.contains(numericQuery))
        }
    }

    var body: some View {
        NavigationStack {
            List(filteredCountries) { option in
                Button {
                    selectedCountry = option
                    dismiss()
                } label: {
                    HStack(spacing: 12) {
                        Text(option.flag)
                            .font(.system(size: 24))
                        VStack(alignment: .leading, spacing: 4) {
                            Text(option.name)
                                .font(.system(size: 17, weight: .medium, design: .rounded))
                                .foregroundColor(colors.textPrimary)
                            Text(option.formattedDialCode)
                                .font(.system(size: 15, weight: .regular, design: .rounded))
                                .foregroundColor(colors.textSecondary)
                        }
                        Spacer()
                        if option == selectedCountry {
                            Image(systemName: "checkmark.circle.fill")
                                .foregroundColor(colors.accent)
                        }
                    }
                    .padding(.vertical, 8)
                }
                .buttonStyle(.plain)
            }
            .listStyle(.plain)
            .searchable(text: $searchText, prompt: "Search countries")
            .navigationTitle("Select Country")
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") {
                        dismiss()
                    }
                }
            }
        }
    }
}

private func formattedPhoneNumber(for digits: String, isoCode: String) -> String {
    guard digits.isEmpty == false else { return "" }
    #if canImport(PhoneNumberKit)
    let formatter = PartialFormatter(
        phoneNumberKit: PhoneNumberHelper.kit,
        defaultRegion: isoCode,
        withPrefix: false
    )
    return formatter.formatPartial(digits)
    #else
    return digits
    #endif
}

private func isValidPhoneNumber(digits: String, country: CountryOption) -> Bool {
    guard digits.isEmpty == false else { return false }
    #if canImport(PhoneNumberKit)
    do {
        _ = try PhoneNumberHelper.kit.parse(
            "+\(country.dialCode)\(digits)",
            withRegion: country.isoCode,
            ignoreType: false
        )
        return true
    } catch {
        return false
    }
    #else
    return digits.count >= 7
    #endif
}

private extension String {
    func getFlag() -> String {
        guard count == 2 else { return "🏳️" }
        let base: UInt32 = 127397
        return uppercased().unicodeScalars.reduce(into: "") { result, scalar in
            if let flagScalar = UnicodeScalar(base + scalar.value) {
                result.append(String(Character(flagScalar)))
            }
        }
    }
}

private struct OTPField: View {
    let colors: ThemedColors
    @Binding var otp: String

    var body: some View {
        ZStack {
            HStack(spacing: 12) {
                ForEach(0..<6, id: \.self) { index in
                    let char = otp.count > index ? String(Array(otp)[index]) : ""
                    Text(char)
                        .frame(width: 46, height: 58)
                        .background(colors.inputBackground.opacity(0.9))
                        .overlay(
                            RoundedRectangle(cornerRadius: 16)
                                .stroke(char.isEmpty ? colors.textSecondary.opacity(0.25) : colors.accent, lineWidth: 1)
                        )
                        .clipShape(RoundedRectangle(cornerRadius: 16))
                        .font(.system(size: 24, weight: .medium, design: .serif))
                        .foregroundColor(colors.textPrimary)
                }
            }

            TextField("", text: Binding(
                get: { otp },
                set: { newValue in
                    let filtered = newValue.filter(\.isNumber)
                    if filtered.count <= 6 {
                        otp = filtered
                    }
                })
            )
            .keyboardType(.numberPad)
            .textContentType(.oneTimeCode)
            .opacity(0.01)
        }
        .padding(.horizontal, 8)
    }
}

private struct StepDots: View {
    let colors: ThemedColors
    let total: Int
    let current: Int

    var body: some View {
        HStack(spacing: 10) {
            ForEach(0..<total, id: \.self) { index in
                Circle()
                    .fill(index == current ? colors.accent : colors.textSecondary.opacity(0.25))
                    .frame(width: 10, height: 10)
            }
        }
    }
}

private struct PrimaryButton: View {
    let colors: ThemedColors
    let title: String
    let enabled: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            Text(title)
                .font(.system(size: 18, weight: .semibold, design: .rounded))
                .frame(maxWidth: .infinity)
                .padding(.vertical, 18)
                .background(enabled ? colors.accent : colors.accent.opacity(0.4))
                .foregroundColor(colors.textPrimary)
                .clipShape(RoundedRectangle(cornerRadius: 999))
        }
        .disabled(!enabled)
    }
}

private struct SuccessIllustration: View {
    let colors: ThemedColors

    var body: some View {
        ZStack {
            Circle()
                .fill(colors.accent.opacity(0.25))
                .frame(width: 220, height: 220)

            Circle()
                .fill(colors.inputBackground.opacity(0.9))
                .frame(width: 170, height: 170)
                .overlay(
                    Circle()
                        .stroke(colors.accent, lineWidth: 6)
                )

            Image(systemName: "checkmark.seal.fill")
                .resizable()
                .scaledToFit()
                .frame(width: 96, height: 96)
                .foregroundColor(colors.accent)

            Image(systemName: "sparkles")
                .resizable()
                .scaledToFit()
                .frame(width: 60, height: 60)
                .foregroundColor(colors.gold)
                .offset(y: 50)
        }
    }
}

// MARK: - Color helpers

private extension Color {
    init(hex: UInt32) {
        let r = Double((hex >> 16) & 0xFF) / 255.0
        let g = Double((hex >> 8) & 0xFF) / 255.0
        let b = Double(hex & 0xFF) / 255.0
        self.init(red: r, green: g, blue: b)
    }
}

#if DEBUG
struct OnboardingFlowView_Previews: PreviewProvider {
    static var previews: some View {
        Group {
            OnboardingFlowView(onFinished: {})
                .preferredColorScheme(.light)
            OnboardingFlowView(onFinished: {})
                .preferredColorScheme(.dark)
        }
    }
}
#endif
