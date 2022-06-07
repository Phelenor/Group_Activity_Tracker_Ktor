package com.rafaelboban.di

import com.google.gson.Gson
import com.rafaelboban.data.event.EventDataSource
import com.rafaelboban.data.location.LocationDataSource
import com.rafaelboban.data.marker.MarkerDataSource
import com.rafaelboban.data.user.UserDataSource
import com.rafaelboban.utils.Constants
import org.koin.dsl.module
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

val mainModule = module {
    single {
        KMongo.createClient(
            // connectionString = "mongodb+srv://Phelenor:${System.getenv(Constants.MONGO_PW)}@clustergat.rhrx9ld.mongodb.net/?retryWrites=true&w=majority"
        ).coroutine.getDatabase(Constants.DATABASE_NAME)
    }
    single { UserDataSource(get()) }
    single { MarkerDataSource(get()) }
    single { LocationDataSource(get()) }
    single { EventDataSource(get()) }
    single { Gson() }
}
