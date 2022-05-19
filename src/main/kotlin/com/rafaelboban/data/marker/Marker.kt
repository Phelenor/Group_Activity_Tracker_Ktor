package com.rafaelboban.data.marker

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Marker(
    val title: String,
    val snippet: String?,
    val latitude: Double,
    val longitude: Double,
    val userId: String,
    @BsonId val id: String = ObjectId().toString()
)
