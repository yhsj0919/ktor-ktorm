package xyz.yhsj.ktor.auth

import io.ktor.server.application.ApplicationCall

/** 权限校验实现，由应用根据角色、菜单或其他权限来源提供。 */
fun interface PermissionChecker {
    suspend fun hasPermission(call: ApplicationCall, permission: String): Boolean
}
