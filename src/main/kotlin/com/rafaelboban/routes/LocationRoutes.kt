package com.rafaelboban.routes

import com.rafaelboban.data.location.Location
import com.rafaelboban.data.location.LocationDataSource
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.saveLocation() {

    authenticate {
        post("/location") {
//            val request = call.receiveOrNull<LocationRequest>() ?: kotlin.run {
//                call.respond(HttpStatusCode.BadRequest)
//                return@post
//            }
//
//            val userId = call.principal<JWTPrincipal>()?.getClaim("userId", String::class) ?: run {
//                call.respond(HttpStatusCode.Unauthorized)
//                return@post
//            }
//
//            val location = Location(userId, request.timestamp, request.latitude, request.longitude)
//            val wasAcknowledged = LocationDataSource.insertLocation(location)
//            if (!wasAcknowledged) {
//                call.respond(HttpStatusCode.InternalServerError)
//                return@post
//            }
//            call.respond(HttpStatusCode.OK)
        }
    }
}