package com.sample.local.pact

import au.com.dius.pact.core.model.Interaction
import au.com.dius.pact.core.model.Pact
import au.com.dius.pact.provider.junit.Provider
import au.com.dius.pact.provider.junit.State
import au.com.dius.pact.provider.junit.loader.PactBroker
import au.com.dius.pact.provider.junit.loader.PactBrokerAuth
import au.com.dius.pact.provider.junit.loader.PactFolder
import au.com.dius.pact.provider.junit5.HttpTestTarget
import au.com.dius.pact.provider.junit5.PactVerificationContext
import au.com.dius.pact.provider.junit5.PactVerificationInvocationContextProvider
import mu.KLogging
import org.apache.http.HttpRequest
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.*
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(PactVerificationInvocationContextProvider::class)
@Provider("kotlinProviderDemo")
//@PactBroker(host = "localhost", port = "9292",
       // authentication = PactBrokerAuth(username = "", password = ""))
@Disabled
class PactProviderBrokerTest {

    @BeforeEach
    fun before(context: PactVerificationContext) {
        context.target = HttpTestTarget("localhost", 8080, "/")
    }

    @TestTemplate
    @ExtendWith(PactVerificationInvocationContextProvider::class)
    internal fun testTemplate(pact: Pact<*>, interaction: Interaction,
                              request: HttpRequest, context: PactVerificationContext) {
        logger.info("""Template: ${pact.provider.name}, ${interaction.description}, 
            |${interaction.interactionId}""".trimMargin())
        context.verifyInteraction()
    }

    @State("State 1: Create Employee")
    fun testCreateEmployee() {
        executedStates.add("State 1: Create Employee")
    }

    @State("State 2: Create Existing Employee")
    fun testCreateEmployeeExists() {
        executedStates.add("State 2: Create Existing Employee")
    }

    @State("State 3: Update Employee")
    fun testUpdateEmployee() {
        executedStates.add("State 3: Update Employee")
    }

    @State("State 4: Get Employee By Name")
    fun testGetEmployeeByName() {
        executedStates.add("State 4: Get Employee By Name")
    }

    @State("State 5: Get Employee By Id")
    fun testGetEmployeeById() {
        executedStates.add("State 5: Get Employee By Id")
    }

    @State("State 6: Get Employee By Salary Range")
    fun testEmployeeBySalaryRange() {
        executedStates.add("State 6: Get Employee By Salary Range")
    }

    @State("State 7: Delete Employee By Name")
    fun testDeleteEmployeeByName() {
        executedStates.add("State 7: Delete Employee By Name")
    }

    @State("State 8: Delete Employee By Id")
    fun testDeleteEmployeeById() {
        executedStates.add("State 8: Delete Employee By Id")
    }

    companion object : KLogging() {
        val executedStates = mutableListOf<String>()

        @BeforeAll
        @JvmStatic
        fun beforeTest() {
            executedStates.clear()
        }

        @AfterAll
        @JvmStatic
        fun afterTest() {
            assertThat(executedStates, equalTo(listOf("State 1: Create Employee",
                    "State 2: Create Existing Employee",
                    "State 3: Update Employee",
                    "State 4: Get Employee By Name",
                    "State 5: Get Employee By Id",
                    "State 6: Get Employee By Salary Range",
                    "State 7: Delete Employee By Name",
                    "State 8: Delete Employee By Id")))
        }
    }
}