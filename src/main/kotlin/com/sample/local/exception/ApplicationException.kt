package com.sample.local.exception

class ApplicationException : Exception {
    constructor(message: String) : super(message) {}
    constructor(message: String, exception: Throwable?) : super(message, exception) {}

    companion object {
        private const val serialVersionUID = -5163852063186094910L
    }
}
