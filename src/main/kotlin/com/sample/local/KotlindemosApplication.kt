package com.sample.local

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement

@SpringBootApplication
@EnableMongoRepositories
@EnableTransactionManagement
class KotlindemosApplication
fun main(args: Array<String>) {
	runApplication<KotlindemosApplication>(*args)
}
