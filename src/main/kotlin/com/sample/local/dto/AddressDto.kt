package com.sample.local.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.http.HttpStatus

data class AddressDto(
        var addressId: Long = -1,
        var no: String = "",
        var street: String = "",
        var city: String = "",
        var state: String = "",
        var country: String = "",
        var zipCode: String = ""
) {
    constructor() : this(-1,
            "",
            "", "", "", "", "")

    constructor(no: String, street: String, city: String,
                state: String, country: String, zipCode: String) : this() {
        this.no = no
        this.street = street
        this.city = city
        this.state = state
        this.country = country
        this.zipCode = zipCode
    }
}