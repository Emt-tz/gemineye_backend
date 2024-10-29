package tz.co.geminey.routes

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import tz.co.geminey.models.Interets


fun Route.interestRoute(){
    get("/") {
        call.respondText("Hello John!")
    }

    post {
//        val interest = call.receive(Interets)
    }
}