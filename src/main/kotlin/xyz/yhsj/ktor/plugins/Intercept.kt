package xyz.yhsj.ktor.plugins

import io.ktor.server.application.*
import io.ktor.server.request.*
import xyz.yhsj.ktor.ext.logger

/**
 * 拦截器
 */
fun Application.configureIntercept() {
    //拦截器
    intercept(ApplicationCallPipeline.Call) {
        logger.info("host:" + call.request.host() + ":" + call.request.port() + call.request.path())
    }

    //TODO 以后配置防止重复点击
    //记录每个session的请求连接时间,连续两次的时间放在redis做个对比,小于某个时间拦截
}

