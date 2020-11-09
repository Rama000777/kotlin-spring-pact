package com.sample.local.dto

import com.sample.local.entity.EmployeeDto

class EmployeeRequestDto {
    var employee: EmployeeDto

    constructor(employee: EmployeeDto) {
        this.employee = employee
    }

    constructor() {
        this.employee = EmployeeDto()
    }
}