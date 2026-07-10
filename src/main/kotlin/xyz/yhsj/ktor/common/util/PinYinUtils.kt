package xyz.yhsj.ktor.common.util

import net.sourceforge.pinyin4j.PinyinHelper
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination

object PinYinUtils {
    private val format = HanyuPinyinOutputFormat().apply {
        toneType = HanyuPinyinToneType.WITHOUT_TONE
    }

    //转换单个中文字符
    private fun getCharacterPinYin(c: Char): String {
        val pinyin = try {
            PinyinHelper.toHanyuPinyinStringArray(c, format)
        } catch (error: BadHanyuPinyinOutputFormatCombination) {
            logger.error("拼音转换失败，字符：{}", c, error)
            null
        }

        return pinyin?.firstOrNull() ?: c.toString()
    }

    //转换一个字符串
    fun getPinYin(str: String, firstSpell: Boolean = false): String {
        val sb = StringBuilder()

        for (i in 0 until str.length) {
            val tmp = getCharacterPinYin(str[i])
            sb.append(
                if (firstSpell) {
                    tmp.first()
                } else {
                    tmp
                }
            )
        }
        return sb.toString().trim { it <= ' ' }
    }

}

//fun main(args: Array<String>) {
//    val str = "司马fas;;56懿"
//    val pinYin = PinYinUtils.getPinYin(str, true)
//
//    println(pinYin)
//}

