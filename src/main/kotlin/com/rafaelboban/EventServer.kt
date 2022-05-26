package com.rafaelboban

import com.rafaelboban.data.event.Event
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap

object EventServer {

    val events = ConcurrentHashMap<String, Event>()

    private val eventsToRemove = mutableListOf<String>()

    init {
        CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                delay(5000L)

                eventsToRemove.removeIf { events[it]?.participants?.isNotEmpty() == true }

                eventsToRemove.forEach { id ->
                    if (events[id]?.participants?.isEmpty() == true) {
                        events[id]?.killEvent()
                        events.remove(id)
                    }
                }

                events.forEach { entry ->
                    if (entry.value.participants.isEmpty()) {
                        eventsToRemove.add(entry.key)
                    }
                }
                println("Active events: ${events.map { "${it.value.name} - ${it.value.participants.size} - ${it.value.participants.map { it.username }}" }}")
            }
        }
    }
}