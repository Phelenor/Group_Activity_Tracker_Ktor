package com.rafaelboban

import com.rafaelboban.data.event.EventController
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap

object EventServer {

    val events = ConcurrentHashMap<String, EventController>()

    private val inactiveEvents = ConcurrentHashMap<String, Long>()

    init {
        CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                delay(5000L)

                events.forEach { entry ->
                    val currentTime = System.currentTimeMillis()
                    if (entry.value.participants.isEmpty()) {
                        inactiveEvents[entry.key] = currentTime
                    } else if (currentTime - entry.value.lastUpdate > 30 * 60 * 1000) {
                        inactiveEvents[entry.key] = currentTime
                    }
                }

                val status = events.map { "${it.value.name}: ${it.value.participants.map { it.username }}\n" }
                println("Active events:\n$status")
            }
        }

        CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                delay(60000L)

                inactiveEvents.forEach { entry ->
                    if (System.currentTimeMillis() - entry.value > 60000) {
                        events[entry.key]?.killEvent()
                        events.remove(entry.key)
                        inactiveEvents.remove(entry.key)
                    }
                }
            }
        }
    }
}