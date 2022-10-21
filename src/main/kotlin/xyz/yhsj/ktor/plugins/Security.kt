package xyz.yhsj.ktor.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.sessions.*
import xyz.yhsj.ktor.auth.jwtCheck
import xyz.yhsj.ktor.auth.sessionCheck
import xyz.yhsj.ktor.auth.setSession


val simpleJWT = JWT.require(Algorithm.HMAC256("secret")).build()


fun Application.configureSecurity() {
    authentication {

        val verifier = JWT.require(Algorithm.HMAC256("secret")).build()

        jwt {
            verifier(verifier)
            validate { credential ->
                JWTPrincipal(credential.payload)
            }
        }
    }
//    //Cookie支持
//    install(Sessions) {
//        setSession()
//    }

    //session校验
    install(Authentication) {
        jwtCheck()
    }
}
