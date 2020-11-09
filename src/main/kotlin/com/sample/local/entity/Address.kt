package com.sample.local.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class Address(
        @Id
        var addressId: Long = -1,
        var no: String = "",
        var street: String = "",
        var city: String = "",
        var state: String = "",
        var country: String = "",
        var zipCode: String = "",
        var isAddressActive: Boolean = true
) {}