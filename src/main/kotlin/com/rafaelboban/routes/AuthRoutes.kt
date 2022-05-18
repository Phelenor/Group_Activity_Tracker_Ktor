package com.rafaelboban.routes

import com.rafaelboban.data.models.User
import com.rafaelboban.data.requests.LoginRequest
import com.rafaelboban.data.requests.RegisterRequest
import com.rafaelboban.data.responses.TokenResponse
import com.rafaelboban.data.responses.SimpleResponse
import com.rafaelboban.data.responses.UserResponse
import com.rafaelboban.data.users.UserDataSource
import com.rafaelboban.security.hashing.SHA256HashingService
import com.rafaelboban.security.hashing.SaltedHash
import com.rafaelboban.security.token.JwtTokenService
import com.rafaelboban.security.token.TokenClaim
import com.rafaelboban.security.token.TokenConfig
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.register() {
    post("/register") {
        val request = call.receiveOrNull<RegisterRequest>() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val usernameTaken = UserDataSource.getUserByUsername(request.username) != null
        if (usernameTaken) {
            call.respond(HttpStatusCode.OK, SimpleResponse(false, "Username taken."))
            return@post
        }

        val emailTaken = UserDataSource.getUserByEmail(request.email) != null
        if (emailTaken) {
            call.respond(HttpStatusCode.OK, SimpleResponse(false, "Email taken."))
            return@post
        }

        val saltedHash = SHA256HashingService.generateSaltedHash(request.password)
        val user = User(
            username = request.username,
            email = request.email,
            password = saltedHash.hash,
            salt = saltedHash.salt
        )

        val wasAcknowledged = UserDataSource.insertUser(user)
        if (!wasAcknowledged) {
            call.respond(HttpStatusCode.InternalServerError)
            return@post
        }

        call.respond(HttpStatusCode.OK, SimpleResponse(true, "Registered"))
    }
}

fun Route.login(tokenConfig: TokenConfig) {
    post("/login") {
        val request = call.receiveOrNull<LoginRequest>() ?: run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val user = UserDataSource.getUserByEmail(request.email) ?: run {
            call.respond(HttpStatusCode.Conflict, "Incorrect email or password.")
            return@post
        }

        val isPasswordCorrect = SHA256HashingService.verify(
            value = request.password,
            saltedHash = SaltedHash(
                hash = user.password,
                salt = user.salt
            )
        )

        if (isPasswordCorrect.not()) {
            call.respond(HttpStatusCode.Conflict, "Incorrect email or password.")
            return@post
        }

        val token = JwtTokenService.generate(
            config = tokenConfig,
            TokenClaim(
                name = "userId",
                value = user.id.toString()
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
        get("/authenticate") {
            val principal = call.principal<JWTPrincipal>() ?: return@get
            val userId = principal.getClaim("userId", String::class) ?: return@get
            val username = principal.getClaim("username", String::class) ?: return@get
            val email = principal.getClaim("email", String::class) ?: return@get

            val response = UserResponse(userId, username, email)
            call.respond(HttpStatusCode.OK, response)
        }
    }
}

fun Route.getUserInfo() {
    authenticate {
        get("/info") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)
            val username = principal?.getClaim("username", String::class)
            call.respond(HttpStatusCode.OK, "Your userId is $userId and username is $username.")
        }
    }
}