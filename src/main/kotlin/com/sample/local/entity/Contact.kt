package com.sample.local.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class Contact(
        @Id
        var contactId: Long = -1,
        var contactType: ContactType,
        var value: String
) {}