package xyz.yhsj.ktor.api

import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.routing
import xyz.yhsj.ktor.api.route.commonApi
import xyz.yhsj.ktor.api.route.loginApi
import xyz.yhsj.ktor.api.route.setupApi
import xyz.yhsj.ktor.api.route.userApi
import xyz.yhsj.ktor.api.route.webSocketApi

fun Application.configureApi() {
    routing {
        loginApi()

        authenticate("basic") {
            userApi()
        }

        commonApi()
        setupApi()
        webSocketApi()
    }
}
