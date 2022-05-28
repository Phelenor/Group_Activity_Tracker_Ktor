package com.rafaelboban.data.event

import com.google.gson.Gson
import com.rafaelboban.EventServer
import com.rafaelboban.data.event.ws.Announcement
import io.ktor.websocket.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.bson.types.ObjectId
import org.koin.java.KoinJavaComponent.inject

class EventController(val name: String, val ownerId: String) {

    private val gson: Gson by inject(Gson::class.java)
    private val eventDataSource: EventDataSource by inject(EventDataSource::class.java)

    val id: String = ObjectId().toString()
    var participants: List<Participant> = emptyList()
    private var lastUpdateTimestamp: Long = Long.MIN_VALUE
    val joinCode: String = List(6) { (('A'..'Z') + ('0'..'9')).random() }.joinToString("")

    private val allParticipants = mutableSetOf<String>()
    var startTimestamp = 0L
    private var endTimestamp = 0L

    var lastUpdate = 0L

    var phase = Phase.WAITING
        set(value) {
            synchronized(field) {
                field = value
                phaseChangedListener?.let { change ->
                    change(value)
                }
            }
        }

    private var phaseChangedListener: ((Phase) -> Unit)? = { newPhase ->
        when (newPhase) {
            Phase.IN_PROGRESS -> startTimestamp = System.currentTimeMillis()
            Phase.FINISHED -> finishActivity()
            else -> Unit
        }
    }

    suspend fun broadcast(message: String) {
        lastUpdateTimestamp = System.currentTimeMillis()
        participants.forEach { participant ->
            if (participant.socket.isActive) {
                participant.socket.send(Frame.Text(message))
            }
        }
    }

    suspend fun broadcastToAllExcept(message: String, exceptParticipantId: String) {
        lastUpdateTimestamp = System.currentTimeMillis()
        participants.forEach { participant ->
            if (participant.id != exceptParticipantId && participant.socket.isActive) {
                participant.socket.send(Frame.Text(message))
            }
        }
    }

    suspend fun addParticipant(userId: String, username: String, socket: WebSocketSession) {
        val participant = Participant(userId, username, socket)
        participants = participants + participant
        allParticipants.add(userId)

        val announcement = Announcement(
                id,
                "$username joined.",
                System.currentTimeMillis(),
                Announcement.TYPE_PLAYER_JOINED
        )
        broadcast(gson.toJson(announcement))
    }

    suspend fun removeParticipant(userId: String, username: String) {
        removeParticipantFromList(userId)

        val announcement = Announcement(
                id,
                "$username quit.",
                System.currentTimeMillis(),
                Announcement.TYPE_PLAYER_LEFT
        )

        broadcast(gson.toJson(announcement))

        if (participants.isEmpty()) {
            EventServer.events.remove(id)
        }
    }

    fun reconnectParticipant(participant: Participant) {
        removeParticipantFromList(participant.id)
        participants = participants + participant
    }

    private fun removeParticipantFromList(userId: String) {
        val participantToRemove = participants.find { it.id == userId } ?: return
        participants = participants - participantToRemove
    }

    fun containsParticipant(participantId: String): Boolean {
        return participants.find { it.id == participantId } != null
    }

    private fun finishActivity() {
        endTimestamp = System.currentTimeMillis()
        val durationMinutes = (endTimestamp - startTimestamp) / 1000.0 / 60
        if (durationMinutes < 3) return
        val event = Event(id, name, allParticipants, startTimestamp, endTimestamp, ownerId)
        CoroutineScope(Dispatchers.IO).launch {
            eventDataSource.insertEvent(event)
        }
    }

    suspend fun killEvent() {
        participants.forEach { participant ->
            participant.socket.close()
        }
    }

    enum class Phase {
        WAITING,
        IN_PROGRESS,
        FINISHED
    }
}