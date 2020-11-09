package com.sample.local.entity

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document

@Document
data class Employee(
        @Id
        var employeeId: Long = -1,
        var firstName: String = "",
        var lastName: String = "",
        var salary : Double = 0.0,
        var address: Set<Long>,
        var contacts: Set<Long>
) {}