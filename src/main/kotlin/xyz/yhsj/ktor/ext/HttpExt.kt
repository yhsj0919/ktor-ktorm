package xyz.yhsj.ktor.ext


import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import io.ktor.util.pipeline.*
import io.ktor.utils.io.jvm.javaio.*
import xyz.yhsj.ktor.auth.AppSession
import xyz.yhsj.ktor.entity.resp.CommonResp
import xyz.yhsj.ktor.validator.ValidationUtils
import java.io.File
import java.io.InputStream
import java.net.URLEncoder
import java.util.*

/**
 * 成功扩展
 */
suspend fun ApplicationCall.success(block: suspend () -> Any?) {
    val data = block()
    if (data is Unit || data == null) {
//        this.respond(HttpStatusCode.OK, CommonResp.empty())
    } else {
        this.respond(HttpStatusCode.OK, data)
    }
}


/**
 * 数据校验
 */
suspend fun <T : Any> T.validated(vararg groups: Class<*>, block: suspend (data: T) -> Any): Any {
    val result = ValidationUtils.validateEntity(this, *groups)
    return if (result.hasErrors) {
        CommonResp.error(msg = result.errorMsg?.values?.first() ?: "未知参数错误")
    } else {
        block(this)
    }
}

/**
 * 数据校验
 */
suspend fun <T : Any> List<T>.validated(vararg groups: Class<*>, block: suspend (data: List<T>) -> Any): Any {
    val result = ValidationUtils.validateList(this, *groups)
    return if (result.hasErrors) {
        CommonResp.error(msg = result.errorMsg?.values?.first() ?: "未知参数错误")
    } else {
        block(this)
    }
}


/**
 * get扩展
 */
@KtorDsl
@JvmName("getExt")
inline fun Route.getExt(
    path: String,
    crossinline body: suspend PipelineContext<Unit, ApplicationCall>.(params: Map<String, String?>, session: AppSession) -> Any
): Route {
    return route(path, HttpMethod.Get) {
        handle {
            val params = call.parameters.toMap().map { it.key to it.value.first() }.toMap()
            call.success {
                //这里session不为空，前面有校验，需要session的校验通过才会来到这里，不需要session的不关注这个属性
                body(params, call.session())
            }
        }
    }
}


/**
 * get扩展
 * @validatedGroups 校验的数组，不传则不校验
 */
@KtorDsl
@JvmName("getExt")
inline fun <reified R : Any> Route.getExt(
    path: String,
    vararg validatedGroups: Class<*>,
    crossinline body: suspend PipelineContext<Unit, ApplicationCall>.(R, session: AppSession) -> Any
): Route {
    return route(path, HttpMethod.Get) {
        handle {

            val params = call.parameters.toMap().map { it.key to it.value.first() }.toMap()
            val data: R = if (params.containsKey("params")) {
                fromJson(params["params"].toString())
            } else {
                fromJson(params.json())
            }

            call.success {
                data.validated(*validatedGroups) {
                    //这里session不为空，前面有校验，需要session的校验通过才会来到这里，不需要session的不关注这个属性
                    body(data, call.session())
                }
            }
        }
    }
}


/**
 * post扩展
 * @validatedGroups 校验的数组，不传则不校验
 */
@KtorDsl
@JvmName("postTyped")
inline fun <reified R : Any> Route.postExt(
    path: String,
    vararg validatedGroups: Class<*>,
    crossinline body: suspend PipelineContext<Unit, ApplicationCall>.(R, session: AppSession) -> Any
): Route {
    assert(validatedGroups.isNotEmpty()) { "校验不可为空！" }

    return route(path, HttpMethod.Post) {
        handle {
            //这里初始化一个空参数，
            val data: R = call.receiveNullable() ?: new()
            call.success {
                data.validated(*validatedGroups) {
                    //这里session不为空，前面有校验，需要session的校验通过才会来到这里，不需要session的不关注这个属性
                    body(data, call.session())
                }
            }
        }
    }
}

/**
 * post空参数扩展
 */
@KtorDsl
@JvmName("postTyped")
inline fun Route.postExt(
    path: String, crossinline body: suspend PipelineContext<Unit, ApplicationCall>.(session: AppSession) -> Any
): Route {
    return route(path, HttpMethod.Post) {
        handle {
            call.success {
                //这里session不为空，前面有校验，需要session的校验通过才会来到这里，不需要session的不关注这个属性
                body(call.session())
            }
        }
    }
}


/**
 * post扩展
 * @validatedGroups 校验的数组，不传则不校验
 * files: Map<String, Any?>  包含PartData.FileItem  和 List<PartData.FileItem>
 */
