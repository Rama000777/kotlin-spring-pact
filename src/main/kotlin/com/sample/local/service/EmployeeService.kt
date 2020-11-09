package com.sample.local.service

import com.sample.local.dto.EmployeeRequestDto
import com.sample.local.dto.EmployeeResponseDto
import com.sample.local.entity.*
import com.sample.local.exception.ApplicationException
import com.sample.local.repository.IAddressRepository
import com.sample.local.repository.IContactRepository
import com.sample.local.repository.IEmployeeRepository
import org.apache.commons.collections4.CollectionUtils
import org.apache.commons.lang3.StringUtils
import org.springframework.beans.BeanUtils
import org.springframework.stereotype.Service
import java.util.*
import java.util.stream.Collectors
import java.util.stream.StreamSupport
import kotlin.collections.ArrayList
import kotlin.collections.HashSet


@Service
class EmployeeService(val employeeRepository: IEmployeeRepository,
                      val addressRepository: IAddressRepository,
                      val contactRepository: IContactRepository,
                      var databaseUtils: DatabaseUtils,
                      var databaseService: DatabaseService) {

    @Throws(ApplicationException::class)
    fun addEmployee(requestDto: EmployeeRequestDto): EmployeeResponseDto {
        var employee = addEmployeeRecord(requestDto.employee)
        return EmployeeResponseDto(ResponseDto(listOf(buildEmployeeDto(employee)), "Employee Created Successfully"))
    }

    @Throws(ApplicationException::class)
    fun updateEmployee(requestDto: EmployeeRequestDto): EmployeeResponseDto {
        validateData(requestDto.employee.employeeId, "Employee ID required")
        updateEmployeeRecord(requestDto.employee)
        return EmployeeResponseDto(ResponseDto("Employee Updated Successfully"))
    }

    fun getEmployees(): EmployeeResponseDto {
        val employees = employeeRepository.findAll()
        if (employees.isNotEmpty()) {
            return buildEmployeesResponse(employees)
        }
        return EmployeeResponseDto()
    }

    fun buildEmployeesResponse(employees: List<Employee>): EmployeeResponseDto {
        val employeesResponse = ArrayList<EmployeeDto>()
        for (employee in employees) {
            var employeeDto = EmployeeDto()
            BeanUtils.copyProperties(employee, employeeDto)
            buildEmployeeResponse(employee, employeeDto)
            employeesResponse.add(employeeDto)
        }
        return EmployeeResponseDto(ResponseDto(employeesResponse));
    }

    @Throws(ApplicationException::class)
    fun deleteEmployee(employeeId: Long): EmployeeResponseDto {
        validateData(employeeId, "Employee ID required")
        val employee = employeeRepository.findById(employeeId)
        if (employee.isEmpty) {
            //throw ApplicationException("Employee not found with the employee ID : $employeeId")
        } else {
            databaseService.deleteByAddressIds(employee.get().address)
            databaseService.deleteByContactIds(employee.get().contacts)
            employeeRepository.deleteById(employeeId)
        }
        return EmployeeResponseDto(ResponseDto("Employee Deleted Successfully"))
    }

    @Throws(ApplicationException::class)
    fun deleteEmployeeByName(firstName: String, lastName: String): EmployeeResponseDto {
        val employees = employeeRepository.findByName(firstName, lastName)
        if (employees.isEmpty()) {
            throw ApplicationException("Employee not found with the firstName : $firstName and lastName : $lastName")
        }
        if (employees.isNotEmpty()) {
            for (employee in employees) {
                databaseService.deleteByAddressIds(employee.address)
                databaseService.deleteByContactIds(employee.contacts)
                employeeRepository.deleteById(employee.employeeId)
            }
        }
        return EmployeeResponseDto(ResponseDto("Employee Deleted Successfully"))
    }

    @Throws(ApplicationException::class)
    fun getEmployeeBySalary(low: Double, high: Double): EmployeeResponseDto {
        val employees = employeeRepository.findBySalary(low, high)
        if (employees.isNotEmpty()) {
            return buildEmployeesResponse(employees)
        }
        return EmployeeResponseDto()
    }

    @Throws(ApplicationException::class)
    fun getEmployee(employeeId: Long): EmployeeResponseDto {
        validateData(employeeId, "Employee ID required")
        val employeeOptional: Optional<Employee> = employeeRepository.findById(employeeId)
        if (employeeOptional.isEmpty) {
            throw ApplicationException("Employee not found with the employee ID : $employeeId")
        }
        return EmployeeResponseDto(ResponseDto(listOf(buildEmployeeDto(employeeOptional.get()))))
    }

    fun buildEmployeeDto(employee: Employee): EmployeeDto {
        var employeeDto = EmployeeDto()
        BeanUtils.copyProperties(employee, employeeDto)
        buildEmployeeResponse(employee, employeeDto)
        return employeeDto;
    }

    @Throws(ApplicationException::class)
    fun findEmployeeByName(firstName: String, lastName: String): EmployeeResponseDto {
        val employeesDatabase = employeeRepository.findByName(firstName, lastName)
        if (employeesDatabase.isEmpty()) {
            throw ApplicationException("Employee not found with the firstName : $firstName and lastName : $lastName")
        }
        return buildEmployeesResponse(employeesDatabase)
    }

    fun buildEmployeeResponse(employee: Employee, employeeDto: EmployeeDto) {
        val addressResponse = ArrayList<AddressDto>()
        val addressData = addressRepository.findByAddressIds(employee.address)
        if (addressData.isNotEmpty()) {
            for (address in addressData) {
                var addressDto = AddressDto()
                BeanUtils.copyProperties(address, addressDto)
                addressResponse.add(addressDto)
            }
        }
        val contactsResponse = ArrayList<ContactDto>()
        val contactsData = contactRepository.findByContactIds(employee.contacts)
        if (addressData.isNotEmpty()) {
            for (contact in contactsData) {
                var contactDto = ContactDto()
                BeanUtils.copyProperties(contact, contactDto)
                contactsResponse.add(contactDto)
            }
        }
        employeeDto.contacts = contactsResponse
        employeeDto.address = addressResponse
    }

    fun validateData(value: Long, message: String) {
        if (value == -1L) {
            throw ApplicationException(message)
        }
    }

    fun addEmployeeRecord(employeeDto: EmployeeDto): Employee {
        var employeeDB = employeeRepository.findByName(employeeDto.firstName,
                employeeDto.lastName)

        if (employeeDB.isNotEmpty()) {
            throw ApplicationException("Employee already exists")
        }

        val existingAddressMap = HashMap<Long, Address>()
        val existingContactsMap = HashMap<Long, Contact>()

        validateCollectAddress(employeeDto.address, existingAddressMap, false)
        validateCollectContacts(employeeDto.contacts, existingContactsMap, false)

        val newAddressMap = HashMap<Long, Address>()
        val newContactsMap = HashMap<Long, Contact>()
        createAddressAndContacts(employeeDto, newAddressMap, newContactsMap)
        return employeeRepository.save(Employee(databaseUtils.getSequenceId(EMPLOYEE) as Long,
                employeeDto.firstName, employeeDto.lastName, employeeDto.salary, newAddressMap.keys, newContactsMap.keys))

    }

    fun updateEmployeeRecord(employeeDto: EmployeeDto) {
        var employeeDB = employeeRepository.findByName(employeeDto.firstName,
                employeeDto.lastName)

        if (employeeDB.isNotEmpty() && employeeDB.get(0).employeeId != employeeDto.employeeId) {
            throw ApplicationException("Employees Id are not matching with given firstName and lastName")
        }

        if (employeeDB.isEmpty()) {
            throw ApplicationException("Employee does not exist to update the record")
        }

        val existingAddressMap = HashMap<Long, Address>()
        val existingContactsMap = HashMap<Long, Contact>()

        validateCollectAddress(employeeDto.address, existingAddressMap, true)
        validateCollectContacts(employeeDto.contacts, existingContactsMap, true)

        var employeeData = employeeRepository.findById(employeeDto.employeeId)
        if (employeeData.isPresent) {
            var finalData = employeeData.get()
            if (!StringUtils.equals(finalData.firstName, employeeDto.firstName)) {
                throw ApplicationException("firstName mismatch for the existing record to update data")
            }
            if (!StringUtils.equals(finalData.lastName, employeeDto.lastName)) {
                throw ApplicationException("lastName mismatch for the existing record to update data")
            }
            updateAddressAndContacts(employeeDto, existingAddressMap, existingContactsMap)
            var allAddIds = finalData.address as HashSet<Long>
            allAddIds.addAll(existingAddressMap.keys)

            var allContIds = finalData.address as HashSet<Long>
            allContIds.addAll(existingContactsMap.keys)

            employeeRepository.save(Employee(finalData.employeeId,
                    finalData.firstName, finalData.lastName, employeeDto.salary, allAddIds, allContIds))
        }

    }

    fun getSequenceId(collectionName: String): Long {
        return databaseUtils.getSequenceId(collectionName) as Long
    }

    fun createAddressAndContacts(employeeDto: EmployeeDto, newAddress: HashMap<Long, Address>,
                                 newContacts: HashMap<Long, Contact>) {
        for (addressDto in employeeDto.address) {
            if (findByStreetCityNo(addressDto.no, addressDto.street,
                            addressDto.city).isEmpty()) {
                val address = createAddress(addressDto)
                newAddress.put(address.addressId, address)
            }
        }

        for (contactDto in employeeDto.contacts) {
            if (contactRepository.findByContactValue(contactDto.value).isEmpty()) {
                val contact = createContact(contactDto)
                newContacts.put(contact.contactId, contact)
            }
        }
    }

    fun updateAddressAndContacts(employeeDto: EmployeeDto, existingAddressMap: HashMap<Long, Address>,
                                 existingContactsMap: HashMap<Long, Contact>) {
        for (addressDto in employeeDto.address) {
            val addressData = existingAddressMap.get(addressDto.addressId)
            if (addressData == null) {
                val address = createAddress(addressDto)
                existingAddressMap.put(address.addressId, address)
            } else {
                updateAddress(addressDto)
            }
        }

        for (contactDto in employeeDto.contacts) {
            val contactsData = existingContactsMap.get(contactDto.contactId)
            if (contactsData == null) {
                val contact = createContact(contactDto)
                existingContactsMap.put(contact.contactId, contact)
            } else {
                updateContact(contactDto)
            }
        }
    }

    fun createAddress(addressDto: AddressDto): Address {
        return addressRepository.save(Address(getSequenceId(ADDRESS), addressDto.no, addressDto.street,
                addressDto.city, addressDto.state,
                addressDto.country, addressDto.zipCode))
    }

    fun updateAddress(addressDto: AddressDto): Address {
        return addressRepository.save(Address(addressDto.addressId, addressDto.no, addressDto.street,
                addressDto.city, addressDto.state,
                addressDto.country, addressDto.zipCode))
    }

    fun createContact(contactDto: ContactDto): Contact {
        return contactRepository.save(Contact(getSequenceId(CONTACT), contactDto.contactType, contactDto.value))
    }

    fun updateContact(contactDto: ContactDto): Contact {
        return contactRepository.save(Contact(contactDto.contactId, contactDto.contactType, contactDto.value))
    }

    fun validateCollectAddress(addressDto: List<AddressDto>, existingAddressMap: HashMap<Long, Address>,
                               isUpdate: Boolean) {
        collectAddress(addressDto, existingAddressMap, isUpdate)

        if (existingAddressMap.keys.isNotEmpty()) {
            val employeeDB = employeeRepository.findByAddressIds(existingAddressMap.keys)

            if (!isUpdate && CollectionUtils.isNotEmpty(employeeDB)) {
                throw ApplicationException("Address already linked to another employee")
            }
        }
    }

    fun validateCollectContacts(contactsDto: List<ContactDto>, existingContactsMap: HashMap<Long, Contact>,
                                isUpdate: Boolean) {
        collectContacts(contactsDto, existingContactsMap, isUpdate)

        if (existingContactsMap.keys.isNotEmpty()) {
            val contactsDatabase = employeeRepository.findByContactIds(existingContactsMap.keys)
            if (!isUpdate && CollectionUtils.isNotEmpty(contactsDatabase)) {
                throw ApplicationException("Contacts already linked to another employee")
            }
        }
    }

    fun collectAddress(addresses: List<AddressDto>, existingAddressMap: HashMap<Long, Address>,
                       isUpdate: Boolean) {
        for (address in addresses) {
            val addressDatabase = findByStreetCityNo(address.no,
                    address.street,
                    address.city
            )
            if (CollectionUtils.isNotEmpty(addressDatabase)) {
                if (isUpdate && !addressDatabase.stream().map(Address::addressId)
                                .collect(Collectors.toSet()).contains(address.addressId)) {
                    throw ApplicationException("Address ID not matching with the given address details")
                }
                existingAddressMap.putAll(addressDatabase.associateBy({ it.addressId }, { it }))
            }
        }
    }

    fun findByStreetCityNo(no: String, street: String, city: String): List<Address> {
        return addressRepository.findByStreetCityNo(no, street, city)
    }

    fun collectContacts(contacts: List<ContactDto>, existingContactsMap: HashMap<Long, Contact>,
                        isUpdate: Boolean) {
        for (contact in contacts) {
            val contactsDatabase = contactRepository.findByContactValue(contact.value)
            if (CollectionUtils.isNotEmpty(contactsDatabase)) {
                if (isUpdate && !contactsDatabase.stream().map(Contact::contactId)
                                .collect(Collectors.toSet()).contains(contact.contactId)) {
                    throw ApplicationException("Contact ID not matching with the given contact details")
                }
                existingContactsMap.putAll(contactsDatabase.associateBy({ it.contactId }, { it }))
            }
        }
    }

    private fun <T> toList(iterable: Iterable<T>): List<T> {
        return StreamSupport.stream(iterable.spliterator(), false).collect(Collectors.toList())
    }

    private companion object {
        private const val EMPLOYEE = "employee"
        private const val CONTACT = "contact"
        private const val ADDRESS = "address"
    }
}
