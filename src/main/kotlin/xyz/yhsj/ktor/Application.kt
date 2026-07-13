package xyz.yhsj.ktor

import io.ktor.server.application.Application
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import xyz.yhsj.ktor.api.configureApi
import xyz.yhsj.ktor.common.util.logger
import xyz.yhsj.ktor.base.cache.configureRedis
import xyz.yhsj.ktor.base.plugins.configureFlyway
import xyz.yhsj.ktor.base.plugins.configureHTTP
import xyz.yhsj.ktor.base.plugins.configureInit
import xyz.yhsj.ktor.base.plugins.configureIntercept
import xyz.yhsj.ktor.base.plugins.configureKoin
import xyz.yhsj.ktor.base.plugins.configureMonitoring
import xyz.yhsj.ktor.base.plugins.configurePermission
import xyz.yhsj.ktor.base.plugins.configureRouting
import xyz.yhsj.ktor.base.plugins.configureSecurity
import xyz.yhsj.ktor.base.plugins.configureSerialization
import xyz.yhsj.ktor.base.plugins.configureSockets
import xyz.yhsj.ktor.base.plugins.configureTemplating
import xyz.yhsj.ktor.dao.database.configureDatabaseLifecycle


fun main(args: Array<String>) {

    val port = args.firstOrNull { it.startsWith("-port=") }?.replace("-port=", "")?.toInt()

    embeddedServer(Netty, port = port ?: 80, module = Application::module).start(wait = true)
}

fun Application.module() {
    configureDatabaseLifecycle()
    configureFlyway()
    configureInit({
        task("初始化缓存") {
            // 服务器启动后执行
        }
    })
    configureKoin()
    configureRedis()
    configureSockets()
    configureSerialization()
    configureHTTP()
    configureSecurity()
    configureIntercept()
    configurePermission()
//        configureAdministration()
    configureMonitoring()
    configureRouting()
    configureApi()
    configureTemplating()
}


//代码初始化
//fun main(args: Array<String>) {
//    val env = applicationEngineEnvironment {
//        module {
//            configureRouting()
//            configureSecurity()
//            configureHTTP()
//            configureKoin()
//            configureIntercept()
//            configureSerialization()
//            configureAdministration()
//            configureRedis()
//            configureLogging()
//        }
////这里可以配置监听多个端口，然后根据端口请求不同的接口
//        // Private API
//        connector {
//            host = "127.0.0.1"
//            port = 9090
//        }
//        // Public API
//        connector {
//            host = "0.0.0.0"
//            port = 8080
//        }
//
//        sslConnector(
//            keyStore = keyStore,
//            keyAlias = "mykey",
//            keyStorePassword = { "changeit".toCharArray() },
//            privateKeyPassword = { "changeit".toCharArray() }) {
//            port = 9091
//            keyStorePath = keyStoreFile.absoluteFile
//        }
//
//    }
//    embeddedServer(Netty, env).apply {
//        start(wait = true)
//    }
//}


//fun main(args: Array<String>): Unit = EngineMain.main(args)
//
//@Suppress("unused") // Referenced in application.conf
//@JvmOverloads
//fun Application.module(testing: Boolean = false) {
//    configureKoin()
//    configureRedis()
//    configureSerialization()
//    configureHTTP()
//    configureSecurity()
//    configureIntercept()
//    configureAdministration()
//    configureMonitoring()
//    configureRouting()
//}





