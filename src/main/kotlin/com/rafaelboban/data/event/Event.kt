package com.rafaelboban.data.event

import com.google.gson.Gson
import com.rafaelboban.EventServer
import com.rafaelboban.data.event.ws.Announcement
import com.rafaelboban.data.event.ws.ParticipantList
import io.ktor.websocket.*
import kotlinx.coroutines.*
import org.koin.java.KoinJavaComponent.inject
import java.util.UUID

class Event(val name: String, private val ownerId: String) {

    private val gson: Gson by inject(Gson::class.java)

    val id: String = UUID.randomUUID().toString()
    var participants: List<Participant> = emptyList()
    private var lastUpdateTimestamp: Long = Long.MIN_VALUE
    val joinCode: String = List(8) { (('A'..'Z') + ('0'..'9')).random() }.joinToString("")

    private var broadcastStatusJob: Job? = null

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
            Phase.IN_PROGRESS -> eventInProgress()
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

    private suspend fun broadcastParticipantStatus() {
        val data = participants.sortedWith(compareBy<Participant> { it.id == ownerId }.thenBy { it.isActive })
            .map { ParticipantData(it.username, it.distance, it.isActive) }
        broadcast(gson.toJson(ParticipantList(data)))
    }

    suspend fun addParticipant(userId: String, username: String, socket: WebSocketSession) {
        val participant = Participant(userId, username, socket)
        participants = participants + participant

        val announcement = Announcement(
            id,
            "$username joined.",
            System.currentTimeMillis(),
            Announcement.TYPE_PLAYER_JOINED
        )
        broadcast(gson.toJson(announcement))
        broadcastParticipantStatus()
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
        broadcastParticipantStatus()

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

    private fun eventInProgress() {
        broadcastStatusJob = CoroutineScope(Dispatchers.IO).launch {
            while (phase == Phase.IN_PROGRESS) {
                broadcastParticipantStatus()
                delay(5000L)
            }
        }
    }

    private fun finishActivity() {
        broadcastStatusJob?.cancel()
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