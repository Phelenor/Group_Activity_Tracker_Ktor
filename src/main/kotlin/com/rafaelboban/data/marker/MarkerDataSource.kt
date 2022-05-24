package com.rafaelboban.data.marker

import org.koin.java.KoinJavaComponent
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

object MarkerDataSource {

    private val database: CoroutineDatabase by KoinJavaComponent.inject(CoroutineDatabase::class.java)
    private val markers = database.getCollection<Marker>()

    suspend fun getMarkersByUserId(userId: String): List<Marker> {
        return markers.find(Marker::userId eq userId).toList()
    }

    suspend fun insertMarker(marker: Marker): Boolean {
        val markerExists = markers.findOneById(marker.id) != null

        return if (markerExists) {
            markers.updateOneById(marker.id, marker).wasAcknowledged()
        } else {
            markers.insertOne(marker).wasAcknowledged()
        }
    }

    suspend fun removeMarker(markerId: String): Boolean {
        return markers.deleteOneById(markerId).wasAcknowledged()
    }
}