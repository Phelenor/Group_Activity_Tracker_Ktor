package com.rafaelboban.data.event

import com.rafaelboban.data.location.LocationPoint
import com.rafaelboban.data.user.User
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

class EventDataSource(db: CoroutineDatabase) {

    private val events = db.getCollection<Event>()
    private val subEvents = db.getCollection<SubEvent>()
    private val locations = db.getCollection<LocationPoint>()
    private val users = db.getCollection<User>()

    suspend fun insertEvent(event: Event): Boolean {
        return events.insertOne(event).wasAcknowledged()
    }

    suspend fun insertSubEvent(subEvent: SubEvent): Boolean {
        return subEvents.insertOne(subEvent).wasAcknowledged()
    }

    suspend fun getEventsForUser(userId: String): List<EventData> {
        val events = subEvents.find(SubEvent::userId eq userId).toList().map { event ->
            val parentEvent = events.findOne(Event::id eq event.eventId) ?: throw IllegalStateException()
            val points = locations.find(LocationPoint::eventId eq parentEvent.id, LocationPoint::userId eq userId).toList()

            val parentEventOwnerSubEvent =
                subEvents.findOne(SubEvent::userId eq parentEvent.ownerId) ?: throw IllegalStateException()
            val parentDistance = parentEventOwnerSubEvent.distance


            val participantUsernames = parentEvent.participants.mapNotNull { userId ->
                val user = users.findOne(User::id eq userId)
                user?.username
            }

            EventData(
                event.id,
                event.startTimestamp,
                event.endTimestamp,
                event.distance,
                parentEvent.startTimestamp,
                parentEvent.endTimestamp,
                parentDistance,
                participantUsernames,
                points
            )
        }

        return events
    }
}