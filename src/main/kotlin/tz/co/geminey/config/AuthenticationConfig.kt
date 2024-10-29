package tz.co.geminey.config

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import java.util.*

fun ApplicationCall.isAuthenticated(): Boolean {
    return authentication.principal<JWTPrincipal>() != null
}

object JwtConfig {
    val jwtAudience = "http://172.20.10.4:8093/"
    val jwtDomain = "http://172.20.10.4:8093/"
    val jwtRealm = "gemineye"
    val jwtSecret = "th!s secret very secrete t0 be secrete f0r secrete"

    val verifier: JWTVerifier = JWT
        .require(Algorithm.HMAC256(jwtSecret))
        .withAudience(jwtAudience)
        .withIssuer(jwtDomain)
        .build()

    data class JwtPayload(val username: String)

    fun makeToken(username: String): String {
      return  JWT.create()
            .withAudience(jwtAudience)
            .withIssuer(jwtDomain)
            .withClaim("username", username)
//            .withExpiresAt(Date(System.currentTimeMillis() + (6 * 60 * 60 * 1000)))
            .withExpiresAt(Date(System.currentTimeMillis() + (5 * 1000)))
            .sign(Algorithm.HMAC256(jwtSecret))
    }
}