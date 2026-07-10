package xyz.yhsj.ktor.infrastructure.plugins

import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationStarted
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.install
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import xyz.yhsj.ktor.common.util.logger

internal data class InitTask(
    val name: String,
    val block: suspend (Application) -> Unit,
)

class InitConfig {
    private val tasks = mutableListOf<InitTask>()

    /** 注册一个应用启动任务。任务将在 Ktor 报告应用启动后执行。 */
    fun task(name: String, block: suspend Application.() -> Unit) {
        require(name.isNotBlank()) { "初始化任务名称不能为空" }
        tasks += InitTask(name) { application -> block(application) }
    }

    internal fun tasks(): List<InitTask> = tasks.toList()
}

val InitPlugin = createApplicationPlugin("Init", ::InitConfig) {
    application.monitor.subscribe(ApplicationStarted) {
        application.launch {
            pluginConfig.tasks().forEach { task ->
                try {
                    task.block(application)
                    logger.info("初始化任务执行完成：{}", task.name)
                } catch (error: CancellationException) {
                    throw error
                } catch (error: Throwable) {
                    logger.error("初始化任务执行失败：{}", task.name, error)
                }
            }
        }
    }
}

/** 安装应用启动初始化插件。 */
fun Application.configureInit(configure: InitConfig.() -> Unit = {}) {
    install(InitPlugin, configure)
}
