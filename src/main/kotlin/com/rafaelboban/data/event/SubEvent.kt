package com.rafaelboban.data.event

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class SubEvent(
    val eventId: String,
    val userId: String,
    val startTimestamp: Long,
    val endTimestamp: Long,
    val distance: Double,
    @BsonId val id: String = ObjectId().toString()
)
