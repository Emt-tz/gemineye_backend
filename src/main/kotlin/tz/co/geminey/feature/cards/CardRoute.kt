package tz.co.geminey.feature.cards

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import tz.co.geminey.components.ApiResponse
import tz.co.geminey.components.ApiResponseUtils

fun Route.cardRoute() {
    val cardRepository = CardRepository()
    get("/cards") {
        val response = cardRepository.getAllCards()
        call.respondText(ApiResponseUtils.toJson(response), ContentType.Application.Json)
    }

    post("/filter/cards") {
        val cardRequest = call.receive<CardRequest>()
        val response =
            if (cardRequest.status != 0) {
                if (cardRequest.category != 0) {
                    cardRepository.getCardsByRelationStatusAndCategory(
                        status = cardRequest.status,
                        category = cardRequest.category
                    )
                } else {
                    cardRepository.getCardsByRelationStatus(status = cardRequest.status)
                }
            } else {
                ApiResponse(
                    code = HttpStatusCode.BadRequest.value,
                    message = HttpStatusCode.BadRequest.description,
                    body = emptyMap<String, Any>()
                )
            }

        call.respondText(ApiResponseUtils.toJson(response), ContentType.Application.Json)
    }
}