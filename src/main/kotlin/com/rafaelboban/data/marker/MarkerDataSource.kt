package com.rafaelboban.data.marker

import org.koin.java.KoinJavaComponent
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

class MarkerDataSource(db: CoroutineDatabase) {

    private val markers = db.getCollection<Marker>()

    suspend fun insertMarker(marker: Marker): Boolean {
        return markers.insertOne(marker).wasAcknowledged()
    }

    suspend fun getMarkersByUserAndEvent(userId: String, eventId: String): List<Marker> {
        return markers.find(Marker::userId eq userId, Marker::eventId eq eventId).toList()
    }
}