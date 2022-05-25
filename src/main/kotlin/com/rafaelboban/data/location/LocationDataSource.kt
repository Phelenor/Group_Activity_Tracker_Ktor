package com.rafaelboban.data.location

import org.litote.kmongo.coroutine.CoroutineDatabase

class LocationDataSource(db: CoroutineDatabase) {

    private val locations = db.getCollection<Location>()

    suspend fun insertLocation(location: Location): Boolean {
        return locations.insertOne(location).wasAcknowledged()
    }
}