package xyz.yhsj.ktor.auth


import io.ktor.server.sessions.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import xyz.yhsj.ktor.ext.logger
import java.io.File
import java.util.*
import kotlin.concurrent.timer

val scope = MainScope()

fun appSessionStorage(rootDir: File, cached: Boolean = true, timeOut: Long = 60000): SessionStorage {

    val storage = directorySessionStorage(rootDir, cached)
    if (timeOut > 0) {
        logger.debug("尝试开启携程清理session文件")
        scope.launch(Dispatchers.IO) {
            //测试循环任务
            timer(startAt = Date(), period = 10 * 1000, action = {
                //尝试清理Session
                getSession(rootDir.path)
                    .filter {
                        Date().time - it.lastModified > timeOut
                    }
                    .forEach {
                        scope.launch(Dispatchers.IO) {
                            logger.debug("清理Session:${it.id}")
                            storage.invalidate(it.id)
                        }
                    }
            })

        }
    }
    return storage
}

fun getSession(directory: String): List<SessionInfo> {
    val fileTree: FileTreeWalk = File(directory).walk()
    return fileTree
        .filter { it.isFile }
        .filter { it.extension == "dat" }
        .map {
            SessionInfo(
                lastModified = it.lastModified(),
                id = it.path.replace(directory, "").replace("\\", "").replace(".dat", "")
            )
        }
        .toList()
}

data class SessionInfo(var lastModified: Long = 0, var id: String)

