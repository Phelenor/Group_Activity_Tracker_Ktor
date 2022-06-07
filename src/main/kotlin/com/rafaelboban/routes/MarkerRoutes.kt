package com.rafaelboban.routes

import com.rafaelboban.data.marker.Marker
import com.rafaelboban.data.marker.MarkerDataSource
import com.rafaelboban.data.requests.MarkerRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.createMarker(markerDataSource: MarkerDataSource) {

    authenticate {
        post("/api/save-marker") {
            val request = call.receiveOrNull<MarkerRequest>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            val userId = call.principal<JWTPrincipal>()?.getClaim("userId", String::class) ?: run {
                call.respond(HttpStatusCode.Unauthorized)
                return@post
            }

            val marker = Marker(request.title, request.snippet, request.latitude, request.longitude, userId, request.eventId)

            val wasAcknowledged = markerDataSource.insertMarker(marker)
            if (!wasAcknowledged) {
                call.respond(HttpStatusCode.InternalServerError)
                return@post
            }

            call.respond(HttpStatusCode.OK, marker)
        }
    }
}

fun Route.getMarkers(markerDataSource: MarkerDataSource) {

    authenticate {
        get("/api/markers/{eventId}") {
            val userId = call.principal<JWTPrincipal>()?.getClaim("userId", String::class) ?: run {
                call.respond(HttpStatusCode.Unauthorized)
                return@get
            }

            val eventId = call.parameters["eventId"] ?: run {
                call.respond(HttpStatusCode.NotFound)
                return@get
            }

            val markers = markerDataSource.getMarkersByUserAndEvent(userId, eventId)
            call.respond(HttpStatusCode.OK, markers)
        }
    }
}
