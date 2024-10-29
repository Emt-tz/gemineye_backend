package tz.co.geminey

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import kotlinx.coroutines.*
import org.ktorm.dsl.*
import org.ktorm.support.mysql.toLowerCase
import tz.co.geminey.config.DBHelper
import tz.co.geminey.config.DBHelper.configureDbVariables
import tz.co.geminey.feature.matches.MatchesTable
import tz.co.geminey.models.CustomerTable
import tz.co.geminey.plugins.*
import java.util.concurrent.TimeUnit

fun main() {
    embeddedServer(Netty, port = 8093, module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    configureDbVariables()
    configureSecurity()
    configureHTTP()
    configureMonitoring()
    configureSerialization()
    configureTemplating()
    configureSockets()
    configureRouting()
}
