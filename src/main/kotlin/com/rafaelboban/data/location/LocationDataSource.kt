package com.rafaelboban.data.location

import org.koin.java.KoinJavaComponent.inject
import org.litote.kmongo.coroutine.CoroutineDatabase

object LocationDataSource {

    private val database: CoroutineDatabase by inject(CoroutineDatabase::class.java)

    private val locations = database.getCollection<Location>()

    suspend fun insertLocation(location: Location): Boolean {
        return locations.insertOne(location).wasAcknowledged()
    }
}