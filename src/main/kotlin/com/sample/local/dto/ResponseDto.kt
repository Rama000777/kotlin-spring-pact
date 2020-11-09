package com.sample.local.entity

data class ResponseDto(var employees: List<EmployeeDto>?,
                       var status: String?) {
    constructor() : this(ArrayList(),
            "")

    constructor(employees: List<EmployeeDto>) : this() {
        this.employees = employees
    }

    constructor(status: String) : this() {
        this.status = status
    }
}