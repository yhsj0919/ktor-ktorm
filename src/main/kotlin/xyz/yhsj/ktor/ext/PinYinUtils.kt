package xyz.yhsj.ktor.ext

import net.sourceforge.pinyin4j.PinyinHelper
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination

object PinYinUtils {
    private var format: HanyuPinyinOutputFormat? = null

    private var pinyin: Array<String>? = null

    init {
        format = HanyuPinyinOutputFormat()
        format!!.toneType = HanyuPinyinToneType.WITHOUT_TONE
        pinyin = null
    }

    //转换单个中文字符
    private fun getCharacterPinYin(c: Char): String? {
        try {
            pinyin = PinyinHelper.toHanyuPinyinStringArray(c, format)
        } catch (e: BadHanyuPinyinOutputFormatCombination) {
            e.printStackTrace()
        }

        // 如果c不是汉字，返回null
        return if (null == pinyin) {
            ""
        } else if (pinyin?.isEmpty() == true) {
            c.toString()
        } else {
            pinyin!![0]
        }

        // 多音字取第一个值
    }

    //转换一个字符串
    fun getPinYin(str: String, firstSpell: Boolean = false): String {
        val sb = StringBuilder()

        for (i in 0 until str.length) {
            val tmp = getCharacterPinYin(str[i])
            if (null == tmp) {
                // 如果str.charAt(i)不是汉字，则保持原样
                sb.append(str[i])
            } else {
                sb.append(
                    if (firstSpell) {
                        tmp[0]
                    } else {
                        tmp
                    }
                )
                //分词
                //if (i < str.length - 1 && null != getCharacterPinYin(str[i + 1])) {
                //sb.append("\'")
                //}
            }
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

