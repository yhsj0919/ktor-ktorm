package xyz.yhsj.ktor.api

import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.routing
import xyz.yhsj.ktor.api.route.commonApi
import xyz.yhsj.ktor.api.route.companyApi
import xyz.yhsj.ktor.api.route.userApi
import xyz.yhsj.ktor.api.route.webSocketApi

fun Application.configureApi() {
    routing {
        authenticate("admin") {
            companyApi()
        }
        authenticate("basic") {
            userApi()
        }

        commonApi()
        webSocketApi()
    }
}
