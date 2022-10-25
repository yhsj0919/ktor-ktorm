package xyz.yhsj.ktor.auth

import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import xyz.yhsj.ktor.JWT_KEY
import xyz.yhsj.ktor.entity.resp.CommonResp
import xyz.yhsj.ktor.plugins.simpleJWT


/**
 * session校验
 * 下面两种校验方式只是为了验证功能
 */
fun AuthenticationConfig.jwtCheck() {
    //admin校验
    jwt(name = "admin") {
        verifier(simpleJWT.verifier)
        skipWhen { call ->
            val skipPath = arrayListOf("/admin/login")
            call.request.path() in skipPath
        }

        validate { credential ->
            if (credential.payload.getClaim(JWT_KEY).asString() != null) {
                JWTPrincipal(credential.payload)
            } else {
                null
            }
        }

        challenge { _, _ ->
            call.respond(HttpStatusCode.OK, CommonResp.login())
        }


    }
    //基础校验
    jwt(name = "basic") {
        verifier(simpleJWT.verifier)
        skipWhen { call ->
            val skipPath = arrayListOf("/login")
            call.request.path() in skipPath
        }

        validate { credential ->
            if (credential.payload.getClaim(JWT_KEY).asString() != null) {
                JWTPrincipal(credential.payload)
            } else {
                null
            }
        }

        challenge { _, _ ->
            call.respond(HttpStatusCode.OK, CommonResp.login())
        }
    }

    //基础校验
    jwt(name = "common") {
        verifier(simpleJWT.verifier)

        validate { credential ->
            if (credential.payload.getClaim(JWT_KEY).asString() != null) {
                JWTPrincipal(credential.payload)
            } else {
                null
            }
        }

        challenge { _, _ ->
            call.respond(HttpStatusCode.OK, CommonResp.login())
        }
    }

}