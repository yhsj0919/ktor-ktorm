package xyz.yhsj.ktor.auth.extension

fun Long.db(): String {
    return "db_${this}"
}

fun String.db(): String {
    return "db_${this}"
}
