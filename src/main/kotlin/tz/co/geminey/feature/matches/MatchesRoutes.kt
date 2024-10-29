package tz.co.geminey.feature.matches

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import tz.co.geminey.components.ApiResponse
import tz.co.geminey.components.ApiResponseUtils

fun Route.matchesRoute() {
    val matchesRepository = MatchesRepository()

    //TODO remove this
    get("/matches-reset") {
        val response = try {
            matchesRepository.resetMatches()
        } catch (e: Exception) {
            e.printStackTrace()
            ApiResponse(
                code = 500,
                message = "Something went wrong try again later",
                body = emptyMap<String, Any>()
            )
        }
        call.respondText(ApiResponseUtils.toJson(response), ContentType.Application.Json)
    }

    //TODO remove this
    get("/matches-set") {
        val response = try {
            matchesRepository.setMatches()
        } catch (e: Exception) {
            e.printStackTrace()
            ApiResponse(
                code = 500,
                message = "Something went wrong try again later",
                body = emptyMap<String, Any>()
            )
        }
        call.respondText(ApiResponseUtils.toJson(response), ContentType.Application.Json)
    }

    get("/meet-me") {
        var response = ApiResponse(
            code = HttpStatusCode.Unauthorized.value,
            message = HttpStatusCode.Unauthorized.description,
            body = emptyMap<String, Any>()
        )
        try {
            val usernameClaim = call.authentication.principal<JWTPrincipal>()?.payload?.getClaim("username")
            val username = usernameClaim?.asString()
            if (username != null) {
                println("CHECK ID:: $username")
                response = matchesRepository.fetchMatchesById(username.toInt())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        println("MATCHES RES:: ${ApiResponseUtils.toJson(response)}")
        call.respondText(ApiResponseUtils.toJson(response), ContentType.Application.Json)
    }

    post("/update/swiped-profiles") {
        val response = try {
            val swipedProfiles = call.receive<UpdateSwipedProfiles>()
            matchesRepository.updateSwipedProfilesInMatches(swipedProfiles)
        } catch (e: Exception) {
            e.printStackTrace()
            ApiResponse(code = 500, message = "Something went wrong try again later", body = emptyMap<String, Any>())
        }
        call.respondText(ApiResponseUtils.toJson(response), ContentType.Application.Json)
    }

    post("/update/swiped-matches") {
        val response = try {
            val swipedProfiles = call.receive<UpdateSwipedMatches>()
            matchesRepository.updateSwipedMatches(swipedProfiles)
        } catch (e: Exception) {
            e.printStackTrace()
            ApiResponse(code = 500, message = "Something went wrong try again later", body = emptyMap<String, Any>())
        }
        call.respondText(ApiResponseUtils.toJson(response), ContentType.Application.Json)
    }

    post("update/favourites") {
        var response = ApiResponse(
            code = HttpStatusCode.BadRequest.value,
            message = HttpStatusCode.BadRequest.description,
            body = emptyMap<String, Any>()
        )
        try {
            val fav = call.receive<UpdateSwipedMatches>()
            response = matchesRepository.updateFavourite(fav)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        call.respondText(ApiResponseUtils.toJson(response), ContentType.Application.Json)
    }

    get("/fetch-matches") {
        var response = ApiResponse(
            code = HttpStatusCode.Unauthorized.value,
            message = HttpStatusCode.Unauthorized.description,
            body = emptyMap<String, Any>()
        )
        try {
            val usernameClaim = call.authentication.principal<JWTPrincipal>()?.payload?.getClaim("username")
            val username = usernameClaim?.asString()
            if (username != null) {
                response = matchesRepository.fetchLikedMatchesById(username.toInt())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        println("MATCHES RES:: ${ApiResponseUtils.toJson(response)}")
        call.respondText(ApiResponseUtils.toJson(response), ContentType.Application.Json)
    }

    get("/runMatches") {
        val scope = CoroutineScope(Dispatchers.Default)
        scope.launch {
            matchesRepository.matchAndStoreMatches()
        }
        call.respondText("called", ContentType.Application.Json)
    }
}