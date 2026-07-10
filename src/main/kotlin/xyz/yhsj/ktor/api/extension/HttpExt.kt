package xyz.yhsj.ktor.api.extension

import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.content.OutgoingContent
import io.ktor.http.content.PartData
import io.ktor.http.content.URIFileContent
import io.ktor.http.content.forEachPart
import io.ktor.http.defaultForFileExtension
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.http.content.JarFileContent
import io.ktor.server.http.content.LocalFileContent
import io.ktor.server.http.content.resolveResource
import io.ktor.server.http.content.resourceClasspathResource
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.request.receiveMultipart
import io.ktor.server.request.receiveNullable
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.RoutingContext
import io.ktor.server.routing.route
import io.ktor.server.util.normalizePathComponents
import io.ktor.util.toMap
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.InternalAPI
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import xyz.yhsj.ktor.auth.AppSession
import xyz.yhsj.ktor.api.model.response.CommonResp
import xyz.yhsj.ktor.auth.extension.session
import xyz.yhsj.ktor.common.json.fromJson
import xyz.yhsj.ktor.common.json.json
import xyz.yhsj.ktor.common.util.new
import xyz.yhsj.ktor.infrastructure.config.imageRootPath
import xyz.yhsj.ktor.common.validation.ValidationUtils
import java.io.File
import java.io.InputStream
import java.net.URLEncoder
import java.util.ArrayList
import java.util.Date
import java.util.HashMap
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.reflect.full.isSubclassOf

/**
 * 成功扩展
 */
