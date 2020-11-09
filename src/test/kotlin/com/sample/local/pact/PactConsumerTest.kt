import au.com.dius.pact.consumer.MockServer
import au.com.dius.pact.consumer.dsl.PactDslJsonBody
import au.com.dius.pact.consumer.dsl.PactDslWithProvider
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt
import au.com.dius.pact.consumer.junit5.PactTestFor
import au.com.dius.pact.core.model.RequestResponsePact
import au.com.dius.pact.core.model.annotations.Pact
import au.com.dius.pact.core.model.annotations.PactFolder
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.sample.local.dto.EmployeeRequestDto
import com.sample.local.dto.EmployeeResponseDto
import com.sample.local.entity.AddressDto
import com.sample.local.entity.ContactDto
import com.sample.local.entity.ContactType
import com.sample.local.entity.EmployeeDto
import org.apache.http.client.config.RequestConfig
import org.apache.http.config.RegistryBuilder
import org.apache.http.conn.socket.ConnectionSocketFactory
import org.apache.http.conn.socket.PlainConnectionSocketFactory
import org.apache.http.conn.ssl.NoopHostnameVerifier
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.conn.ssl.TrustSelfSignedStrategy
import org.apache.http.impl.client.CloseableHttpClient
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler
import org.apache.http.impl.client.HttpClients
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager
import org.apache.http.ssl.SSLContextBuilder
import org.apache.http.ssl.SSLContexts
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod.*
import org.springframework.http.HttpStatus.*
import org.springframework.http.MediaType
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.web.client.RestTemplate
import java.security.KeyManagementException
import java.security.KeyStoreException
import java.security.NoSuchAlgorithmException
import java.util.*
import kotlin.collections.HashMap
import kotlin.collections.set


@ExtendWith(PactConsumerTestExt::class)
@PactTestFor(providerName = "kotlinProviderDemo", port = "8081")
@TestMethodOrder(MethodOrderer.OrderAnnotation::class)
@PactFolder("src/test/resources/pact")
class PactConsumerTest {

    private val log: Logger = LoggerFactory.getLogger(PactConsumerTest::class.java)

    private val mapper = jacksonObjectMapper()
    private var employeeDto = EmployeeDto()
    private var addressDto = AddressDto()
    private var contactDto = ContactDto()
    private val testRestTemplate: TestRestTemplate = TestRestTemplate()
    private val headers = HttpHeaders()
    private val maxTotal = 10
    private val defaultMaxPerRoute = 10
    private val socketTimeout = 5000
    private val connectTimeout = 5000
    private val connectionRequestTimeout = 5000

    @BeforeEach
    fun init() {
        loadCustomConverters()
        addressDto = AddressDto("01", "Funny Street",
                "Funny City", "Funny State", "Funny Country", "50001")
        contactDto = ContactDto(ContactType.Mobile, "+12132132134")
        employeeDto = EmployeeDto("Funny FirstName", "Funny LastName",
                5000.50, listOf(addressDto), listOf(contactDto))
        headers.add("Content-Type", APPLICATION_JSON_VALUE)
        System.setProperty("setpact.writer.overwrite", "true")
    }

    private fun buildRequestForEmployee(): PactDslJsonBody {
        val request: PactDslJsonBody = PactDslJsonBody()
        request.`object`("employee").numberType("employeeId", 1)
                .stringType("firstName", "Funny FirstName")
                .stringType("lastName", "Funny LastName")
                .decimalType("salary", 5000.50)
                .eachLike("address")
                .numberType("addressId", 1)
                .stringType("no", "01")
                .stringType("street", "Funny Street")
                .stringType("city", "Funny City")
                .stringType("state", "Funny State")
                .stringType("country", "Funny Country")
                .stringType("zipCode", "50001")
                .closeObject()
                .closeArray()
                .eachLike("contacts")
                .numberType("contactId", 1)
                .stringType("contactType", "Mobile")
                .stringType("value", "+12132132134")
                .closeObject()
                .closeArray()
                .closeObject()
        return request
    }

