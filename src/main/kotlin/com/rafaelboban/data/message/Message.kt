package com.rafaelboban.data.message

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Message(
    val userId: String,
    val timestamp: Long,
    val message: String,
    @BsonId val id: String = ObjectId().toString()
)