package com.rafaelboban.di

import com.google.gson.Gson
import com.rafaelboban.utils.Constants
import org.koin.dsl.module
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

val databaseModule = module {
    single { KMongo.createClient().coroutine.getDatabase(Constants.DATABASE_NAME) }
}

val gsonModule = module {
    single { Gson() }
}