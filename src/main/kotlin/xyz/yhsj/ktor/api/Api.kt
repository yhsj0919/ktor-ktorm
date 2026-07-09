package xyz.yhsj.ktor.api

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import xyz.yhsj.ktor.api.apis.commonApi
import xyz.yhsj.ktor.api.apis.companyApi
import xyz.yhsj.ktor.api.apis.userApi
import xyz.yhsj.ktor.api.apis.webSocketApi

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
