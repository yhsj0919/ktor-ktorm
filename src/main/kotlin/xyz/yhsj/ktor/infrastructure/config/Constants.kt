package xyz.yhsj.ktor.infrastructure.config

import xyz.yhsj.ktor.common.util.RuntimeEnv

const val DES_KEY: String = "KtorKtorm"
const val SESSION_TIMEOUT: Long = 60 * 60 * 24 * 30
const val JWT_KEY = "id"
const val BaseDataBase = "ydb3"

var MySqlHost = when {
    RuntimeEnv.isDev() -> "127.0.0.1:3306"
    RuntimeEnv.isDocker() -> "172.17.0.1:3306"
    RuntimeEnv.isWSL() -> "127.0.0.1:3306"
    RuntimeEnv.isWindows() -> "127.0.0.1:3306"
    RuntimeEnv.isLinux() -> "127.0.0.1:3306"
    else -> "127.0.0.1:3306"
}

const val MySqlUserName = "root"
const val MySqlPassWord = "123456"

var REDIS_HOST = when {
    RuntimeEnv.isDocker() -> "172.17.0.1:6379"
    else -> "127.0.0.1:6379"
}
const val REDIS_PASSWORD = "123456"

var imageRootPath = when {
    RuntimeEnv.isWindows() -> "D:/Image"
    else -> "./Image"
}
