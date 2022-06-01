package com.rafaelboban.data.location

import org.litote.kmongo.coroutine.CoroutineDatabase

class LocationDataSource(db: CoroutineDatabase) {

    private val locations = db.getCollection<LocationPoint>()

    suspend fun insertLocation(location: LocationPoint): Boolean {
        return locations.insertOne(location).wasAcknowledged()
    }
}