package com.rafaelboban.security.hashing

import com.password4j.Password

object HashingService {

    fun generateHash(password: String): String {
        return Password.hash(password).addRandomSalt().withArgon2().result
    }

    fun verifyPassword(password: String, hash: String): Boolean {
        return Password.check(password, hash).withArgon2()
    }
}