package xyz.yhsj.ktor.auth.extension

import io.ktor.server.application.ApplicationCall
import xyz.yhsj.ktor.auth.PermissionChecker
import xyz.yhsj.ktor.common.error.ForbiddenException
import xyz.yhsj.ktor.base.plugins.PermissionCheckerKey

/** 校验当前请求是否拥有全部指定权限；未传权限时直接跳过。 */
suspend fun ApplicationCall.requirePermissions(permissions: Array<String>) {
    if (permissions.isEmpty()) return

    val checker: PermissionChecker = application.attributes.getOrNull(PermissionCheckerKey)
        ?: error("PermissionPlugin 尚未安装")
    permissions.forEach { permission ->
        require(permission.isNotBlank()) { "权限编码不能为空" }
        if (!checker.hasPermission(this, permission)) {
            throw ForbiddenException()
        }
    }
}
