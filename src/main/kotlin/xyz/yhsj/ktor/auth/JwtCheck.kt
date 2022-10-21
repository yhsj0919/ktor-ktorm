package xyz.yhsj.ktor.auth

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import xyz.yhsj.ktor.entity.resp.CommonResp
import xyz.yhsj.ktor.ext.sessionOrNull

/**
 * session校验
 * 下面两种校验方式只是为了验证功能
 */
fun AuthenticationConfig.jwtCheck() {
    //admin校验
    jwt(name = "admin") {
        challenge { defaultScheme, realm ->

            val session = call.sessionOrNull<AppSession>()
            if (session != null && session.user?.companyId == null) {
                call.respond(HttpStatusCode.OK, CommonResp.error(msg = "换个超管账号再来吧~"))
            } else {
                call.respond(HttpStatusCode.OK, CommonResp.login())
            }
        }

        validate { credential ->

            UserIdPrincipal(credential.payload.getClaim("id").asString())

//            if (session.user?.type != -1) {
//                null
//            } else {
//                //这里返回null就会调用challenge
//                session
//            }
        }
        skipWhen { call ->
            val skipPath = arrayListOf("/admin/login")
            call.request.path() in skipPath
        }
    }
    //基础校验
    jwt(name = "basic") {
        challenge {defaultScheme, realm ->
            val session = call.sessionOrNull<AppSession>()
            if (session != null && session.user?.companyId == null) {
                call.respond(HttpStatusCode.OK, CommonResp.error(msg = "换个普通账号再来吧~"))
            } else {
                call.respond(HttpStatusCode.OK, CommonResp.login())
            }
        }
        validate { credential ->
//            return@validate if (session.user?.companyId == null) {
//                null
//            } else {
//                //这里返回null就会调用challenge
//                session
//            }
            UserIdPrincipal(credential.payload.getClaim("id").asString())
        }
        skipWhen { call ->
            val skipPath = arrayListOf("/login")
            call.request.path() in skipPath
        }
    }

    //基础校验
    jwt(name = "common") {
        challenge {defaultScheme, realm ->
            call.respond(HttpStatusCode.OK, CommonResp.login())
        }
        validate { credential ->
//            return@validate if (session.user == null) {
//                null
//            } else {
//                //这里返回null就会调用challenge
//                session
//            }

            UserIdPrincipal(credential.payload.getClaim("id").asString())
        }
    }

}