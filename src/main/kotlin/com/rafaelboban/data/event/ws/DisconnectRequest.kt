package com.rafaelboban.data.event.ws

import com.rafaelboban.utils.Constants.TYPE_DISCONNECT_REQUEST

class DisconnectRequest(
    val eventId: String
) : BaseModel(TYPE_DISCONNECT_REQUEST)