package com.rafaelboban.data.location

import org.litote.kmongo.coroutine.CoroutineDatabase

class LocationDataSource(db: CoroutineDatabase) {

    private val locations = db.getCollection<LocationPointData>()

    suspend fun insertLocation(location: LocationPointData): Boolean {
        return locations.insertOne(location).wasAcknowledged()
    }
}