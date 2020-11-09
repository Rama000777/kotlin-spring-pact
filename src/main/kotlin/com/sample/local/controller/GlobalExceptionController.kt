package com.sample.local.controller

import com.sample.local.exception.ApplicationException
import com.sample.local.exception.ErrorDetails
import com.sample.local.exception.ErrorResponse
import org.springframework.beans.ConversionNotSupportedException
import org.springframework.beans.TypeMismatchException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatus.*
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.http.converter.HttpMessageNotWritableException
import org.springframework.lang.Nullable
import org.springframework.util.CollectionUtils
import org.springframework.validation.BindException
import org.springframework.web.HttpMediaTypeNotAcceptableException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingPathVariableException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.ServletRequestBindingException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.ServletWebRequest
import org.springframework.web.context.request.WebRequest
import org.springframework.web.context.request.async.AsyncRequestTimeoutException
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.multipart.support.MissingServletRequestPartException
import org.springframework.web.servlet.NoHandlerFoundException
import org.springframework.web.util.WebUtils
import java.io.IOException
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * ResponseEntityExceptionHandler as Base
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ApplicationException::class)
    fun handleApplicationException(exception: ApplicationException, response: HttpServletResponse,
                                   request: HttpServletRequest): ResponseEntity<ErrorResponse> {
        response.status = INTERNAL_SERVER_ERROR.value()
        return buildResponseEntity(ErrorDetails(INTERNAL_SERVER_ERROR,
                request.requestURL.toString(), exception))
    }

    private fun <E : Throwable> error(exception: Throwable, httpStatus: HttpStatus,
                                      details: StringBuffer): ResponseEntity<ErrorResponse> {
        return buildResponseEntity(ErrorDetails(httpStatus,
                details.toString(), exception))
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleHttpRequestMethodNotSupported(
            exception: HttpRequestMethodNotSupportedException, headers: HttpHeaders,
            status: HttpStatus, request: WebRequest): ResponseEntity<ErrorResponse> {
        val supportedMethods = exception.supportedHttpMethods
        if (!CollectionUtils.isEmpty(supportedMethods)) {
            headers.allow = supportedMethods!!
        }
        val builder = StringBuilder()
        builder.append("Media type is not supported. Supported media types are ")
        exception.supportedHttpMethods?.forEach { t -> builder.append(t).append(", ") }
        return buildResponseEntity(ErrorDetails(METHOD_NOT_ALLOWED,
                builder.substring(0, builder.length - 2), exception))
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun handleHttpMediaTypeNotSupported(
            exception: HttpMediaTypeNotSupportedException, headers: HttpHeaders,
            status: HttpStatus, request: WebRequest): ResponseEntity<ErrorResponse> {
        val mediaTypes = exception.supportedMediaTypes
        if (!CollectionUtils.isEmpty(mediaTypes)) {
            headers.accept = mediaTypes
        }
        val builder = StringBuilder()
        builder.append(exception.contentType)
        builder.append(" media type is not supported. Supported media types are ")
        exception.supportedMediaTypes.forEach { t -> builder.append(t).append(", ") }
        return buildResponseEntity(ErrorDetails(UNSUPPORTED_MEDIA_TYPE,
                builder.substring(0, builder.length - 2), exception))
    }

    @ExceptionHandler(HttpMediaTypeNotAcceptableException::class)
    protected fun handleHttpMediaTypeNotAcceptable(
            exception: HttpMediaTypeNotAcceptableException, headers: HttpHeaders,
            status: HttpStatus, request: WebRequest): ResponseEntity<ErrorResponse> {
        val builder = StringBuilder()
        builder.append("Supported media types are ")
        exception.supportedMediaTypes.forEach { t -> builder.append(t).append(", ") }
        return buildResponseEntity(ErrorDetails(NOT_ACCEPTABLE,
                builder.substring(0, builder.length - 2), exception))
    }

    @ExceptionHandler(MissingPathVariableException::class)
    protected fun handleMissingPathVariable(
            exception: MissingPathVariableException, headers: HttpHeaders,
            status: HttpStatus, request: WebRequest): ResponseEntity<ErrorResponse> {
        val errorDetails = ErrorDetails(status)
        errorDetails.trace = exception.message.toString()
        return buildResponseEntity(errorDetails)
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    protected fun handleMissingServletRequestParameter(
            exception: MissingServletRequestParameterException, headers: HttpHeaders,
            status: HttpStatus, request: WebRequest): ResponseEntity<ErrorResponse> {
        val errorDetails = ErrorDetails(status)
        errorDetails.trace = exception.message.toString()
        return buildResponseEntity(errorDetails)
    }

    @ExceptionHandler(ServletRequestBindingException::class)
    protected fun handleServletRequestBindingException(
            exception: ServletRequestBindingException, headers: HttpHeaders,
            status: HttpStatus, request: WebRequest): ResponseEntity<ErrorResponse> {
        val errorDetails = ErrorDetails(status)
        errorDetails.trace = exception.message.toString()
        return buildResponseEntity(errorDetails)
    }

    @ExceptionHandler(ConversionNotSupportedException::class)
    protected fun handleConversionNotSupported(
            exception: ConversionNotSupportedException, headers: HttpHeaders,
            status: HttpStatus, request: WebRequest): ResponseEntity<ErrorResponse> {
        val errorDetails = ErrorDetails(status)
        errorDetails.trace = exception.message.toString()
        return buildResponseEntity(errorDetails)
    }

    @ExceptionHandler(TypeMismatchException::class)
    protected fun handleTypeMismatch(
            exception: TypeMismatchException, headers: HttpHeaders,
            status: HttpStatus, request: WebRequest): ResponseEntity<ErrorResponse> {
        val errorDetails = ErrorDetails(status)
        errorDetails.trace = exception.message.toString()
        return buildResponseEntity(errorDetails)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    protected fun handleHttpMessageNotReadable(
            exception: HttpMessageNotReadableException, headers: HttpHeaders,
            status: HttpStatus, request: WebRequest): ResponseEntity<ErrorResponse> {
        val error = "Malformed JSON request"
        return buildResponseEntity(ErrorDetails(BAD_REQUEST, error, exception))
    }

    @ExceptionHandler(HttpMessageNotWritableException::class)
    protected fun handleHttpMessageNotWritable(
            exception: HttpMessageNotWritableException, headers: HttpHeaders,
            status: HttpStatus, request: WebRequest): ResponseEntity<ErrorResponse> {
        val error = "Error writing JSON output"
        return buildResponseEntity(ErrorDetails(INTERNAL_SERVER_ERROR, error, exception))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    protected fun handleMethodArgumentNotValid(
            exception: MethodArgumentNotValidException, headers: HttpHeaders,
            status: HttpStatus, request: WebRequest): ResponseEntity<ErrorResponse> {
        val errorDetails = ErrorDetails(status)
        errorDetails.trace = exception.message.toString()
        return buildResponseEntity(errorDetails)
    }

    @ExceptionHandler(MissingServletRequestPartException::class)
    protected fun handleMissingServletRequestPart(
            exception: MissingServletRequestPartException, headers: HttpHeaders,
            status: HttpStatus, request: WebRequest): ResponseEntity<ErrorResponse> {
        val errorDetails = ErrorDetails(status)
        errorDetails.trace = exception.message.toString()
        return buildResponseEntity(errorDetails)
    }

    @ExceptionHandler(BindException::class)
    protected fun handleBindException(
            exception: BindException, headers: HttpHeaders, status: HttpStatus,
            request: WebRequest): ResponseEntity<ErrorResponse> {
        val errorDetails = ErrorDetails(status)
        errorDetails.trace = exception.message.toString()
        return buildResponseEntity(errorDetails)
    }

    @ExceptionHandler(NoHandlerFoundException::class)
    protected fun handleNoHandlerFoundException(
            exception: NoHandlerFoundException, headers: HttpHeaders, status: HttpStatus,
            request: WebRequest): ResponseEntity<ErrorResponse> {
        val errorDetails = ErrorDetails(status)
        var httpRequest = request as HttpServletRequest
        errorDetails.message = String.format("Could not find the %s method for URL %s",
                httpRequest.method, request.requestURL)
        errorDetails.trace = exception.message.toString()
        return buildResponseEntity(errorDetails)
    }

    @Nullable
    @ExceptionHandler(AsyncRequestTimeoutException::class)
    protected fun handleAsyncRequestTimeoutException(
            exception: AsyncRequestTimeoutException, headers: HttpHeaders,
            status: HttpStatus, webRequest: WebRequest): ResponseEntity<ErrorResponse>? {
        if (webRequest is ServletWebRequest) {
            val response = webRequest.response
            if (response != null && response.isCommitted) {
                return null
            }
        }
        val errorDetails = ErrorDetails(status)
        errorDetails.trace = exception.message.toString()
        return buildResponseEntity(errorDetails)
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    protected fun handleMethodArgumentTypeMismatch(exception: MethodArgumentTypeMismatchException,
                                                   request: WebRequest): ResponseEntity<ErrorResponse> {
        val errorDetails = ErrorDetails(BAD_REQUEST)
        errorDetails.message = String.format("The parameter '%s' of value '%s' could not be converted to type '%s'",
                exception.name, exception.value, exception.requiredType!!.simpleName)
        errorDetails.trace = exception.message.toString()
        return buildResponseEntity(errorDetails)
    }

    private fun buildResponseEntity(errorDetails: ErrorDetails): ResponseEntity<ErrorResponse> {
        return ResponseEntity(
                ErrorResponse(errorDetails),
                errorDetails.status
        )
    }
}
