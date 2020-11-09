package com.sample.local.exception

import com.fasterxml.jackson.annotation.JsonFormat
import org.springframework.http.HttpStatus
import java.time.LocalDateTime

data class ErrorDetails(@JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy hh:mm:ss")
                        val timestamp: LocalDateTime = LocalDateTime.now(),
                        var message: String = "",
                        var trace: String = "",
                        var status: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR) {

    constructor(status: HttpStatus) : this() {
        this.status = status
    }

    constructor(status: HttpStatus, ex: Throwable) : this() {
        this.status = status
        this.message = "Unexpected error"
        this.trace = ex.localizedMessage
    }

    constructor(status: HttpStatus, message: String, ex: Throwable) : this() {
        this.status = status
        this.message = message
        this.trace = ex.localizedMessage
    }
}