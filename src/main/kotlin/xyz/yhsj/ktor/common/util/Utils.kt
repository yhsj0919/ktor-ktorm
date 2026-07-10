package xyz.yhsj.ktor.common.util

import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.*
import kotlin.random.Random

object Utils {
    /**
     * 获取订单号
     * @param prefix 前缀
     */
    fun getOrderNo(prefix: String, company: Long): String {

        return prefix + "$company".padStart(4, '0') +
                SimpleDateFormat("yyMMddHHmmss").format(Date()) +
                "${Random.nextInt(10, 99)}"

    }

    fun isAfterTen(): Boolean {
        val now = LocalTime.now()
        val ten = LocalTime.of(10, 0) // 当天 10:00
        return now.isAfter(ten)
    }

    fun getDayTimestamp(before: Boolean = false): Long {
        val calendar = Calendar.getInstance().apply {
            // 如果需要前一天，就先往前一天
            if (before) {
                add(Calendar.DAY_OF_MONTH, -1)
            }
            // 归零到 0 点
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return calendar.timeInMillis
    }
}

fun Long.getDayTimestamp(before: Boolean = false): Long {

    val cal = Calendar.getInstance()
    cal.timeInMillis = this
    // 如果需要前一天，就先往前一天
    if (before) {
        cal.add(Calendar.DAY_OF_MONTH, -1)
    }
    // 归零到 0 点
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)

    return cal.timeInMillis
}

fun Long.getMonthRange(): Pair<Long, Long> {
    val cal = Calendar.getInstance()
    cal.timeInMillis = this

    // 设置为当月第一天 00:00:00
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val firstMillis = cal.timeInMillis

    // 设置为当月最后一天 23:59:59.999
    cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
    cal.set(Calendar.HOUR_OF_DAY, 23)
    cal.set(Calendar.MINUTE, 59)
    cal.set(Calendar.SECOND, 59)
    cal.set(Calendar.MILLISECOND, 999)
    val lastMillis = cal.timeInMillis

    return firstMillis to lastMillis
}
fun Long.getYearRange(): Pair<Long, Long> {
    val cal = Calendar.getInstance()
    cal.timeInMillis = this

    // 设置为当年第一天 00:00:00.000
    cal.set(Calendar.MONTH, Calendar.JANUARY)
    cal.set(Calendar.DAY_OF_MONTH, 1)
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val firstMillis = cal.timeInMillis

    // 设置为当年最后一天 23:59:59.999
    cal.set(Calendar.MONTH, Calendar.DECEMBER)
    cal.set(Calendar.DAY_OF_MONTH, 31)
    cal.set(Calendar.HOUR_OF_DAY, 23)
    cal.set(Calendar.MINUTE, 59)
    cal.set(Calendar.SECOND, 59)
    cal.set(Calendar.MILLISECOND, 999)
    val lastMillis = cal.timeInMillis

    return firstMillis to lastMillis
}


fun BigDecimal?.plus(other: BigDecimal?): BigDecimal {
    return (this ?: 0.0.toBigDecimal()).add(other ?: 0.0.toBigDecimal())
}

/**
 * Enables the use of the `-` operator for [BigDecimal] instances.
 */
fun BigDecimal?.minus(other: BigDecimal?): BigDecimal {
    return (this ?: 0.0.toBigDecimal()).subtract(other ?: 0.0.toBigDecimal())
}

fun BigDecimal?.abs(): BigDecimal {
    return (this ?: 0.0.toBigDecimal()).abs()
}

/**
 * 获取首字母
 */
fun String.pingYin(firstSpell: Boolean = false): String {
    return PinYinUtils.getPinYin(this, true)
}

/**
 * 将参数字符串，转为map
 */
fun String.paramsToMap(): Map<String, String> {
    return this.split("&").associate {
        val ss = it.split("=")
        Pair(ss[0], ss[1])
    }
}
