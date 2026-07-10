package xyz.yhsj.ktor.auth.extension

import xyz.yhsj.ktor.auth.AppSession


/**
 * 获取数据库
 */
fun AppSession.db(): String {
    return "db_${this.user?.companyId}"
}


fun Long.db(): String {
    return "db_${this}"
}

fun String.db(): String {
    return "db_${this}"
}
