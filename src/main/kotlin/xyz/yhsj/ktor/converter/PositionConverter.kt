package xyz.yhsj.ktor.converter

import com.alibaba.excel.converters.Converter
import com.alibaba.excel.converters.ReadConverterContext
import com.alibaba.excel.converters.WriteConverterContext
import com.alibaba.excel.metadata.data.WriteCellData



class PositionConverter : Converter<List<String>> {
    override fun convertToJavaData(context: ReadConverterContext<*>): List<String> {
        return context.readCellData.stringValue.split("|")
    }

    /**
     * 这里是写的时候会调用 不用管
     *
     * @return
     */
    override fun convertToExcelData(context: WriteConverterContext<List<String>>): WriteCellData<*> {
        val position = context.value
        return WriteCellData<Any>(position.joinToString("|"))
    }

}