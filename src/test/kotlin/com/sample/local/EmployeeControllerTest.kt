package com.sample.local


import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.sample.local.dto.EmployeeRequestDto
import com.sample.local.dto.EmployeeResponseDto
import com.sample.local.entity.*
import com.sample.local.exception.ApplicationException
import com.sample.local.exception.ErrorResponse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.web.server.LocalServerPort
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.http.*
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder.*
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
class EmployeeResourceTest {
    @LocalServerPort
    var randomServerPort = 0

    @Autowired
    private lateinit var testRestTemplate: TestRestTemplate

    @Autowired private lateinit var mongoTemplate: MongoTemplate

    var baseUrl = ""
    var headers = HttpHeaders()

    @BeforeAll
    fun initBeforeAll() {
        mongoTemplate.dropCollection("Employee")
        mongoTemplate.dropCollection("Contact")
        mongoTemplate.dropCollection("Address")
    }

    @BeforeEach
    fun init() {
        baseUrl = "http://localhost:$randomServerPort/"
        headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        val jackson2HttpMessageConverter = MappingJackson2HttpMessageConverter()
        jackson2HttpMessageConverter.objectMapper =  json()
                .modules(JavaTimeModule().addDeserializer(LocalDateTime::class.java,
                        LocalDateTimeDeserializer(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))))
                .featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .build<ObjectMapper>()
        testRestTemplate.restTemplate.messageConverters.add(jackson2HttpMessageConverter)

        mongoTemplate.dropCollection("Employee")
        mongoTemplate.dropCollection("Contact")
        mongoTemplate.dropCollection("Address")
    }

    @Test
    fun testEmployee() {
        val request = HttpEntity(buildRequestDto(), headers)
        val employeeCreateResponse = testRestTemplate.exchange("$baseUrl/demo/employee",
                HttpMethod.POST, request, EmployeeResponseDto::class.java)

        assertTrue(employeeCreateResponse.statusCode == HttpStatus.CREATED, "Expected to match")

        val employeeGetResponse = testRestTemplate.exchange("""$baseUrl/demo/employee/${employeeCreateResponse
                .body?.response?.employees?.get(0)?.employeeId}""",
                HttpMethod.GET, HttpEntity(null, headers), EmployeeResponseDto::class.java)
        assertTrue(employeeGetResponse.statusCode == HttpStatus.OK, "Expected to match")
    }

    @Test
    @Throws(ApplicationException::class)
    fun testEmployeeWithException() {
        assertThrows(ApplicationException::class.java) {
            val request = HttpEntity(buildRequestDto(), headers)
            val errorResponse = testRestTemplate.exchange("$baseUrl/demo/employee",
                    HttpMethod.POST, request, ErrorResponse::class.java)
            assertTrue(errorResponse.statusCode == HttpStatus.INTERNAL_SERVER_ERROR,
                    "Employee Already Exists")
        }
    }

    @Test
    fun testExpectedException() {
        assertThrows(NumberFormatException::class.java) { "One".toInt() }
    }

    fun buildRequestDto(): EmployeeRequestDto {
        val employeeRequestDto: EmployeeRequestDto = EmployeeRequestDto()
        val employeeDto: EmployeeDto = EmployeeDto()
        val contactsDto: ContactDto = ContactDto()
        val addressDto: AddressDto = AddressDto()
        with(addressDto) {
            zipCode = "12345"
            country = "Nameless Country"
            state = "Nameless States"
            city = "Nameless City"
            no = "1"
            street = "Zero Street"
        }
        with(contactsDto) {
            value = "9999999999"
            contactType = ContactType.Mobile
        }
        with(employeeDto) {
            firstName = "First Sample LastName"
            lastName = "First Sample LastName"
            salary = 1000.10
            contacts = listOf(contactsDto)
            address = listOf(addressDto)
        }

        with(employeeRequestDto) {
            employee = employeeDto
        }

        return employeeRequestDto;
    }
}