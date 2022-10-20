package xyz.yhsj.ktor.status

import com.google.gson.JsonSyntaxException
import com.zaxxer.hikari.pool.HikariPool
import io.ktor.http.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.ContentTransformationException
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.util.*
import org.koin.core.error.NoBeanDefFoundException
import redis.clients.jedis.exceptions.JedisConnectionException
import xyz.yhsj.ktor.entity.resp.CommonResp
import java.io.FileNotFoundException
import java.sql.SQLException
import java.sql.SQLNonTransientException
import java.sql.SQLSyntaxErrorException

fun StatusPagesConfig.statusPage() {

    status(HttpStatusCode.NotFound) { call, _ ->
        println(call.request.path())
        call.respond(HttpStatusCode.OK, CommonResp.notFound(msg = "路径不存在"))
    }
    status(HttpStatusCode.UnsupportedMediaType) { call, _ ->
        call.respond(HttpStatusCode.OK, CommonResp.error(msg = "不支持的媒体类型"))
    }

    status(HttpStatusCode.UnsupportedMediaType) { call, _ ->
        call.respond(HttpStatusCode.OK, CommonResp.error(msg = "不支持的媒体类型"))
    }

    status(HttpStatusCode.InternalServerError) { call, error ->
        call.respond(HttpStatusCode.OK, CommonResp.error(msg = "服务器异常"))
    }
    exception<Exception> { call, error ->

        call.respond(
            HttpStatusCode.OK, when (error) {


                is JsonSyntaxException -> {
                    CommonResp.error(msg = "JSON数据格式错误:${error.cause?.message}")
                }

                is ContentTransformationException -> {
                    CommonResp.error(msg = "JSON数据转换错误")
                }

                is NullPointerException -> {
                    CommonResp.error(msg = "服务器空指针异常")
                }

                is NoBeanDefFoundException -> {
                    CommonResp.error(msg = "依赖注入异常")
                }

                is JedisConnectionException -> {
                    error.printStackTrace()

                    CommonResp.error(msg = "Redis连接异常")
                }

//                is SQLSyntaxErrorException -> {
//                    error.printStackTrace()
//                    if (error.message?.contains("Unknown database") == true)
//                        CommonResp.error(msg = "数据库不存在")
//                    else
//                        CommonResp.error(msg = "数据库异常")
//                }
//
//                is SQLNonTransientException -> {
//                    error.printStackTrace()
//                    CommonResp.error(msg = "数据库异常")
//                }
                is HikariPool.PoolInitializationException ->{
                    error.printStackTrace()
                    if (error.message?.contains("Unknown database") == true)
                        CommonResp.error(msg = "数据库不存在")
                    else
                        CommonResp.error(msg = "数据库异常")
                }

                is SQLException -> {
                    error.printStackTrace()
                    println(error.message)
                    if (error.message?.contains("database exists") == true)
                        CommonResp.error(msg = "数据库已经存在")
                    else if (error.message?.contains("Unknown database") == true)
                        CommonResp.error(msg = "数据库不存在")
                    else
                        CommonResp.error(msg = "数据库异常")
                }

                else -> {
                    error.printStackTrace()
                    CommonResp.error(msg = "服务器异常")
                }
            }
        )
    }
}