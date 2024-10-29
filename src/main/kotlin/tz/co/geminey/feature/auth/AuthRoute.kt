package tz.co.geminey.feature.auth

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import tz.co.geminey.components.ApiResponseUtils
import tz.co.geminey.feature.auth.repositories.AuthRepository
import tz.co.geminey.feature.auth.repositories.RegistrationRepository

fun Application.authRoute(rootDirectory: String) {
    routing {
        val registrationRepository = RegistrationRepository(rootDirectory)
        val authRepository = AuthRepository()

        post("/resend-otp") {
            try {
                val req = call.receive<ResendOtp>()
                val res = registrationRepository.onResendOTP(req.username)
                call.respondText(ApiResponseUtils.toJson(res), ContentType.Application.Json)
            } catch (e: Throwable) {
                e.printStackTrace()
                call.respond(HttpStatusCode.BadRequest, HttpStatusCode.BadRequest.description)
            }
        }

        post("/validate/otp") {
            try {
                val req = call.receive<OTPValidation>()
                val res = registrationRepository.validateOTP(req)
                call.respondText(ApiResponseUtils.toJson(res), ContentType.Application.Json)
            } catch (e: Exception) {
                e.printStackTrace()
                call.respond(HttpStatusCode.BadRequest, HttpStatusCode.BadRequest.description)
            }
        }

        post("/register") {
            try {
                val req = call.receive<RegisterRequest>()
                val response = registrationRepository.registerUser(req)
                call.respondText(response.toString(), ContentType.Application.Json)
            } catch (e: Exception) {
                println("registrationRoute - Exception -> ${e.message}")
                call.respond(HttpStatusCode.BadRequest, HttpStatusCode.BadRequest.description)
            }
        }

        post("/login") {
            try {
                val loginRequest = call.receive<LoginRequest>()
                val response = authRepository.getLoginDetails(loginRequest)
                call.respondText(response.toString(), ContentType.Application.Json)
            } catch (e: Exception) {
                println("registrationRoute - Exception -> ${e.message}")
                call.respond(HttpStatusCode.BadRequest, HttpStatusCode.BadRequest.description)
            }
        }

        post("/check-user") {
            try {
                val userReq = call.receive<Username>()
                val response = authRepository.getUserByUserName(userReq)
                call.respondText(response.toString(), ContentType.Application.Json)
            } catch (e: Exception) {
                println("registrationRoute - Exception -> ${e.message}")
                call.respond(HttpStatusCode.BadRequest, HttpStatusCode.BadRequest.description)
            }
        }

        //TODO encrypt password from mobile level
        post("/change-password") {
            try {
                val req = call.receive<LoginRequest>()
                println("Check user -> $req")
                val response = authRepository.changePassword(req)
                call.respondText(response.toString(), ContentType.Application.Json)
            } catch (e: Exception) {
                println("registrationRoute - Exception -> ${e.message}")
                call.respond(HttpStatusCode.BadRequest, HttpStatusCode.BadRequest.description)
            }
        }
    }

}