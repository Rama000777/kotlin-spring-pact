package com.sample.local.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

data class ContactDto(
        var contactId: Long = -1,
        var contactType: ContactType,
        var value: String
) {
    constructor() : this(-1,
            ContactType.Mobile,
            "")

    constructor(contactType: ContactType, value: String) : this() {
        this.contactType = contactType
        this.value = value
    }
}