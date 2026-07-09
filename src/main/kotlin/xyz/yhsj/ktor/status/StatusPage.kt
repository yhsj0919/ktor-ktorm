package xyz.yhsj.ktor.status

import com.google.gson.JsonSyntaxException
import com.zaxxer.hikari.pool.HikariPool
import io.ktor.http.*
import io.ktor.server.plugins.ContentTransformationException
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import org.koin.core.error.NoDefinitionFoundException
import redis.clients.jedis.exceptions.JedisConnectionException
import xyz.yhsj.ktor.entity.resp.CommonResp
import xyz.yhsj.ktor.error.AppException
import java.sql.SQLException

fun StatusPagesConfig.statusPage() {
    status(HttpStatusCode.NotFound) { call, _ ->
        println(call.request.path())
        call.respond(HttpStatusCode.OK, CommonResp.notFound(msg = "path not found"))
    }

    status(HttpStatusCode.UnsupportedMediaType) { call, _ ->
        call.respond(HttpStatusCode.OK, CommonResp.error(msg = "unsupported media type"))
    }

    status(HttpStatusCode.InternalServerError) { call, _ ->
        call.respond(HttpStatusCode.OK, CommonResp.error(msg = "server error"))
    }

    exception<Throwable> { call, error ->
        error.printStackTrace()
        call.respond(
            HttpStatusCode.OK, when (error) {
                is JsonSyntaxException -> CommonResp.error(msg = "invalid JSON: ${error.cause?.message}")
                is ContentTransformationException -> CommonResp.error(msg = "JSON conversion error")
                is NullPointerException -> CommonResp.error(msg = "null pointer")
                is NoDefinitionFoundException -> CommonResp.error(msg = "dependency injection error")
                is JedisConnectionException -> CommonResp.error(msg = "Redis connection error")
                is HikariPool.PoolInitializationException -> {
                    if (error.message?.contains("Unknown database") == true) {
                        CommonResp.error(msg = "database not found")
                    } else {
                        CommonResp.error(msg = "database error")
                    }
                }
                is AppException -> CommonResp.error(code = error.code, msg = error.message ?: "")
                is SQLException -> {
                    when {
                        error.message?.contains("database exists") == true -> CommonResp.error(msg = "database exists")
                        error.message?.contains("Unknown database") == true -> CommonResp.error(msg = "database not found")
                        error.message?.contains("doesn't exist") == true -> CommonResp.error(msg = "table not found")
                        else -> CommonResp.error(msg = "database error")
                    }
                }
                is NotImplementedError -> CommonResp.error(msg = "not implemented")
                else -> CommonResp.error(msg = "server error")
            }
        )
    }
}
