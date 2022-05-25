package com.rafaelboban.data.message

import org.litote.kmongo.coroutine.CoroutineDatabase

class MessageDataSource(db: CoroutineDatabase) {

    private val messages = db.getCollection<Message>()

    suspend fun getAllMessages(): List<Message> {
        return messages.find()
            .descendingSort(Message::timestamp)
            .toList()
    }

    suspend fun insertMessage(message: Message) {
        messages.insertOne(message)
    }
} 