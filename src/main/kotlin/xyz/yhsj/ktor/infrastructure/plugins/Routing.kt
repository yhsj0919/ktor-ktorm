package xyz.yhsj.ktor.infrastructure.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.statuspages.*
import xyz.yhsj.ktor.common.error.statusPage

fun Application.configureRouting() {
    //请求头
    install(AutoHeadResponse)

    //404,500,异常处理
    install(StatusPages) {
        statusPage()
    }
}
