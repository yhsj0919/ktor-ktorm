package xyz.yhsj.ktor.plugins


import io.ktor.server.application.*
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import xyz.yhsj.ktor.service.CompanyService
import xyz.yhsj.ktor.service.UserService
import xyz.yhsj.ktor.service.impl.CompanyServiceImpl
import xyz.yhsj.ktor.service.impl.UserServiceImpl


fun Application.configureKoin() {
    //模块依赖注入
    install(Koin) {

        modules(koinModule)

    }
}


val koinModule = module {

    single<UserService> { UserServiceImpl() }
    single<CompanyService> { CompanyServiceImpl() }

}