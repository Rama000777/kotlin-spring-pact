package com.sample.local.service

import com.mongodb.BasicDBObject
import com.mongodb.client.FindIterable
import com.mongodb.client.MongoCollection
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Projections.include
import com.mongodb.client.model.UpdateOptions
import org.bson.Document
import org.bson.conversions.Bson
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component

@Component
class DatabaseUtils {
    @Autowired
    lateinit var mongoTemplate: MongoTemplate

    fun getSequenceId(name: String): Any? {
        val sequence: Bson = eq("name", name)
        val updateSequence: Bson = Document("\$inc", BasicDBObject("value", 1L))
        getCollection("Sequences").updateOne(sequence, updateSequence, UpdateOptions().upsert(true))
        return getCollection("Sequences").find(sequence).first()!!.get("value")
    }

    fun getCollection(collectionName: String): MongoCollection<Document> {
        return mongoTemplate.getCollection(collectionName)
    }

}