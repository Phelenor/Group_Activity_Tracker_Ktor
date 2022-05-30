package com.rafaelboban.routes

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.rafaelboban.EventServer
import com.rafaelboban.data.event.*
import com.rafaelboban.data.event.ws.*
import com.rafaelboban.data.location.LocationDataSource
import com.rafaelboban.data.location.LocationPointData
import com.rafaelboban.plugins.TrackingSession
import com.rafaelboban.utils.Constants.TYPE_ANNOUNCEMENT
import com.rafaelboban.utils.Constants.TYPE_CHAT_MESSAGE
import com.rafaelboban.utils.Constants.TYPE_DISCONNECT_REQUEST
import com.rafaelboban.utils.Constants.TYPE_FINISH_EVENT
import com.rafaelboban.utils.Constants.TYPE_JOIN_HANDSHAKE
import com.rafaelboban.utils.Constants.TYPE_LOCATION_DATA
import com.rafaelboban.utils.Constants.TYPE_PHASE_CHANGE
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.channels.consumeEach
import org.koin.ktor.ext.inject

fun Route.eventWebSocket(locationDataSource: LocationDataSource, eventDataSource: EventDataSource, gson: Gson) {
    standardWebSocket("/ws/event") { socket, userId, message, payload ->
        when (payload) {
            is JoinEventHandshake -> {
                val event = EventServer.events[payload.eventId] ?: return@standardWebSocket
                event.lastUpdate = System.currentTimeMillis()
                if (event.containsParticipant(payload.userId)) {
                    val reconnectedParticipant = event.participants.find { it.id == payload.userId }!!.copy(socket = socket)
                    event.reconnectParticipant(reconnectedParticipant)
                } else {
                    event.addParticipant(payload.userId, payload.username, socket)
                }
            }
            is LocationData -> {
                val event = EventServer.events[payload.eventId] ?: return@standardWebSocket
                event.lastUpdate = System.currentTimeMillis()
                if (event.phase == EventController.Phase.IN_PROGRESS) {
                    event.broadcastToAllExcept(message, userId)
                }
                locationDataSource.insertLocation(
                    LocationPointData(
                        userId,
                        payload.eventId,
                        payload.timestamp,
                        payload.latitude,
                        payload.longitude
                    )
                )
            }
            is ChatMessage -> {
                val event = EventServer.events[payload.eventId] ?: return@standardWebSocket
                event.lastUpdate = System.currentTimeMillis()
                event.broadcast(message)
            }
            is Announcement -> {
                val event = EventServer.events[payload.eventId] ?: return@standardWebSocket
                event.lastUpdate = System.currentTimeMillis()
                event.broadcast(message)
            }
            is PhaseChange -> {
                val event = EventServer.events[payload.eventId] ?: return@standardWebSocket
                event.lastUpdate = System.currentTimeMillis()
                if (userId == event.ownerId) {
                    event.phase = payload.phase
                    event.broadcastToAllExcept(message, userId)
                }
            }
            is DisconnectRequest -> {
                val event = EventServer.events[payload.eventId] ?: return@standardWebSocket
                println("$userId disconnected from ${event.name}.")
                event.lastUpdate = System.currentTimeMillis()
                event.removeParticipant(userId, payload.username)
            }
            is FinishEvent -> {
                val event = EventServer.events[payload.eventId] ?: return@standardWebSocket
                println("$userId finished ${event.name}.")
                event.lastUpdate = System.currentTimeMillis()
                val announcement = Announcement(
                    event.id,
                    "${payload.username} has finished his activity.",
                    System.currentTimeMillis(),
                    Announcement.TYPE_PLAYER_FINISHED
                )

                event.broadcast(gson.toJson(announcement))

                if (event.startTimestamp != 0L) {
                    val endTimestamp = System.currentTimeMillis()
                    val durationMinutes = (endTimestamp - event.startTimestamp) / 1000.0 / 60
                    if (durationMinutes < 3) return@standardWebSocket

                    val isSaved = eventDataSource.insertSubEvent(
                        SubEvent(
                            event.id,
                            payload.userId,
                            event.startTimestamp,
                            endTimestamp,
                            payload.distance
                        )
                    )
                    println("SubEvent($userId) saved: $isSaved")
                }
            }
        }
    }
}

fun Route.standardWebSocket(
    route: String,
    handleFrame: suspend (
        socket: DefaultWebSocketServerSession,
        participantId: String,
        message: String,
        payload: BaseModel
    ) -> Unit
) {
    webSocket(route) {
        val gson: Gson by inject()
        val session = call.sessions.get<TrackingSession>()
        if (session == null) {
            close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No session."))
            return@webSocket
        }

        try {
            incoming.consumeEach { frame ->
                if (frame is Frame.Text) {
                    val message = frame.readText()
                    val jsonObject = JsonParser.parseString(message).asJsonObject
                    val type = when (jsonObject.get("type").asString) {
                        TYPE_JOIN_HANDSHAKE -> JoinEventHandshake::class.java
                        TYPE_CHAT_MESSAGE -> ChatMessage::class.java
                        TYPE_LOCATION_DATA -> LocationData::class.java
                        TYPE_ANNOUNCEMENT -> Announcement::class.java
                        TYPE_PHASE_CHANGE -> PhaseChange::class.java
                        TYPE_DISCONNECT_REQUEST -> DisconnectRequest::class.java
                        TYPE_FINISH_EVENT -> FinishEvent::class.java
                        else -> BaseModel::class.java
                    }
                    val payload = gson.fromJson(message, type)
                    handleFrame(this, session.userId, message, payload)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}