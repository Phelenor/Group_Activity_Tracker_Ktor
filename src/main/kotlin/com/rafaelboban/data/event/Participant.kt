package com.rafaelboban.data.event

import io.ktor.websocket.*

data class Participant(
    val id: String,
    val username: String,
    var socket: WebSocketSession,
    var distance: Double = 0.0,
    var isActive: Boolean = true
)
