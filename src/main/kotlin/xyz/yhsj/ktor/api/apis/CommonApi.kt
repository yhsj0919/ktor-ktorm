package xyz.yhsj.ktor.api.apis

import io.ktor.server.http.content.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import xyz.yhsj.ktor.ext.getExt
import xyz.yhsj.ktor.imageRootPath
import java.io.File

fun Route.commonApi() {
    getExt("/") { _, _ ->
        call.respondRedirect("/index.html", permanent = true)
    }

    staticFiles("/", File(imageRootPath))
    staticResources("/", "static")
}
