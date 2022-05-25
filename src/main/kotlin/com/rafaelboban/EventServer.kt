package com.rafaelboban

import com.rafaelboban.data.event.Event
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap

object EventServer {

    val events = ConcurrentHashMap<String, Event>()

    init {
        CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                delay(5000L)
                events.forEach { entry ->

                }
                println("Active events: ${events.map { "${it.value.name} - ${it.value.participants.size} - ${it.value.participants.map { it.id }}" }}")
            }
        }
    }
}