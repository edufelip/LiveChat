package com.project.livechat.data.providers

import com.google.firebase.firestore.FirebaseFirestore
import com.project.livechat.data.contracts.IContactsRemoteData
import com.project.livechat.domain.models.Contact
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.conflate
import javax.inject.Inject

class ContactsRemoteData @Inject constructor(
    fireStore: FirebaseFirestore
) : IContactsRemoteData {

    private val collection = fireStore.collection("users")

    override fun checkContacts(phoneContacts: List<Contact>): Flow<Contact> {
        val flow = callbackFlow {
            for (phoneContact in phoneContacts) {
                collection.whereGreaterThanOrEqualTo("phone_num", phoneContact.phoneNo)
                    .get()
                    .addOnSuccessListener {
                        trySend(phoneContact)
                    }
                    .addOnFailureListener {
                        it.printStackTrace()
                    }
            }
            awaitClose()
        }.conflate()

        return flow
    }
}
