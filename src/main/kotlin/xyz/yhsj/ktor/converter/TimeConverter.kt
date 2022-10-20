package xyz.yhsj.ktor.converter

import com.alibaba.excel.converters.Converter
import com.alibaba.excel.converters.ReadConverterContext
import com.alibaba.excel.converters.WriteConverterContext
import com.alibaba.excel.metadata.data.WriteCellData
import java.text.SimpleDateFormat


class TimeConverter : Converter<Long> {
    override fun convertToJavaData(context: ReadConverterContext<*>): Long {
        return context.readCellData.stringValue.toLong()
    }

    /**
     * 这里是写的时候会调用 不用管
     *
     * @return
     */
    override fun convertToExcelData(context: WriteConverterContext<Long>): WriteCellData<*> {


        return WriteCellData<Any>(SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(context.value))
    }

}