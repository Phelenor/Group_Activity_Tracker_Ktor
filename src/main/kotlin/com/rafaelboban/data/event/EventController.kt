package com.rafaelboban.data.event

import com.google.gson.Gson
import com.rafaelboban.EventServer
import com.rafaelboban.data.event.ws.Announcement
import com.rafaelboban.data.event.ws.ParticipantDataList
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
    val joinCode: String = List(6) { (('A'..'Z') + ('0'..'9')).random() }.joinToString("")

    var currentParticipants: List<Participant> = emptyList()
    private val allParticipants = mutableSetOf<String>()
    private val participantsStatusMap = hashMapOf<String, ParticipantData>()

    var lastUpdateTimestamp = 0L
    var startTimestamp = 0L
    private var endTimestamp = 0L

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
        currentParticipants.forEach { participant ->
            if (participant.socket.isActive) {
                participant.socket.send(Frame.Text(message))
            }
        }
    }

    suspend fun broadcastToAllExcept(message: String, exceptParticipantId: String) {
        lastUpdateTimestamp = System.currentTimeMillis()
        currentParticipants.forEach { participant ->
            if (participant.id != exceptParticipantId && participant.socket.isActive) {
                participant.socket.send(Frame.Text(message))
            }
        }
    }

    suspend fun addParticipant(userId: String, username: String, socket: WebSocketSession) {
        val participant = Participant(userId, username, socket)
        currentParticipants = currentParticipants + participant
        allParticipants.add(userId)
        participantsStatusMap[userId] = ParticipantData(
            userId,
            username,
            System.currentTimeMillis()
        )

        val announcement = Announcement(
            id,
            "$username joined.",
            System.currentTimeMillis(),
            Announcement.TYPE_PLAYER_JOINED
        )
        broadcast(gson.toJson(announcement))
    }

    suspend fun updateUserStatus(userId: String, toStatus: ParticipantData.Status? = null) {
        participantsStatusMap[userId]?.apply {
            lastUpdateTimestamp = System.currentTimeMillis()
            toStatus?.let { status = it }
        }
        val participantDataList = ParticipantDataList(participantsStatusMap.values.toList())
        broadcast(gson.toJson(participantDataList))
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

        if (currentParticipants.isEmpty()) {
            EventServer.events.remove(id)
        }
    }

    fun reconnectParticipant(participant: Participant) {
        removeParticipantFromList(participant.id)
        currentParticipants = currentParticipants + participant
    }

    private fun removeParticipantFromList(userId: String) {
        val participantToRemove = currentParticipants.find { it.id == userId } ?: return
        currentParticipants = currentParticipants - participantToRemove
    }

    fun containsParticipant(participantId: String): Boolean {
        return currentParticipants.find { it.id == participantId } != null
    }

    private fun finishActivity() {
        if (startTimestamp == 0L) return
        endTimestamp = System.currentTimeMillis()
        val durationMinutes = (endTimestamp - startTimestamp) / 1000.0 / 60
        if (durationMinutes < 3) return
        val event = Event(id, name, allParticipants, startTimestamp, endTimestamp, ownerId)

        println("$name finished.")
        CoroutineScope(Dispatchers.IO).launch {
            val isSaved = eventDataSource.insertEvent(event)
            println("Event($name) saved: $isSaved")
        }
    }

    suspend fun killEvent() {
        currentParticipants.forEach { participant ->
            participant.socket.close()
        }
    }

    enum class Phase {
        WAITING,
        IN_PROGRESS,
        FINISHED
    }
}