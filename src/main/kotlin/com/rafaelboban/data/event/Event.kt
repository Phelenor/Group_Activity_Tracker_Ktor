package com.rafaelboban.data.event

import org.bson.codecs.pojo.annotations.BsonId

data class Event(
    @BsonId val id: String,
    val participants: Set<String>,
    val startTimestamp: Long,
    val endTimestamp: Long,
    val ownerId: String
)
