package com.rafaelboban.data.location

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class LocationPointData(
    val userId: String,
    val eventId: String,
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    @BsonId val id: String = ObjectId().toString()
)
