package com.sample.local.repository

import com.sample.local.entity.Address
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.data.mongodb.repository.Query

interface  IAddressRepository : MongoRepository<Address, Long> {
    @Query("{'no' : ?0, 'street' : ?1, 'city' : ?2}")
    fun findByStreetCityNo(no: String, street:String, city:String): List<Address>
    @Query("{ _id: { '\$in': [?0 ] } }")
    fun findByAddressIds(ids: Set<Long>): List<Address>
}