suspend fun ApplicationCall.success(block: suspend () -> Any?) {
    val data = block()
    if (data is Unit || data == null) {
        respond(HttpStatusCode.OK, CommonResp.success())
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
inline fun Route.getExt(
    path: String,

    crossinline body: suspend RoutingContext.(params: Map<String, String?>, session: AppSession) -> Any,
): Route {
    return route(path, HttpMethod.Get) {
        handle {
            val params = call.parameters.toMap().map { it.key to it.value.firstOrNull() }.toMap()
            call.success {
                //这里session不为空，前面有校验，需要session的校验通过才会来到这里，不需要session的不关注这个属性
                body(params, call.session())
            }
        }
    }
}


/**
 * get扩展
 * @validate 校验的数组，不传则不校验
 */
inline fun <reified R : Any> Route.getExt(
    path: String,
    vararg validate: Class<*>,
    crossinline body: suspend RoutingContext.(R, session: AppSession) -> Any,
): Route {
    return route(path, HttpMethod.Get) {
        handle {

            val params = call.parameters.toMap().map { it.key to it.value.firstOrNull() }.toMap()
            val data: R = if (params.containsKey("params")) {
                fromJson(params["params"].toString())
            } else {
                fromJson(params.json())
            }

            call.success {
                if (validate.isEmpty()) {
                    body(data, call.session())
                } else {
                    data.validated(*validate) { body(data, call.session()) }
                }
            }
        }
    }
}


/**
 * post扩展
 * @validate 校验的数组，不传则不校验
 */
inline fun <reified R : Any> Route.postExt(
    path: String,
    vararg validate: Class<*>,
    crossinline body: suspend RoutingContext.(R, session: AppSession) -> Any,
): Route {
    return route(path, HttpMethod.Post) {
        handle {
            //这里初始化一个空参数，
            val data: R = call.receiveJsonOrNull() ?: new()
            call.success {
                if (validate.isEmpty()) {
                    body(data, call.session())
                } else {
                    data.validated(*validate) { body(data, call.session()) }
                }
            }
        }
    }
}

/**
 * post空参数扩展
 */
inline fun Route.postExt(
    path: String,
    crossinline body: suspend RoutingContext.(session: AppSession) -> Any,
): Route {
    return route(path, HttpMethod.Post) {
        handle {
            call.success {
                body(call.session())
            }
        }
    }
}

/**
 * post扩展
 * @validate 校验的数组，不传则不校验
 */
inline fun <reified R : Any> Route.postFormExt(
    path: String,
    vararg validate: Class<*>,
    crossinline body: suspend RoutingContext.(R, session: AppSession) -> Any,
): Route {
    return route(path, HttpMethod.Post) {
        handle {
            val params = call.receiveParameters().toFormMap()
            val data: R = if (R::class.isSubclassOf(Map::class)) {
                params as R
            } else if (params.isEmpty()) {
                new()
            } else {
                fromJson(params.json())
            }
            call.success {
                if (validate.isEmpty()) {
                    body(data, call.session())
                } else {
                    data.validated(*validate) { body(data, call.session()) }
                }
            }
        }
    }
}


/**
 * post扩展
 * @validate 校验的数组，不传则不校验
 * files: Map<String, Any?>  包含PartData.FileItem  和 List<PartData.FileItem>
 */
inline fun <reified R : Any> Route.postFileExt(
    path: String,
    vararg validate: Class<*>,
    crossinline body: suspend RoutingContext.(R, files: ArrayList<PartFile>, session: AppSession) -> Any,
): Route {
    return route(path, HttpMethod.Post) {
        handle {
            //这里初始化一个空参数，
            val part = call.receiveMultiParts()
            val params = part.items
            val files = part.files

            try {
                val data: R = fromJson(params.json())
                call.success {
                    if (validate.isEmpty()) {
                        body(data, files, call.session())
                    } else {
                        data.validated(*validate) { body(data, files, call.session()) }
                    }
                }
            } finally {
                part.close()
            }
        }
    }
}


/**
 * postFile空参数扩展
 */
inline fun Route.postFileExt(
    path: String,
    crossinline body: suspend RoutingContext.(params: Map<String, Any?>, files: ArrayList<PartFile>, session: AppSession) -> Any,
): Route {
    return route(path, HttpMethod.Post) {
        handle {
            val part = call.receiveMultiParts()

            val params = part.items
            val files = part.files

            try {
                call.success {
                    body(params, files, call.session())
                }
            } finally {
                part.close()
            }
        }
    }
}


/**
 * 将普通表单参数转换为对象可反序列化的 Map。
 * 同名参数保留为列表，字段名以 [] 结尾时去掉后缀并始终保留列表。
 * 同时支持 user[name] 形式的一级嵌套字段。
 */
@PublishedApi
@Suppress("UNCHECKED_CAST")
internal fun Parameters.toFormMap(): Map<String, Any?> {
    val result = linkedMapOf<String, Any?>()
    entries().forEach { (rawName, values) ->
        val name = rawName.removeSuffix("[]")
        val value: Any? = if (rawName.endsWith("[]") || values.size > 1) {
            values
        } else {
            values.firstOrNull()
        }

        val start = name.indexOf('[')
        val end = name.indexOf(']', start + 1)
        if (start > 0 && end == name.length - 1) {
            val parentName = name.substring(0, start)
            val childName = name.substring(start + 1, end)
            val parent = (result[parentName] as? MutableMap<String, Any?>)
                ?: linkedMapOf<String, Any?>().also { result[parentName] = it }
            parent[childName] = value
        } else {
            result[name] = value
        }
    }
    return result
}

/**
 * 获取表单参数，支持表单数组的话，请使用 [] 结尾的 Key。
 */
suspend fun ApplicationCall.receiveMultiParts(): MyPartData {

    val multipartData = receiveMultipart()

    val items = hashMapOf<String, Any?>()
    val files = arrayListOf<PartFile>()
    try {
        multipartData.forEachPart { part ->
            var handedToFile = false
            try {
                when (part) {
                    is PartData.FormItem -> {
                        val name = part.name ?: ""
                        putMultipartValue(items, name, part.value)
                    }

                    is PartData.FileItem -> {
                        val fileName = part.originalFileName ?: "upload_${Date().time}"
                        files.add(PartFile(fileName, part.contentType, part.provider()) { part.release() })
                        handedToFile = true
                    }

                    else -> Unit
                }
            } finally {
                // 文件流交给 PartFile 后延迟释放，其余 Part 在本轮处理后立即释放。
                if (!handedToFile) part.release()
            }
        }
    } catch (cause: Throwable) {
        // 解析中途失败时，释放已经加入列表但尚未交给 Service 的文件流。
        files.forEach { file ->
            runCatching { file.close() }
        }
        throw cause
    }
    return MyPartData(items, files)
}

class PartFile(
    val originalFileName: String,
    val contentType: ContentType?,
    val stream: ByteReadChannel,
    private val release: suspend () -> Unit,
) {
    private val released = AtomicBoolean(false)

    suspend fun close() {
        if (released.compareAndSet(false, true)) {
            release()
        }
    }
}

class MyPartData(
    val items: HashMap<String, Any?>,
    val files: ArrayList<PartFile>,
) {
    suspend fun close() {
        var firstFailure: Throwable? = null
        files.forEach { file ->
            runCatching { file.close() }.onFailure { error ->
                if (firstFailure == null) firstFailure = error
            }
        }
        firstFailure?.let { throw it }
    }
}

/** 处理 multipart 文本字段，支持重复字段、[] 数组和一级嵌套字段。 */
private fun putMultipartValue(target: MutableMap<String, Any?>, rawName: String, value: String) {
    val isArray = rawName.endsWith("[]")
    val name = rawName.removeSuffix("[]")
    val start = name.indexOf('[')
    val end = name.indexOf(']', start + 1)

    if (start > 0 && end == name.length - 1) {
        val parentName = name.substring(0, start)
        val childName = name.substring(start + 1, end)
        @Suppress("UNCHECKED_CAST")
        val parent = (target[parentName] as? MutableMap<String, Any?>)
            ?: linkedMapOf<String, Any?>().also { target[parentName] = it }
        appendMultipartValue(parent, childName, value, isArray)
    } else {
        appendMultipartValue(target, name, value, isArray)
    }
}

private fun appendMultipartValue(
    target: MutableMap<String, Any?>,
    name: String,
    value: String,
    forceList: Boolean,
) {
    val old = target[name]
    target[name] = when {
        old == null && forceList -> arrayListOf(value)
        old == null -> value
        old is MutableList<*> -> {
            @Suppress("UNCHECKED_CAST")
            (old as MutableList<String>).also { it += value }
        }
        else -> arrayListOf(old.toString(), value)
    }
}


/**
 * 保存图片
 * 文件列表来自 Multipart 请求，保存后仍由调用方负责关闭文件流。
 *
 */
suspend fun ArrayList<PartFile>.saveFile(
    root: String = imageRootPath,
    folder: String,
    name: String? = null
): List<String> {
    //存图片
    val rootPath = File(root).canonicalFile.toPath()
    val rootFolder = File(root, folder).canonicalFile
    require(rootFolder.toPath().startsWith(rootPath)) { "文件目录不能超出根目录" }
    if (!rootFolder.exists() && !rootFolder.mkdirs()) {
        error("无法创建文件目录：${rootFolder.absolutePath}")
    }
    require(rootFolder.isDirectory) { "文件目录不可用：${rootFolder.absolutePath}" }

    val images = ArrayList<String>()
    var lastTimestamp = -1L
    this.forEachIndexed { index, file ->
        var currentTimestamp = Date().time

        if (currentTimestamp == lastTimestamp) {
            currentTimestamp++  // 如果时间戳相同，则手动调整
        }

        lastTimestamp = currentTimestamp
        val fileName = name ?: "image_${currentTimestamp}_$index.jpg"
        val targetFile = File(rootFolder, fileName).canonicalFile
        require(targetFile.toPath().startsWith(rootFolder.toPath())) { "文件名不能包含路径穿越字符" }
        images.add("$folder/$fileName")

        withContext(Dispatchers.IO) {
            targetFile.outputStream().use { output ->
                file.stream.toInputStream().use { input -> input.copyTo(output) }
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
fun RoutingContext.resources(dir: String, name: String): OutgoingContent? {
    return call.resolveResource(name, dir)
}

/**
 * 获取资源文件
 */
@OptIn(InternalAPI::class)
fun Application.resources(
    dir: String? = null,
    name: String,
): OutgoingContent? {
    if (name.endsWith("/") || name.endsWith("\\")) {
        return null
    }
    val normalizedPath = normalisedPath(dir, name)
    for (url in environment.classLoader.getResources(normalizedPath).asSequence()) {
        resourceClasspathResource(
            url,
            normalizedPath
        ) { ContentType.defaultForFileExtension(it.path.extension()) }?.let { content ->
            return content
        }
    }

    return null
}

/**
 * 格式化路径
 */
private fun String.extension(): String {
    val indexOfName = lastIndexOf('/').takeIf { it != -1 } ?: lastIndexOf('\\').takeIf { it != -1 } ?: 0
    val indexOfDot = indexOf('.', indexOfName)
    return if (indexOfDot >= 0) substring(indexOfDot) else ""
}

/**
 * 规范化路径
 */
private fun normalisedPath(resourcePackage: String?, path: String): String {
    val pathComponents = path.split('/', '\\')
    if (pathComponents.contains("..")) {
        throw BadRequestException("Relative path should not contain path traversing characters: $path")
    }
    return (resourcePackage.orEmpty().split('.', '/', '\\') + pathComponents)
        .normalizePathComponents()
        .joinToString("/")
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


/**
 * Json转换
 */
suspend inline fun <reified T : Any> ApplicationCall.receiveJsonOrNull(): T? {
    return receiveNullable<T>()
}
