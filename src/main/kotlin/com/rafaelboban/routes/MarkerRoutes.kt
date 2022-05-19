package com.rafaelboban.routes

import com.rafaelboban.data.marker.Marker
import com.rafaelboban.data.marker.MarkerDataSource
import com.rafaelboban.data.requests.DeleteMarkerRequest
import com.rafaelboban.data.requests.MarkerRequest
import com.rafaelboban.data.responses.DeleteMarkerResponse
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.createMarker() {

    authenticate {
        post("/create-marker") {
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

            val wasAcknowledged = MarkerDataSource.insertMarker(marker)
            if (!wasAcknowledged) {
                call.respond(HttpStatusCode.InternalServerError)
                return@post
            }

            call.respond(HttpStatusCode.OK, marker)
        }
    }
}

fun Route.deleteMarker() {

    authenticate {
        post("/delete-marker") {
            val request = call.receiveOrNull<DeleteMarkerRequest>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            val wasAcknowledged = MarkerDataSource.removeMarker(request.id)
            if (!wasAcknowledged) {
                call.respond(HttpStatusCode.Conflict)
                return@post
            }

            call.respond(HttpStatusCode.OK, DeleteMarkerResponse(request.id))
        }
    }
}

fun Route.getMarkers() {

    authenticate {
        get("/markers") {
            val userId = call.principal<JWTPrincipal>()?.getClaim("userId", String::class) ?: run {
                call.respond(HttpStatusCode.Unauthorized)
                return@get
            }

            val markers = MarkerDataSource.getMarkersByUserId(userId)
            call.respond(HttpStatusCode.OK, markers)
        }
    }
}