package tz.co.geminey.getway

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.auth.*
import io.ktor.client.plugins.auth.providers.*
import io.ktor.client.request.*
import io.ktor.client.request.headers
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

class HttpHelper(val client: HttpClient) {

    @OptIn(ExperimentalEncodingApi::class)
    suspend fun postSMS(data: Any): String {
        try {
            val auth = "AC3d0afe540aad4fc2910463708f7b2491:8a45ef3981460c3b5313a364c53e82cb"
            val bytes = Base64.encode(auth.toByteArray(Charsets.US_ASCII))
            val res = client.post("https://apisms.beem.africa/v1/send") {
                contentType(ContentType.Application.Json)
                setBody(data)
                headers { append(HttpHeaders.Authorization, "Basic $bytes") }
            }
            return res.bodyAsText()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return ""
    }
}