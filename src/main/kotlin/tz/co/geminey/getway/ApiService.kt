package tz.co.geminey.getway

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.*

interface ApiService {
    companion object {
        fun create(): ApiService {
            return ApiServiceImplementation(client =
            HttpClient(CIO) {
                install(Logging) {
                    level = LogLevel.ALL
                }
                install(ContentNegotiation) {
                    json()
                }
                install(Auth) {
                    basic {
                        credentials {
                            BasicAuthCredentials(
                                "25172665c59b9401",
                                "MjY5ZGI4NzgxYWUwMDMyOTRjNGM0ZDQwZDAwYWQ1MzU0YWMwMzFiZDkwYzZjODNlODYxNWI4Y2QyNTgzYjEyMQ=="
                            )
                        }
                    }
                }
            })
        }
    }

//    suspend fun postSMS(data: SMSData): String
}