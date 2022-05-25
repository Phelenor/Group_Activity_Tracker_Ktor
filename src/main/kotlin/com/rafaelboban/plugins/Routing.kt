package com.rafaelboban.plugins

import com.rafaelboban.data.location.LocationDataSource
import com.rafaelboban.data.marker.MarkerDataSource
import com.rafaelboban.data.message.MessageDataSource
import com.rafaelboban.data.user.UserDataSource
import com.rafaelboban.routes.*
import com.rafaelboban.security.token.TokenConfig
import io.ktor.server.routing.*
import io.ktor.server.application.*
import org.koin.java.KoinJavaComponent.inject
import org.koin.ktor.ext.inject

fun Application.configureRouting(tokenConfig: TokenConfig) {
    val userDataSource by inject<UserDataSource>()
    val markerDataSource by inject<MarkerDataSource>()
    val locationDataSource by inject<LocationDataSource>()
    val messageDataSource by inject<MessageDataSource>()

    routing {
        register(userDataSource)
        login(userDataSource, tokenConfig)
        createMarker(markerDataSource)
        deleteMarker(markerDataSource)
        getMarkers(markerDataSource)
        createEvent()
        joinEvent()
        eventWebSocket(locationDataSource, messageDataSource)
        authenticate()
    }
}
