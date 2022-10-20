package xyz.yhsj.ktor.converter

import com.alibaba.excel.converters.Converter
import com.alibaba.excel.converters.ReadConverterContext
import com.alibaba.excel.converters.WriteConverterContext
import com.alibaba.excel.metadata.data.WriteCellData



class StatusConverter : Converter<Int> {
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
                0 -> "未完成"
                1 -> "已完成"
                9 -> "空车出厂"
                666 -> "已称重"
                else -> "异常"
            }
        )
    }

}