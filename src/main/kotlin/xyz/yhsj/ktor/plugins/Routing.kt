package xyz.yhsj.ktor.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.autohead.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.routing.*
import xyz.yhsj.ktor.api.commonApi
import xyz.yhsj.ktor.api.jwtApi
import xyz.yhsj.ktor.api.webSocketRoute
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
            jwtApi()
        }
        authenticate("common") {

        }

        //下面的不含权限验证
        commonApi()

        webSocketRoute()
    }
}
