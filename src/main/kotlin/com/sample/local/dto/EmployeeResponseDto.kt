package com.sample.local.dto

import com.sample.local.entity.ResponseDto

class EmployeeResponseDto {
    var response: ResponseDto

    constructor(response: ResponseDto) {
        this.response = response
    }

    constructor() {
        this.response = ResponseDto()
    }
}