package com.rafaelboban.data.event.ws

import com.rafaelboban.utils.Constants.TYPE_MARKER_MESSAGE

data class MarkerMessage(
    val eventId: String,
    val latitude: Double,
    val longitude: Double,
    val title: String,
    val snippet: String,
) : BaseModel(TYPE_MARKER_MESSAGE)
