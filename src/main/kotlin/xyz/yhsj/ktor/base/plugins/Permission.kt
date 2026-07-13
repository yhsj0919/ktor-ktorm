package xyz.yhsj.ktor.base.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.install
import io.ktor.util.AttributeKey
import xyz.yhsj.ktor.auth.PermissionChecker

internal val PermissionCheckerKey = AttributeKey<PermissionChecker>("PermissionChecker")

class PermissionConfig {
    /** 未配置具体权限来源时默认拒绝所有已声明权限。 */
    var checker: PermissionChecker = PermissionChecker { _, _ -> false }
}

val PermissionPlugin = createApplicationPlugin("Permission", ::PermissionConfig) {
    application.attributes.put(PermissionCheckerKey, pluginConfig.checker)
}

fun Application.configurePermission(configure: PermissionConfig.() -> Unit = {}) {
    install(PermissionPlugin, configure)
}
