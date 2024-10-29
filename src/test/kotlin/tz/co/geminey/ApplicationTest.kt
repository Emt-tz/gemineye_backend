package tz.co.geminey

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*
import tz.co.geminey.plugins.*

class ApplicationTest {
    @Test
    fun testRoot() = testApplication {
        application {
            configureRouting()
        }
        client.get("/card_categories").apply {
            assertEquals(HttpStatusCode.OK, status)
            assertEquals("Hello Categories!", bodyAsText())
        }
    }
}