    private fun buildResponseForEmployee(): PactDslJsonBody {
        val response: PactDslJsonBody = PactDslJsonBody()
        response.`object`("response")
                .eachLike("employees")
                .numberType("employeeId", 1)
                .stringType("firstName", "Funny FirstName")
                .stringType("lastName", "Funny LastName")
                .decimalType("salary", 5000.5)
                .eachLike("address")
                .numberType("addressId", 1)
                .stringType("no", "01")
                .stringType("street", "Funny Street")
                .stringType("city", "Funny City")
                .stringType("state", "Funny State")
                .stringType("country", "Funny Country")
                .stringType("zipCode", "50001")
                .closeObject()
                .closeArray()
                .eachLike("contacts")
                .numberType("contactId", 1)
                .stringType("contactType", "Mobile")
                .stringType("value", "+12132132134")
                .closeObject()
                .closeArray()
                .closeObject()
                .closeArray()
                .closeObject()
        return response
    }

    @Pact(consumer = "kotlinConsumerDemo", provider = "kotlinProviderDemo")
    fun createEmployee(builder: PactDslWithProvider): RequestResponsePact {
        val response: PactDslJsonBody = PactDslJsonBody()
        response.`object`("response")
                .stringType("status", "Employee Created Successfully")
                .closeObject()
        return builder.given("State 1: Create Employee")
                .uponReceiving("Create Employee")
                .path("/demo/employee")
                .method(POST.name)
                .headers(headersPact())
                .body(buildRequestForEmployee())
                .willRespondWith().status(CREATED.value())
                .headers(headersPact())
                .body(response)
                .toPact()
    }

    @Pact(consumer = "kotlinConsumerDemo", provider = "kotlinProviderDemo")
    fun createEmployeeExists(builder: PactDslWithProvider): RequestResponsePact {
        val response: PactDslJsonBody = PactDslJsonBody()
        response.`object`("response")
                .timestamp("timestamp", "dd-mm-YYYY hh:mm:ss", Date())
                .stringType("message", "http://localhost:8081/demo/employee")
                .stringType("trace", "Employee already exists")
                .stringType("status", "INTERNAL_SERVER_ERROR")
                .closeObject()
        return builder.given("State 2: Create Existing Employee")
                .uponReceiving("Create Existing Employee")
                .path("/demo/employee")
                .method(POST.name)
                .headers(headersPact())
                .body(buildRequestForEmployee())
                .willRespondWith().status(INTERNAL_SERVER_ERROR.value())
                .headers(headersPact())
                .body(response)
                .toPact()
    }

    @Pact(consumer = "kotlinConsumerDemo", provider = "kotlinProviderDemo")
    fun updateEmployee(builder: PactDslWithProvider): RequestResponsePact {
        val response: PactDslJsonBody = PactDslJsonBody()
        response.`object`("response")
                .stringType("status", "Employee Updated Successfully")
                .closeObject()
        return builder.given("State 3: Update Employee")
                .uponReceiving("Update Employee")
                .path("/demo/employee")
                .method(PUT.name)
                .headers(headersPact())
                .body(buildRequestForEmployee())
                .willRespondWith().status(OK.value())
                .headers(headersPact())
                .body(response)
                .toPact()
    }

    @Pact(consumer = "kotlinConsumerDemo", provider = "kotlinProviderDemo")
    fun getEmployeeByName(builder: PactDslWithProvider): RequestResponsePact {
        return builder.given("State 4: Get Employee By Name")
                .uponReceiving("Get Employee By Name")
                .path("/demo/employee/firstName/Funny FirstName/lastName/Funny LastName")
                .method(GET.name)
                .body("")
                .willRespondWith().status(OK.value())
                .headers(headersPact())
                .body(buildResponseForEmployee())
                .toPact()
    }

    @Pact(consumer = "kotlinConsumerDemo", provider = "kotlinProviderDemo")
    fun getEmployeeById(builder: PactDslWithProvider): RequestResponsePact {
        return builder.given("State 5: Get Employee By Id")
                .uponReceiving("Get Employee By ID")
                .path("/demo/employee/1")
                .method(GET.name)
                .body("")
                .willRespondWith().status(OK.value())
                .headers(headersPact())
                .body(buildResponseForEmployee())
                .toPact()
    }

