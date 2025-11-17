package com.edufelip.livechat.shared.data

import com.edufelip.livechat.data.mappers.toDomain
import com.edufelip.livechat.data.mappers.toEntity
import com.edufelip.livechat.domain.models.Contact
import com.edufelip.livechat.shared.data.database.ContactEntity
import kotlin.test.Test
import kotlin.test.assertEquals

class ContactMappersTest {
    @Test
    fun domainToEntityRoundTripPreservesIdentity() {
        val domain =
            Contact(
                id = 42,
                name = "Ava",
                phoneNo = "+15550100",
                description = "Designer",
                photo = "https://example.com/avatar.png",
                isRegistered = true,
                firebaseUid = "uid-123",
            )

        val roundTrip = domain.toEntity().toDomain()

        assertEquals(domain, roundTrip)
    }

    @Test
    fun entityToDomainRoundTripPreservesIdentity() {
        val entity =
            ContactEntity(
                id = 99,
                name = "Ben",
                phoneNo = "+15559876",
                description = null,
                photo = null,
                isRegistered = false,
                firebaseUid = null,
            )

        val roundTrip = entity.toDomain().toEntity()

        assertEquals(entity, roundTrip)
    }
}
