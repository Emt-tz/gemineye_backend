package tz.co.geminey.feature.card_categories

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.cardCategoriesRoute(){
    val cardCategoryRepository = CardCategoryRepository()
    get("/card_categories") {
        val response = cardCategoryRepository.getCategories()
        call.respondText(response.toString(), ContentType.Application.Json)
    }

    post {

    }
}