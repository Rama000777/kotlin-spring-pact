package com.sample.local.repository

import com.sample.local.entity.Employee
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query

interface  IEmployeeRepository : MongoRepository<Employee, Long> {
    @Query("{ 'firstName' : ?0, 'lastName' : ?1}")
    fun findByName(firstName: String, lastName: String): List<Employee>

    @Query("{ address: { '\$in': [?0 ] } }")
    fun findByAddressIds(ids: Set<Long>): List<Employee>

    @Query("{ contacts: { '\$in': [?0 ] } }")
    fun findByContactIds(ids: Set<Long>): List<Employee>

    @Query("{ salary : { '\$gt': ?0, '\$lt': ?1 } }")
    fun findBySalary(low: Double, high:Double): List<Employee>
}
