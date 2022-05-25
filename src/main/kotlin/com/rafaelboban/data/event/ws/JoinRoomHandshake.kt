package com.rafaelboban.data.event.ws

import com.rafaelboban.utils.Constants.TYPE_JOIN_HANDSHAKE

data class JoinEventHandshake(
    val userId: String,
    val username: String,
    val eventId: String,
) : BaseModel(TYPE_JOIN_HANDSHAKE)