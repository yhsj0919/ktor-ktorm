package xyz.yhsj.ktor.api.route

import io.ktor.server.routing.Route
import org.koin.ktor.ext.inject
import xyz.yhsj.ktor.api.extension.postExt
import xyz.yhsj.ktor.api.model.request.user.AdminSetupRequest
import xyz.yhsj.ktor.service.UserService

/** 首次启动时创建系统管理员，初始化完成后接口自动失效。 */
fun Route.setupApi() {
    val userService by inject<UserService>()

    postExt<AdminSetupRequest>("/setup/admin") { params, _ ->
        userService.initializeAdmin(params)
    }
}
