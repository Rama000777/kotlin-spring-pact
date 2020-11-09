package com.sample.local.controller

import com.sample.local.dto.EmployeeRequestDto
import com.sample.local.dto.EmployeeResponseDto
import com.sample.local.exception.ApplicationException
import com.sample.local.service.EmployeeService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/demo/employee")
class EmployeeController(val employeeService: EmployeeService) {

    @PostMapping
    fun createEmployee(@RequestBody employee: EmployeeRequestDto):
            ResponseEntity<EmployeeResponseDto> {
        return ResponseEntity<EmployeeResponseDto>(employeeService.addEmployee(employee),
                HttpStatus.CREATED)
    }

    @PutMapping
    fun updateEmployee(@RequestBody employee: EmployeeRequestDto):
            ResponseEntity<EmployeeResponseDto> {
        return ResponseEntity<EmployeeResponseDto>(employeeService.updateEmployee(employee),
                HttpStatus.OK)
    }

    @DeleteMapping(path = ["/{employeeId}"])
    fun deleteEmployeeById(@PathVariable(value = "employeeId") employeeId: Long):
            ResponseEntity<EmployeeResponseDto> {
        return ResponseEntity<EmployeeResponseDto>(employeeService.deleteEmployee(employeeId),
                HttpStatus.OK)
    }

    @DeleteMapping(path = ["/firstName/{firstName}/lastName/{lastName}"])
    fun deleteEmployeeByName(@PathVariable(value = "firstName") firstName: String,
                             @PathVariable(value = "lastName") lastName: String):
            ResponseEntity<EmployeeResponseDto> {
        return ResponseEntity<EmployeeResponseDto>(employeeService.deleteEmployeeByName(firstName, lastName),
                HttpStatus.OK)
    }

    @GetMapping(path = ["/firstName/{firstName}/lastName/{lastName}"])
    fun findEmployeeByName(@PathVariable(value = "firstName") firstName: String,
                             @PathVariable(value = "lastName") lastName: String):
            ResponseEntity<EmployeeResponseDto> {
        return ResponseEntity<EmployeeResponseDto>(employeeService.findEmployeeByName(firstName, lastName),
                HttpStatus.OK)
    }

    @GetMapping(path = ["/{employeeId}"])
    @Throws(ApplicationException::class)
    fun getEmployeeById(@PathVariable(value = "employeeId") employeeId: Long): ResponseEntity<EmployeeResponseDto> {
        return ResponseEntity<EmployeeResponseDto>(employeeService.getEmployee(employeeId),
                HttpStatus.OK)
    }

    @GetMapping(path = ["salary/low/{low}/high/{high}"])
    @Throws(ApplicationException::class)
    fun getEmployeeBySalaryRange(@PathVariable(value = "low") low: Double,
                                  @PathVariable(value = "high") high:Double): ResponseEntity<EmployeeResponseDto> {
        return ResponseEntity<EmployeeResponseDto>(employeeService.getEmployeeBySalary(low,high),
                HttpStatus.OK)
    }

    @GetMapping
    fun getEmployees(): ResponseEntity<EmployeeResponseDto> {
        return ResponseEntity<EmployeeResponseDto>(employeeService.getEmployees(),
                HttpStatus.OK)
    }
}