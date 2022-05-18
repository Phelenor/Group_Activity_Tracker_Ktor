package com.rafaelboban.data

import com.rafaelboban.utils.Constants.DATABASE_NAME
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

object Database {

    val database = KMongo.createClient().coroutine.getDatabase(DATABASE_NAME)
}