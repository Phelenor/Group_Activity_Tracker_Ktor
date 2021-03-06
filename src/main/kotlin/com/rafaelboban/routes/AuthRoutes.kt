package com.rafaelboban.routes

import com.rafaelboban.data.user.User
import com.rafaelboban.data.requests.LoginRequest
import com.rafaelboban.data.requests.RegisterRequest
import com.rafaelboban.data.responses.TokenResponse
import com.rafaelboban.data.responses.SimpleResponse
import com.rafaelboban.data.responses.UserResponse
import com.rafaelboban.data.user.UserDataSource
import com.rafaelboban.plugins.TrackingSession
import com.rafaelboban.security.hashing.HashingService
import com.rafaelboban.security.token.JwtTokenService
import com.rafaelboban.security.token.TokenClaim
import com.rafaelboban.security.token.TokenConfig
import com.rafaelboban.utils.Constants
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.util.*

fun Route.register(userDataSource: UserDataSource) {
    post("/api/register") {
        val request = call.receiveOrNull<RegisterRequest>() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val usernameTaken = userDataSource.getUserByUsername(request.username) != null
        if (usernameTaken) {
            call.respond(HttpStatusCode.OK, SimpleResponse(false, "Username taken."))
            return@post
        }

        val emailTaken = userDataSource.getUserByEmail(request.email) != null
        if (emailTaken) {
            call.respond(HttpStatusCode.OK, SimpleResponse(false, "Email taken."))
            return@post
        }

        val passwordHash = HashingService.generateHash(request.password)
        val user = User(
            username = request.username,
            email = request.email,
            password = passwordHash
        )

        val wasAcknowledged = userDataSource.insertUser(user)
        if (!wasAcknowledged) {
            call.respond(HttpStatusCode.InternalServerError)
            return@post
        }

        call.respond(HttpStatusCode.OK, SimpleResponse(true, "Registered"))
    }
}

fun Route.login(userDataSource: UserDataSource, tokenConfig: TokenConfig) {
    post("/api/login") {
        val request = call.receiveOrNull<LoginRequest>() ?: run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val user = userDataSource.getUserByEmail(request.email) ?: run {
            call.respond(HttpStatusCode.Conflict, "Incorrect email or password.")
            return@post
        }

        val isPasswordCorrect = HashingService.verifyPassword(
            password = request.password,
            hash = user.password
        )

        if (isPasswordCorrect.not()) {
            call.respond(HttpStatusCode.Conflict, "Incorrect email or password.")
            return@post
        }

        val token = JwtTokenService.generate(
            config = tokenConfig,
            TokenClaim(
                name = "userId",
                value = user.id
            ),
            TokenClaim(
                name = "username",
                value = user.username
            ),
            TokenClaim(
                name = "email",
                value = user.email
            )
        )

        call.respond(HttpStatusCode.OK, TokenResponse(token))
    }
}

fun Route.authenticate() {
    authenticate {
        get("/api/authenticate") {
            val principal = call.principal<JWTPrincipal>() ?: return@get
            val userId = principal.getClaim("userId", String::class) ?: return@get
            val username = principal.getClaim("username", String::class) ?: return@get
            val email = principal.getClaim("email", String::class) ?: return@get

            if (call.sessions.get<TrackingSession>() == null) {
                call.sessions.set(TrackingSession(userId, generateNonce()))
            }

            val response = UserResponse(userId, username, email)
            call.respond(HttpStatusCode.OK, response)
        }
    }
}
