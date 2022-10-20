package xyz.yhsj.ktor.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.sessions.*
import xyz.yhsj.ktor.auth.sessionCheck
import xyz.yhsj.ktor.auth.setSession

fun Application.configureSecurity() {
    //Cookie支持
    install(Sessions) {
        setSession()
    }

    //session校验
    install(Authentication) {
        sessionCheck()
    }
}
