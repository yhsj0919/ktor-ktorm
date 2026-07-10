package xyz.yhsj.ktor.common.error

import com.google.gson.JsonSyntaxException
import com.zaxxer.hikari.pool.HikariPool
import io.ktor.http.*
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.ContentTransformationException
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import kotlinx.coroutines.CancellationException
import org.koin.core.error.NoDefinitionFoundException
import redis.clients.jedis.exceptions.JedisConnectionException
import xyz.yhsj.ktor.api.model.response.CommonResp
import xyz.yhsj.ktor.common.util.logger
import java.sql.SQLException

fun StatusPagesConfig.statusPage() {
    status(HttpStatusCode.NotFound) { call, _ ->
        logger.warn("请求路径不存在：{}", call.request.path())
        call.respond(HttpStatusCode.NotFound, CommonResp.notFound(msg = "path not found"))
    }

    status(HttpStatusCode.UnsupportedMediaType) { call, _ ->
        call.respond(HttpStatusCode.UnsupportedMediaType, CommonResp.error(msg = "unsupported media type"))
    }

    status(HttpStatusCode.InternalServerError) { call, _ ->
        call.respond(HttpStatusCode.InternalServerError, CommonResp.error(msg = "server error"))
    }

    exception<Throwable> { call, error ->
        if (error is CancellationException) {
            throw error
        }
        logger.error("请求处理失败", error)
        val (status, response) = when (error) {
            is BadRequestException,
            is JsonSyntaxException,
            is ContentTransformationException -> HttpStatusCode.BadRequest to CommonResp.error(msg = "请求参数错误")

            is AppException -> appExceptionResponse(error)
            is NullPointerException -> HttpStatusCode.InternalServerError to CommonResp.error(msg = "server error")
            is NoDefinitionFoundException -> HttpStatusCode.InternalServerError to CommonResp.error(msg = "dependency injection error")
            is JedisConnectionException -> HttpStatusCode.ServiceUnavailable to CommonResp.error(msg = "Redis connection error")
            is HikariPool.PoolInitializationException -> {
                if (error.message?.contains("Unknown database") == true) {
                    HttpStatusCode.InternalServerError to CommonResp.error(msg = "database not found")
                } else {
                    HttpStatusCode.InternalServerError to CommonResp.error(msg = "database error")
                }
            }
            is SQLException -> {
                val response = when {
                    error.message?.contains("database exists") == true -> CommonResp.error(msg = "database exists")
                    error.message?.contains("Unknown database") == true -> CommonResp.error(msg = "database not found")
                    error.message?.contains("doesn't exist") == true -> CommonResp.error(msg = "table not found")
                    else -> CommonResp.error(msg = "database error")
                }
                HttpStatusCode.InternalServerError to response
            }
            is NotImplementedError -> HttpStatusCode.NotImplemented to CommonResp.error(msg = "not implemented")
            else -> HttpStatusCode.InternalServerError to CommonResp.error(msg = "server error")
        }
        call.respond(status, response)
    }
}

private fun appExceptionResponse(error: AppException): Pair<HttpStatusCode, CommonResp> {
    val status = HttpStatusCode.fromValue(error.code).takeIf { it.value in 400..599 }
        ?: HttpStatusCode.InternalServerError
    return status to CommonResp.error(code = error.code, msg = error.message ?: "server error")
}
