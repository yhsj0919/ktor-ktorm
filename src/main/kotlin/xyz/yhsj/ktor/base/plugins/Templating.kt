package xyz.yhsj.ktor.base.plugins


import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.thymeleaf.Thymeleaf
import io.ktor.server.thymeleaf.ThymeleafContent
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver
import io.ktor.server.application.*
import io.ktor.server.routing.*

fun Application.configureTemplating() {


    install(Thymeleaf) {
        setTemplateResolver(ClassLoaderTemplateResolver().apply {
            prefix = "thymeleaf/"
            suffix = ".html"
            characterEncoding = "utf-8"
        })
    }

}

