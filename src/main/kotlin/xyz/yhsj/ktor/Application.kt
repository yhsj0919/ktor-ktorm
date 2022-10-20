package xyz.yhsj.ktor

import io.ktor.server.application.*
import io.ktor.server.netty.*
import xyz.yhsj.ktor.plugins.*
import io.ktor.server.engine.*


fun main() {
    embeddedServer(Netty, port = 8081, host = "0.0.0.0") {
        configureKoin()
        configureRedis()
        configureSockets()
        configureSerialization()
        configureHTTP()
        configureSecurity()
        configureIntercept()
//        configureAdministration()
        configureMonitoring()
        configureRouting()
    }.start(wait = true)
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





