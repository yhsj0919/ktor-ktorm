package xyz.yhsj.ktor.api.route

import io.ktor.server.response.header
import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject
import xyz.yhsj.ktor.auth.AppSession
import xyz.yhsj.ktor.auth.SessionUser
import xyz.yhsj.ktor.dao.entity.user.User
import xyz.yhsj.ktor.api.model.request.user.UserCreateRequest
import xyz.yhsj.ktor.api.model.request.user.UserDeleteRequest
import xyz.yhsj.ktor.api.model.request.user.UserLoginRequest
import xyz.yhsj.ktor.api.model.request.user.UserUpdateRequest
import xyz.yhsj.ktor.api.extension.postExt
import xyz.yhsj.ktor.api.model.response.toUserInfo
import xyz.yhsj.ktor.base.plugins.simpleJWT
import xyz.yhsj.ktor.service.UserService

fun Route.loginApi() {
    val userService by inject<UserService>()

    postExt<UserLoginRequest>("/login") { params, _ ->
        val rasp = userService.login(params)
        val user = rasp.data as User?
        if (rasp.code == 200 && user?.id != null) {
            val sessionUser = SessionUser(
                id = user.id,
                userName = user.userName,
                roleId = user.roleId,
                nickName = user.nickName,
                type = user.type,
            )
            val token = simpleJWT.sign(value = user.id.toString(), entity = AppSession(user = sessionUser))
            call.response.header("Authorization", "Bearer $token")
            rasp.data = user.toUserInfo()
        }
        rasp
    }
}

fun Route.userApi() {
    val userService by inject<UserService>()

    route("/user") {


        /**
         * 删除
         */
        postExt<UserDeleteRequest>("/delete") { user, session ->
            userService.deleteUser(user, session)
        }

        /**
         * 注册
         */
        postExt<UserCreateRequest>("/register") { user, session ->
            userService.register(user, session)
        }

        /**
         * 列表
         */
        postExt("/list") { session ->
            userService.getUsers()
        }

        /**
         * 删除
         */
        postExt<UserUpdateRequest>("/edit") { user, session ->
            userService.editUser(user, session)
        }


    }
}
