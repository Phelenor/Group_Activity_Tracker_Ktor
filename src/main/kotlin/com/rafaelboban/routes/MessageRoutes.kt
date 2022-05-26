package com.rafaelboban.routes

import com.rafaelboban.data.message.MessageDataSource
import com.rafaelboban.data.requests.MessagesRequest
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.getMessages(messageDataSource: MessageDataSource) {

    authenticate {
        post("/api/messages") {
            val request = call.receiveOrNull<MessagesRequest>() ?: kotlin.run {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            val messages = messageDataSource.getMessagesForEvent(request.eventId)
            call.respond(HttpStatusCode.OK, messages)
        }
    }
}
