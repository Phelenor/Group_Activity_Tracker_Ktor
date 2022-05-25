package com.rafaelboban.plugins

import com.rafaelboban.utils.Constants
import io.ktor.server.application.*
import io.ktor.server.sessions.*

fun Application.configureSessions() {
    install(Sessions) {
        cookie<TrackingSession>(Constants.SESSION_NAME)
    }
}

data class TrackingSession(
    val userId: String,
    val sessionId: String
)