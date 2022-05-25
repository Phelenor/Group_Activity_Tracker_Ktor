package com.rafaelboban.data.message

import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

class MessageDataSource(db: CoroutineDatabase) {

    private val messages = db.getCollection<Message>()

    suspend fun getMessagesForEvent(eventId: String): List<Message> {
        return messages.find(Message::id eq eventId)
            .descendingSort(Message::timestamp)
            .toList()
    }

    suspend fun insertMessage(message: Message) {
        messages.insertOne(message)
    }
} 