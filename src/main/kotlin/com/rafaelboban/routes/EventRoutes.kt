package com.rafaelboban.routes

import com.rafaelboban.EventServer
import com.rafaelboban.data.event.Event
import com.rafaelboban.data.requests.CreateEventRequest
import com.rafaelboban.data.requests.JoinEventRequest
import com.rafaelboban.data.responses.CreateJoinEventResponse
import com.rafaelboban.plugins.TrackingSession
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*

fun Route.createEvent() {

    authenticate {
        post("/api/create-event") {
            val request = call.receiveOrNull<CreateEventRequest>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            val userId = call.principal<JWTPrincipal>()?.getClaim("userId", String::class) ?: run {
                call.respond(HttpStatusCode.Unauthorized)
                return@post
            }

            val event = Event(request.name, userId)
            EventServer.events[event.id] = event

            val response = CreateJoinEventResponse(event.id, event.joinCode)
            call.respond(HttpStatusCode.OK, response)
        }
    }
}

fun Route.joinEvent() {

    authenticate {
        post("/api/join-event") {
            val request = call.receiveOrNull<JoinEventRequest>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            val event = EventServer.events.values.find { it.joinCode == request.joinCode } ?: run {
                call.respond(HttpStatusCode.NotFound)
                return@post
            }

            val response = CreateJoinEventResponse(event.id, request.joinCode)
            call.respond(HttpStatusCode.OK, response)
        }
    }
}