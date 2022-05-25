package com.rafaelboban.data.user

import org.koin.java.KoinJavaComponent
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.eq

class UserDataSource(db: CoroutineDatabase) {

    private val users = db.getCollection<User>()

    suspend fun getUserByEmail(email: String): User? {
        return users.findOne(User::email eq email)
    }

    suspend fun getUserByUsername(username: String): User? {
        return users.findOne(User::username eq username)
    }

    suspend fun insertUser(user: User): Boolean {
        return users.insertOne(user).wasAcknowledged()
    }
}