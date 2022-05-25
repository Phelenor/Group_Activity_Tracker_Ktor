package com.rafaelboban

import com.rafaelboban.di.databaseModule
import com.rafaelboban.di.gsonModule
import io.ktor.server.application.*
import com.rafaelboban.plugins.*
import com.rafaelboban.security.token.TokenConfig
import com.rafaelboban.utils.Constants.THIRTY_DAYS_MILIS
import io.ktor.server.application.ApplicationCallPipeline.ApplicationPhase.Plugins
import io.ktor.server.sessions.*
import io.ktor.util.*
import org.koin.core.context.startKoin

fun main(args: Array<String>): Unit =
    io.ktor.server.netty.EngineMain.main(args)

@Suppress("unused") // application.conf references the main function. This annotation prevents the IDE from marking it as unused.
fun Application.module() {

    val jwtTokenConfig = TokenConfig(
        issuer = environment.config.property("jwt.issuer").getString(),
        audience = environment.config.property("jwt.audience").getString(),
        expiresIn = THIRTY_DAYS_MILIS,
        secret = System.getenv("JWT_SECRET")
    )

    startKoin { modules(databaseModule, gsonModule) }

    configureSessions()
    configureSockets()
    configureSecurity(jwtTokenConfig)
    configureRouting(jwtTokenConfig)
    configureHTTP()
    configureMonitoring()
    configureSerialization()

    intercept(Plugins) {
        if (call.sessions.get<TrackingSession>() == null) {
            val userId = call.parameters["user_id"] ?: return@intercept
            call.sessions.set(TrackingSession(userId, generateNonce()))
        }
    }
}
