package com.rafaelboban.data.users

import com.rafaelboban.data.Database
import com.rafaelboban.data.models.User
import org.litote.kmongo.eq

object UserDataSource {

    private val db = Database.database

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