    @Pact(consumer = "kotlinConsumerDemo", provider = "kotlinProviderDemo")
    fun getEmployeeBySalary(builder: PactDslWithProvider): RequestResponsePact {
        return builder.given("State 6: Get Employee By Salary Range")
                .uponReceiving("Get Employee By Salary Range")
                .path("/demo/employee/salary/low/2000/high/6000")
                .method(GET.name)
                .body("")
                .willRespondWith().status(OK.value())
                .headers(headersPact())
                .body(buildResponseForEmployee())
                .toPact()
    }

    @Pact(consumer = "kotlinConsumerDemo", provider = "kotlinProviderDemo")
    fun deleteEmployeeByName(builder: PactDslWithProvider): RequestResponsePact {
        val response: PactDslJsonBody = PactDslJsonBody()
        response.`object`("response")
                .stringType("status", "Employee Deleted Successfully")
                .closeObject()
        return builder.given("State 7: Delete Employee By Name")
                .uponReceiving("Delete Employee By Name")
                .path("/demo/employee/firstName/Funny FirstName/lastName/Funny LastName")
                .method(DELETE.name)
                .body("")
                .willRespondWith().status(OK.value())
                .headers(headersPact())
                .body(response)
                .toPact()
    }

    @Pact(consumer = "kotlinConsumerDemo", provider = "kotlinProviderDemo")
    fun deleteEmployeeById(builder: PactDslWithProvider): RequestResponsePact {
        val response: PactDslJsonBody = PactDslJsonBody()
        response.`object`("response")
                .stringType("status", "Employee Deleted Successfully")
                .closeObject()
        return builder.given("State 8: Delete Employee By Id")
                .uponReceiving("Delete Employee By Id")
                .path("/demo/employee/1")
                .method(DELETE.name)
                .body("")
                .willRespondWith().status(OK.value())
                .headers(headersPact())
                .body(response)
                .toPact()
    }

    fun headersPact(): HashMap<String, String> {
        val headers = HashMap<String, String>()
        headers["Content-Type"] = APPLICATION_JSON_VALUE
        return headers
    }

