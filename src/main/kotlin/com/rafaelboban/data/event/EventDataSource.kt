package com.rafaelboban.data.event

import com.rafaelboban.data.location.LocationPoint
import com.rafaelboban.data.location.LocationPointData
import com.rafaelboban.data.user.User
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

class EventDataSource(db: CoroutineDatabase) {

    private val events = db.getCollection<Event>()
    private val subEvents = db.getCollection<SubEvent>()
    private val locations = db.getCollection<LocationPointData>()
    private val users = db.getCollection<User>()

    suspend fun insertEvent(event: Event): Boolean {
        return events.insertOne(event).wasAcknowledged()
    }

    suspend fun insertSubEvent(subEvent: SubEvent): Boolean {
        return subEvents.insertOne(subEvent).wasAcknowledged()
    }

    suspend fun getEventsForUser(userId: String): List<EventData> {
        val events = subEvents.find(SubEvent::userId eq userId).toList().mapNotNull { event ->
            val parentEvent = events.findOne(Event::id eq event.eventId) ?: return@mapNotNull null
            val participantUsernames = parentEvent.participants.mapNotNull { userId ->
                val user = users.findOne(User::id eq userId)
                user?.username
            }

            EventData(
                event.id,
                parentEvent.id,
                parentEvent.name,
                event.startTimestamp,
                event.endTimestamp,
                event.distance,
                parentEvent.startTimestamp,
                parentEvent.endTimestamp,
                participantUsernames
            )
        }

        return events
    }

    suspend fun getPointsForEvent(eventId: String, userId: String): List<LocationPoint> {
        return locations.find(LocationPointData::eventId eq eventId, LocationPointData::userId eq userId)
            .toList()
            .sortedBy { it.timestamp }
            .map { LocationPoint(it.latitude, it.longitude) }
    }
}