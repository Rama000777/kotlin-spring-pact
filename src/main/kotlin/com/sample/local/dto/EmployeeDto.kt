package com.sample.local.entity

import org.springframework.data.mongodb.core.mapping.Document

data class EmployeeDto(
        var employeeId: Long = -1,
        var firstName: String = "",
        var lastName: String = "",
        var salary: Double = 0.0,
        var address: List<AddressDto>,
        var contacts: List<ContactDto>
) {
    constructor() : this(-1,
            "",
            "",
            0.0,
            ArrayList<AddressDto>(),
            ArrayList<ContactDto>())

    constructor(firstName: String, lastName: String, salary: Double,
                address: List<AddressDto>, contacts: List<ContactDto>) : this() {
        this.firstName = firstName
        this.lastName = lastName
        this.address = address
        this.contacts = contacts
        this.salary = salary
    }
}