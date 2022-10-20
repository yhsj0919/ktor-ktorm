package xyz.yhsj.ktor.ext

import io.ktor.http.*
import io.ktor.server.http.content.*
import java.nio.file.Paths
import kotlin.io.path.pathString

fun getResources(path: String): String {


    val projectDirAbsolutePath = Paths.get("").toAbsolutePath().toString()
    val resourcesPath = Paths.get(projectDirAbsolutePath, "/src/main/resources/$path")
//    val paths = Files.walk(resourcesPath)
//        .filter { item -> Files.isRegularFile(item) }
//        .filter { item -> item.toString().endsWith(".ico") }
//        .forEach { item -> println("filename: $item") }

//    resourceClasspathResource("","") { ContentType.defaultForFileExtension(it) }
    return resourcesPath.pathString
}