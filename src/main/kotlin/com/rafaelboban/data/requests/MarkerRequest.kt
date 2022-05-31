package com.rafaelboban.data.requests

data class MarkerRequest(
    val title: String,
    val snippet: String?,
    val latitude: Double,
    val longitude: Double,
    val eventId: String
)
