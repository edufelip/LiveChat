# Contacts Requirements

## R-027: Contacts Permission
The app shall request system-level permission to access device contacts.

## R-028: Remote Discovery
The system shall use the `phoneExistsMany` cloud function to verify which phone numbers are registered users.

## R-029: Contact List Segregation
The app shall display contacts in two sections: "On LiveChat" and "Invite to LiveChat".

## R-030: Sync Fingerprinting
The system shall generate a fingerprint of the local contact list and only perform a full sync if the fingerprint changes.

## R-031: Invitation Sharing
The system shall utilize the platform's native share sheet for sending invite links.
