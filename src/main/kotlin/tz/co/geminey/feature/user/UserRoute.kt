package tz.co.geminey.feature.user

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import tz.co.geminey.feature.auth.UpdatePhone

fun Route.userRoute(rootDirectory: String) {
        val authRepository = UserRepository(rootDirectory)
        post("/update/username"){
            try {
                val usernameClaim = call.authentication.principal<JWTPrincipal>()?.payload?.getClaim("username")
                val username = usernameClaim?.asString()
                if (username != null){
                    val req = call.receive<UpdatePhone>()
                    println("update username -> $req")
                    val response = authRepository.updateUserName(req, username)
                    call.respondText(response.toString(), ContentType.Application.Json)
                }else {
                    println("registrationRoute - /update/username -> username not found")
                    call.respond(HttpStatusCode.BadRequest, HttpStatusCode.BadRequest.description)
                }
            } catch (e: Exception) {
                println("user route - username - Exception -> ${e.message}")
                call.respond(HttpStatusCode.BadRequest, HttpStatusCode.BadRequest.description)
            }
        }

    post("/update/profile") {
        try {
            val usernameClaim = call.authentication.principal<JWTPrincipal>()?.payload?.getClaim("username")
            val username = usernameClaim?.asString()
            if (username != null){
                val req = call.receive<EditProfile>()
                println("update username -> $req")
                val response = authRepository.updateUserProfile(req, username)
                call.respondText(response.toString(), ContentType.Application.Json)
            }else {
                println("registrationRoute - /update/username -> username not found")
                call.respond(HttpStatusCode.BadRequest, HttpStatusCode.BadRequest.description)
            }
        }catch (e: Exception){
            println("user route - prof - Exception -> ${e.message}")
            call.respond(HttpStatusCode.BadRequest, HttpStatusCode.BadRequest.description)
        }
    }
}