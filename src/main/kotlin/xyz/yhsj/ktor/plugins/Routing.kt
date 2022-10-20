package xyz.yhsj.ktor.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.routing.*
import xyz.yhsj.ktor.api.*
import xyz.yhsj.ktor.status.statusPage

fun Application.configureRouting() {
    //请求头
    install(AutoHeadResponse)

    //404,500,异常处理
    install(StatusPages) {
        statusPage()
    }

    routing {
        //这个是带权限验证的，可以校验不同的权限
        authenticate("admin") {

        }
        authenticate("basic") {

        }
        authenticate("common") {

        }

        //下面的不含权限验证
        commonApi()

        webSocketRoute()
    }
}
