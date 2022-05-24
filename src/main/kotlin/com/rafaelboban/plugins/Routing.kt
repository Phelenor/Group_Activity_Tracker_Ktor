package com.rafaelboban.plugins

import com.rafaelboban.routes.*
import com.rafaelboban.security.token.TokenConfig
import io.ktor.server.routing.*
import io.ktor.server.application.*

fun Application.configureRouting(tokenConfig: TokenConfig) {

    routing {
        register()
        login(tokenConfig)
        createMarker()
        deleteMarker()
        getMarkers()
        createEvent()
        joinEvent()
        eventWebSocket()
        authenticate()
    }
}