@KtorDsl
@JvmName("postTyped")
inline fun <reified R : Any> Route.postFileExt(
    path: String,
    vararg validatedGroups: Class<*>,
    crossinline body: suspend PipelineContext<Unit, ApplicationCall>.(R, files: Map<String, Any?>, session: AppSession) -> Any
): Route {
    return route(path, HttpMethod.Post) {
        handle {
            //这里初始化一个空参数，
            val part = call.receiveMultiParts()
            val files = part.convertFile()
            val params = part.convertItem()

            val data: R = fromJson(params.json())
            call.success {
                data.validated(*validatedGroups) {
                    //这里session不为空，前面有校验，需要session的校验通过才会来到这里，不需要session的不关注这个属性
                    body(data, files, call.session())
                }
            }
        }
    }
}


/**
 * postFile空参数扩展
 */
@KtorDsl
@JvmName("postTyped")
inline fun Route.postFileExt(
    path: String,
    crossinline body: suspend PipelineContext<Unit, ApplicationCall>.(params: Map<String, Any?>, files: Map<String, Any?>, session: AppSession) -> Any
): Route {
    return route(path, HttpMethod.Post) {
        handle {
            val part = call.receiveMultiParts()
            val files = part.convertFile()
            val params = part.convertItem()

            call.success {
                //这里session不为空，前面有校验，需要session的校验通过才会来到这里，不需要session的不关注这个属性
                body(params, files,call.session())
            }
        }
    }
}


/**
 * 获取表单参数，支持表单数组的话，请使用[]结尾的Key
 */
suspend fun ApplicationCall.receiveMultiParts(): Map<String, Any?> = try {
    val part = receiveMultipart().readAllParts()

    part.groupBy { it.name ?: "" }.map {
        if (it.key.endsWith("[]")) {
            it.key.removeSuffix("[]") to it.value
        } else {
            it.key to it.value.firstOrNull()
        }
    }.toMap()
} catch (th: Throwable) {
    th.printStackTrace()
    mapOf()
}

/**
 * 转换成文件
 * 表单提取文件参数
 */

fun Map<String, Any?>.convertFile(): Map<String, Any?> {
    val file = this.filter { it.value is PartData.FileItem }.map { it.key to it.value as PartData.FileItem? }.toMap()

    val files = this.filter { it.value is List<*> }.filter {
        (it.value as List<*>).filterIsInstance<PartData.FileItem>().isNotEmpty()
    }

    val params = hashMapOf<String, Any?>()
    params.putAll(file)
    params.putAll(files)
    return params
}

/**
 * 转换成参数
 * 表单提取文本参数
 */
fun Map<String, Any?>.convertItem(): Map<String, Any?> {
    val form =
        this.filter { it.value is PartData.FormItem }.map { it.key to (it.value as PartData.FormItem?)?.value }.toMap()

    val forms = this.filter { it.value is List<*> }.filter {
        (it.value as List<*>).filterIsInstance<PartData.FormItem>().isNotEmpty()
    }.map { it.key to (it.value as List<PartData.FormItem?>).map { item -> item?.value }.toList() }.toMap()

    val params = hashMapOf<String, Any?>()
    params.putAll(form)
    params.putAll(forms)
    return params
}

/**
 * 保存图片
 * 请保证map里只有PartData.FileItem和List<PartData.FileItem>
 *
 */
fun Map<String, Any?>.saveFile(root: String, folder: String): List<String> {
    //存图片
    val rootFolder = File("$root/${folder}")
    if (!rootFolder.exists()) {
        rootFolder.mkdir()
    }

    val images = ArrayList<String>()

    this.forEach { (_, file) ->
        if (file != null) {
            if (file is PartData.FileItem) {
//                println(">>>>>>>>>${file.originalFileName}>>>>>>>>>")
                val fileName = file.originalFileName
                val fileBytes = file.streamProvider().readBytes()

                images.add("$folder/$fileName")

                File("${rootFolder.path}/$fileName").writeBytes(fileBytes)
            }
            if (file is List<*>) {
                file.map { it as PartData.FileItem }.forEach { data ->
//                    println(">>>>>>>>>${data.originalFileName}>>>>>>>>>")
                    val fileName = data.originalFileName
                    val fileBytes = data.streamProvider().readBytes()
                    images.add("$folder/$fileName")
                    File("${rootFolder.path}/$fileName").writeBytes(fileBytes)
                }
            }
        }
    }

    return images
}

/**
 * 文件下载名
 */
fun String.downloadName(): String {
    val fileName = URLEncoder.encode(this, "UTF-8")
    return "attachment;filename=$fileName"
}

/**
 * 获取资源文件
 */
fun PipelineContext<Unit, ApplicationCall>.resources(dir: String, name: String): OutgoingContent? {
    return call.resolveResource(name, dir)
}

/**
 * 根据OutgoingContent获取输入流
 */
fun OutgoingContent.inputStream(): InputStream? {
    return when (this) {
        is LocalFileContent -> {
            readFrom().toInputStream()
        }

        is JarFileContent -> {
            readFrom().toInputStream()
        }

        is URIFileContent -> {
            readFrom().toInputStream()
        }

        else -> {
            null
        }
    }
}