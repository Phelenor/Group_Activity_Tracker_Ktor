package com.rafaelboban.data.user

import com.rafaelboban.data.Database
import org.litote.kmongo.eq

object UserDataSource {

    private val users = Database.database.getCollection<User>()

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