package tz.co.geminey.feature.compatibility

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import tz.co.geminey.components.ApiResponse
import tz.co.geminey.components.ApiResponseUtils

fun Route.quizRoute() {
    val quizRepository = QuizRepository()
    get("/question") {
        var response = ApiResponse(code = HttpStatusCode.BadRequest.value, message = HttpStatusCode.BadRequest.description, body = emptyMap<String, Any>())

        try {
            val usernameClaim = call.authentication.principal<JWTPrincipal>()?.payload?.getClaim("username")
            val username = usernameClaim?.asString()

            if (username != null) {
                response = quizRepository.getQuestionsAndAnswerByVersion(username)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        call.respondText(ApiResponseUtils.toJson(response), ContentType.Application.Json)
    }

    post("/update/result") {
        var response =
            ApiResponse(code = HttpStatusCode.BadRequest.value, message = HttpStatusCode.BadRequest.description, body = emptyMap<String, Any>())

        try {
            val usernameClaim = call.authentication.principal<JWTPrincipal>()?.payload?.getClaim("username")
            val username = usernameClaim?.asString()
            if (username != null) {
                val resultList = call.receive<Results>()
                response = quizRepository.saveResults(username, resultList)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        call.respondText(ApiResponseUtils.toJson(response), ContentType.Application.Json)
    }
}