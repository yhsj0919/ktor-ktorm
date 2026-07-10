package xyz.yhsj.ktor.api.route

import io.ktor.server.response.header
import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject
import xyz.yhsj.ktor.auth.AppSession
import xyz.yhsj.ktor.persistence.entity.user.SysUser
import xyz.yhsj.ktor.persistence.entity.user.User
import xyz.yhsj.ktor.api.extension.postExt
import xyz.yhsj.ktor.infrastructure.plugins.simpleJWT
import xyz.yhsj.ktor.service.UserService
import xyz.yhsj.ktor.common.validation.VG

fun Route.userApi() {
    val userService by inject<UserService>()
    /**
     * 登录
     */
    postExt<SysUser>("/login", VG.Login::class.java) { params, _ ->
        val rasp = userService.login(params)
        val user = rasp.data as User?
        if (rasp.code == 200 && user?.id != null) {
            val token = simpleJWT.sign(value = user.id.toString(), entity = AppSession(user = user))
            call.response.header("Authorization", "Bearer $token")
        }
        rasp
    }

    route("/user") {


        /**
         * 删除
         */
        postExt<SysUser>("/delete", VG.Delete::class.java) { user, session ->
            userService.deleteUser(user, session)
        }

        /**
         * 注册
         */
        postExt<SysUser>("/register", VG.Add::class.java) { user, session ->
            userService.register(user, session)
        }

        /**
         * 列表
         */
        postExt("/list") { session ->
            userService.getUsers()
        }

        /**
         * 列表
         */
        postExt<SysUser>("/listWithCompany") { params, session ->
            userService.getUsersWithCompany(params, session)
        }

        /**
         * 删除
         */
        postExt<SysUser>("/edit", VG.Update::class.java) { user, session ->
            userService.editUser(user, session)
        }


    }
}
