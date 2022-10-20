package xyz.yhsj.ktor.ext

import java.util.*

/**
 * 一天0：0：0
 */
fun getDayOfFirstTime(): Long {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MILLISECOND, 0)

    return calendar.timeInMillis
}

fun getDayOfLastTime(): Long {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MILLISECOND, 0)

    return calendar.timeInMillis + 24 * 60 * 60 * 1000 - 1

}