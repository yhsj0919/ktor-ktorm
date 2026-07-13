package xyz.yhsj.ktor.api.route

import io.ktor.server.http.content.staticFiles
import io.ktor.server.http.content.staticResources
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.Route
import xyz.yhsj.ktor.api.extension.getExt
import xyz.yhsj.ktor.base.config.imageRootPath
import java.io.File

fun Route.commonApi() {
    getExt("/") { _, _ ->
        call.respondRedirect("/index.html", permanent = true)
    }

    staticFiles("/", File(imageRootPath))
    staticResources("/", "static")
}
