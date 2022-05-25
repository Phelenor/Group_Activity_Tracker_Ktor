package com.rafaelboban.routes

import com.rafaelboban.data.marker.Marker
import com.rafaelboban.data.marker.MarkerDataSource
import com.rafaelboban.data.requests.DeleteMarkerRequest
import com.rafaelboban.data.requests.MarkerRequest
import com.rafaelboban.data.responses.DeleteMarkerResponse
import com.rafaelboban.plugins.TrackingSession
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

fun Route.createMarker(markerDataSource: MarkerDataSource) {

    authenticate {
        post("/api/create-marker") {
            val request = call.receiveOrNull<MarkerRequest>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            val userId = call.principal<JWTPrincipal>()?.getClaim("userId", String::class) ?: run {
                call.respond(HttpStatusCode.Unauthorized)
                return@post
            }

            val marker = if (request.id != null) {
                Marker(request.title, request.snippet, request.latitude, request.longitude, userId, request.id)
            } else {
                Marker(request.title, request.snippet, request.latitude, request.longitude, userId)
            }

            val wasAcknowledged = markerDataSource.insertMarker(marker)
            if (!wasAcknowledged) {
                call.respond(HttpStatusCode.InternalServerError)
                return@post
            }

            call.respond(HttpStatusCode.OK, marker)
        }
    }
}

fun Route.deleteMarker(markerDataSource: MarkerDataSource) {

    authenticate {
        post("/api/delete-marker") {
            val request = call.receiveOrNull<DeleteMarkerRequest>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            val wasAcknowledged = markerDataSource.removeMarker(request.id)
            if (!wasAcknowledged) {
                call.respond(HttpStatusCode.Conflict)
                return@post
            }

            call.respond(HttpStatusCode.OK, DeleteMarkerResponse(request.id))
        }
    }
}

fun Route.getMarkers(markerDataSource: MarkerDataSource) {

    authenticate {
        get("/api/markers") {
            val userId = call.principal<JWTPrincipal>()?.getClaim("userId", String::class) ?: run {
                call.respond(HttpStatusCode.Unauthorized)
                return@get
            }

            val markers = markerDataSource.getMarkersByUserId(userId)
            call.respond(HttpStatusCode.OK, markers)
        }
    }
}