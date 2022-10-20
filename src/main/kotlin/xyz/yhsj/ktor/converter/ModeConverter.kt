package xyz.yhsj.ktor.converter

import com.alibaba.excel.converters.Converter
import com.alibaba.excel.converters.ReadConverterContext
import com.alibaba.excel.converters.WriteConverterContext
import com.alibaba.excel.metadata.data.WriteCellData


class ModeConverter : Converter<Int> {
    override fun convertToJavaData(context: ReadConverterContext<*>): Int {
        return context.readCellData.stringValue.toInt()
    }

    /**
     * 这里是写的时候会调用 不用管
     *
     * @return
     */
    override fun convertToExcelData(context: WriteConverterContext<Int>): WriteCellData<*> {
        val status = context.value

        return WriteCellData<Any>(
            when (status) {
                0 -> "生产"
                1 -> "销售"
                2 -> "进料"
                3 -> "单向计数"
                4 -> "双向计数"
                5 -> "生产打卡"
                666 -> "特殊打卡"
                30 -> "进厂"
                31 -> "出厂"
                else -> "未知"
            }
        )
    }

}