package com.rafaelboban.plugins

import com.google.gson.Gson
import com.rafaelboban.data.event.EventDataSource
import com.rafaelboban.data.location.LocationDataSource
import com.rafaelboban.data.marker.MarkerDataSource
import com.rafaelboban.data.user.UserDataSource
import com.rafaelboban.routes.*
import com.rafaelboban.security.token.TokenConfig
import io.ktor.server.application.*
import io.ktor.server.routing.*
import org.koin.ktor.ext.inject

fun Application.configureRouting(tokenConfig: TokenConfig) {
    val userDataSource by inject<UserDataSource>()
    val markerDataSource by inject<MarkerDataSource>()
    val locationDataSource by inject<LocationDataSource>()
    val eventDataSource by inject<EventDataSource>()
    val gson by inject<Gson>()

    routing {
        register(userDataSource)
        login(userDataSource, tokenConfig)
        createMarker(markerDataSource)
        deleteMarker(markerDataSource)
        getMarkers(markerDataSource)
        createEvent()
        joinEvent()
        checkEventStatus()
        getEvents(eventDataSource)
        eventWebSocket(locationDataSource, eventDataSource, gson)
        authenticate()
    }
}
