package com.rafaelboban.data.marker

import com.rafaelboban.data.Database
import org.litote.kmongo.eq

object MarkerDataSource {

    private val markers = Database.database.getCollection<Marker>()

    suspend fun getMarkersByUserId(userId: String): List<Marker> {
        return markers.find(Marker::userId eq userId).toList()
    }

    suspend fun insertMarker(marker: Marker): Boolean {
        val noteExists = markers.findOneById(marker.id) != null

        return if (noteExists) {
            markers.updateOneById(marker.id, marker).wasAcknowledged()
        } else {
            markers.insertOne(marker).wasAcknowledged()
        }
    }

    suspend fun removeMarker(markerId: String): Boolean {
        return markers.deleteOneById(markerId).wasAcknowledged()
    }
}