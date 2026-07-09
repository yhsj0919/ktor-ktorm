package xyz.yhsj.ktor.plugins

import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import xyz.yhsj.ktor.service.CommonService
import xyz.yhsj.ktor.service.CompanyService
import xyz.yhsj.ktor.service.UserService
import xyz.yhsj.ktor.service.impl.CommonServiceImpl
import xyz.yhsj.ktor.service.impl.CompanyServiceImpl
import xyz.yhsj.ktor.service.impl.UserServiceImpl

fun Application.configureKoin() {
    install(Koin) {
        modules(koinModule)
    }
}

val koinModule = module {
    single<CommonService> { CommonServiceImpl() }
    single<CompanyService> { CompanyServiceImpl() }
    single<UserService> { UserServiceImpl() }
}
