package com.rafaelboban.plugins

import com.rafaelboban.routes.*
import com.rafaelboban.security.token.TokenConfig
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*

fun Application.configureRouting(tokenConfig: TokenConfig) {

    routing {
        register()
        login(tokenConfig)
        createMarker()
        deleteMarker()
        getMarkers()
        authenticate()
    }
}
