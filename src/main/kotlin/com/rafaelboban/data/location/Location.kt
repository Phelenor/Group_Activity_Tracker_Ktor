package com.rafaelboban.data.location

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Location(
    val userId: String,
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    @BsonId val id: String = ObjectId().toString()
)