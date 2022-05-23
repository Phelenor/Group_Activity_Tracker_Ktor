package com.rafaelboban.data.user

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class User(
    val username: String,
    val email: String,
    val password: String,
    @BsonId val id: String = ObjectId().toString()
)
