import Contacts
import Foundation
import LiveChatCompose

enum IosContactsProvider {
    static func fetchContacts() -> [DomainContact] {
        let store = CNContactStore()
        let status = CNContactStore.authorizationStatus(for: .contacts)
        switch status {
        case .authorized:
            break
        case .notDetermined:
            let semaphore = DispatchSemaphore(value: 0)
            var granted = false
            store.requestAccess(for: .contacts) { didGrant, _ in
                granted = didGrant
                semaphore.signal()
            }
            _ = semaphore.wait(timeout: .now() + 5)
            if !granted {
                return []
            }
        default:
            return []
        }
        let keys: [CNKeyDescriptor] = [
            CNContactGivenNameKey as CNKeyDescriptor,
            CNContactFamilyNameKey as CNKeyDescriptor,
            CNContactPhoneNumbersKey as CNKeyDescriptor,
        ]
        var results: [DomainContact] = []
        let request = CNContactFetchRequest(keysToFetch: keys)
        request.sortOrder = .givenName
        do {
            try store.enumerateContacts(with: request) { contact, _ in
                let name = [contact.givenName, contact.familyName]
                    .filter { !$0.isEmpty }
                    .joined(separator: " ")
                let displayName = name.isEmpty ? "Unknown" : name
                for phone in contact.phoneNumbers {
                    let value = phone.value.stringValue.trimmingCharacters(in: .whitespacesAndNewlines)
                    if value.isEmpty { continue }
                    let domainContact = DomainContact(
                        id: Int64(results.count),
                        name: displayName,
                        phoneNo: value,
                        description: nil,
                        photo: nil,
                        isRegistered: false,
                        firebaseUid: nil
                    )
                    results.append(domainContact)
                }
            }
        } catch {
            return []
        }
        return results
    }
}
