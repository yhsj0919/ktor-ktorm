package xyz.yhsj.ktor.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import xyz.yhsj.ktor.auth.SimpleJWT
import xyz.yhsj.ktor.auth.jwtCheck


val simpleJWT = SimpleJWT("secret")

fun Application.configureSecurity() {
    //session校验
    install(Authentication) {
        jwtCheck()
    }
}