    @Test
    @PactTestFor(pactMethod = "createEmployee")
    @Order(1)
    fun testCreateEmployee(mockServer: MockServer) {
        val headers = HttpHeaders()
        headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE)
        var request: HttpEntity<EmployeeRequestDto> = HttpEntity<EmployeeRequestDto>(EmployeeRequestDto(employeeDto),
                headers)
        val response: ResponseEntity<EmployeeResponseDto> = testRestTemplate
                .exchange("${mockServer.getUrl()}/demo/employee",
                        POST, request, EmployeeResponseDto::class.java)
        assertEquals(response.statusCode, CREATED, "Expected the status to match")
    }

    @Test
    @PactTestFor(pactMethod = "createEmployeeExists")
    @Order(2)
    fun testCreateEmployeeWithExists(mockServer: MockServer) {
        var request: HttpEntity<EmployeeRequestDto> = HttpEntity<EmployeeRequestDto>(EmployeeRequestDto(employeeDto),
                headers)
        val response: ResponseEntity<EmployeeResponseDto> = testRestTemplate
                .exchange("${mockServer.getUrl()}/demo/employee",
                        POST, request, EmployeeResponseDto::class.java)
        assertEquals(response.statusCode, INTERNAL_SERVER_ERROR, "Expected the status to match")
    }

    @Test
    @PactTestFor(pactMethod = "updateEmployee")
    @Order(3)
    fun testUpdateEmployee(mockServer: MockServer) {
        var request: HttpEntity<EmployeeRequestDto> = HttpEntity<EmployeeRequestDto>(EmployeeRequestDto(employeeDto),
                headers)
        val response: ResponseEntity<EmployeeResponseDto> = testRestTemplate
                .exchange("${mockServer.getUrl()}/demo/employee",
                        PUT, request, EmployeeResponseDto::class.java)
        assertEquals(response.statusCode, OK, "Expected the status to match")
    }

    @Test
    @PactTestFor(pactMethod = "getEmployeeByName")
    @Order(4)
    fun testGetEmployeeByName(mockServer: MockServer) {
        val response: ResponseEntity<EmployeeResponseDto> = testRestTemplate
                .exchange("${mockServer.getUrl()}/demo/employee/firstName/Funny FirstName/lastName/Funny LastName",
                        GET, null, EmployeeResponseDto::class.java)
        assertEquals(response.statusCode, OK, "Expected the status to match")
    }

    @Test
    @PactTestFor(pactMethod = "getEmployeeById")
    @Order(5)
    fun testGetEmployeeById(mockServer: MockServer) {
        val response: ResponseEntity<EmployeeResponseDto> = testRestTemplate
                .exchange("${mockServer.getUrl()}/demo/employee/1",
                        GET, null, EmployeeResponseDto::class.java)
        assertEquals(response.statusCode, OK, "Expected the status to match")
    }

    @Test
    @PactTestFor(pactMethod = "getEmployeeBySalary")
    @Order(6)
    fun testGetEmployeeBySalary(mockServer: MockServer) {
        val response: ResponseEntity<EmployeeResponseDto> = testRestTemplate
                .exchange("${mockServer.getUrl()}/demo/employee/salary/low/2000/high/6000",
                        GET, null, EmployeeResponseDto::class.java)
        assertEquals(response.statusCode, OK, "Expected the status to match")
    }


    @Test
    @PactTestFor(pactMethod = "deleteEmployeeByName")
    @Order(7)
    fun testDeleteEmployeeByName(mockServer: MockServer) {
        val response: ResponseEntity<EmployeeResponseDto> = testRestTemplate
                .exchange("${mockServer.getUrl()}/demo/employee/firstName/Funny FirstName/lastName/Funny LastName",
                        DELETE, null, EmployeeResponseDto::class.java)
        assertEquals(response.statusCode, OK, "Expected the status to match")
    }

    @Test
    @PactTestFor(pactMethod = "deleteEmployeeById")
    @Order(8)
    fun testDeleteEmployeeById(mockServer: MockServer) {
        val response: ResponseEntity<EmployeeResponseDto> = testRestTemplate
                .exchange("${mockServer.getUrl()}/demo/employee/1",
                        DELETE, null, EmployeeResponseDto::class.java)
        assertEquals(response.statusCode, OK, "Expected the status to match")
    }

    fun loadCustomConverters() {
        var messageConverter = MappingJackson2HttpMessageConverter()
        messageConverter.objectMapper = mapper
        testRestTemplate.restTemplate.messageConverters.add(messageConverter)
    }

    @Throws(KeyStoreException::class, NoSuchAlgorithmException::class, KeyManagementException::class)
    fun restTemplateWithSSL(): RestTemplate {
        val httpClient = HttpClients.custom()
                .setSSLSocketFactory(SSLConnectionSocketFactory(SSLContexts.custom()
                        .loadTrustMaterial(null, TrustSelfSignedStrategy())
                        .build(), NoopHostnameVerifier.INSTANCE))
                .build()
        val requestFactory = HttpComponentsClientHttpRequestFactory()
        requestFactory.httpClient = httpClient
        return RestTemplate(requestFactory)
    }

    fun poolConnectionMangerWithSSL() {
        try {
            val poolConnManager = PoolingHttpClientConnectionManager(RegistryBuilder
                    .create<ConnectionSocketFactory>()
                    .register("http", PlainConnectionSocketFactory.getSocketFactory())
                    .register("https", SSLConnectionSocketFactory(SSLContextBuilder()
                    .loadTrustMaterial(null, TrustSelfSignedStrategy())
                            .build()))
                    .build())
            poolConnManager.maxTotal = maxTotal
            poolConnManager.defaultMaxPerRoute= defaultMaxPerRoute
            val requestConfig = RequestConfig.custom().setConnectionRequestTimeout(connectionRequestTimeout)
                    .setSocketTimeout(socketTimeout).setConnectTimeout(connectTimeout).build()
            val httpClient = getConnection(poolConnManager, requestConfig)
        } catch (e: Exception) {

        }
    }

    private fun getConnection(poolConnManager: PoolingHttpClientConnectionManager, requestConfig: RequestConfig): CloseableHttpClient? {
        val httpClient: CloseableHttpClient = HttpClients.custom()
                .setConnectionManager(poolConnManager)
                .setDefaultRequestConfig(requestConfig)
                .setRetryHandler(DefaultHttpRequestRetryHandler(0, false)).build()
        if (poolConnManager.totalStats != null) {
            log.info("now client pool " + poolConnManager.totalStats.toString())
        }
        return httpClient
    }
}