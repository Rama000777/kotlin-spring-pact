package com.sample.local.repository

import com.sample.local.entity.Address
import com.sample.local.entity.Contact
import com.sample.local.entity.Employee
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query

interface  IContactRepository : MongoRepository<Contact, Long> {
    @Query("{ 'value' : ?0}")
    fun findByContactValue(value: String): List<Contact>
    @Query("{ value: { '\$in': [?0 ] } }")
    fun findByContactValues(values: Set<String>): List<Contact>
    @Query("{ _id: { '\$in': [?0 ] } }")
    fun findByContactIds(ids: Set<Long>): List<Contact>
}
