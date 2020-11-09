package com.sample.local.service

import com.sample.local.entity.Address
import com.sample.local.entity.Contact
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Criteria.where
import org.springframework.data.mongodb.core.query.Query
import org.springframework.stereotype.Repository
import java.util.*
import java.util.stream.Collectors


@Repository
class DatabaseService {
    @Autowired
    lateinit var mongoTemplate: MongoTemplate

    fun getAddressIds(no: String, street:String, city:String): Set<Long> {
        val query = Query(Criteria().orOperator(where("no").`is`(no), where("street").`is`(street), where("city").`is`(city)))
        query.fields().include("_id")
        val addressValues: List<Address> = mongoTemplate.find(query, Address::class.java, "address")
        return addressValues.stream().map<Long>(Address::addressId).collect(Collectors.toSet()) as HashSet<Long>
    }

    fun getContactIds(value: String): Set<Long> {
        val query = Query(Criteria().orOperator(where("value").`is`(value)))
        query.fields().include("_id")
        val contactValues: List<Contact> = mongoTemplate.find(query, Contact::class.java, "contact")
        return contactValues.stream().map<Long>(Contact::contactId).collect(Collectors.toSet()) as HashSet<Long>
    }

    fun deleteByContactIds(values: Set<Long>){
        mongoTemplate.remove(Query(Criteria.where("id").`in`(values)), Contact::class.java)
    }

    fun deleteByAddressIds(values: Set<Long>){
        mongoTemplate.remove(Query(Criteria.where("id").`in`(values)), Address::class.java)
    }

}