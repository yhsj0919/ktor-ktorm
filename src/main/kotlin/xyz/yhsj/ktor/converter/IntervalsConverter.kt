package xyz.yhsj.ktor.converter

import com.alibaba.excel.converters.Converter
import com.alibaba.excel.converters.ReadConverterContext
import com.alibaba.excel.converters.WriteConverterContext
import com.alibaba.excel.metadata.data.WriteCellData


class IntervalsConverter : Converter<Long> {
    override fun convertToJavaData(context: ReadConverterContext<*>): Long {
        return context.readCellData.stringValue.toLong()
    }

    /**
     * 这里是写的时候会调用 不用管
     *
     * @return
     */
    override fun convertToExcelData(context: WriteConverterContext<Long>): WriteCellData<*> {
        val intervals: Long = context.value

        val minutes = intervals / (1000 * 60)

        val real = if (minutes > 60 * 24) {
            val d = minutes / (60 * 24);
            val d_m = minutes % (60 * 24);
            val h = d_m / 60;
            "${d}天${h}小时";
        } else if (minutes > 60) {
            val h = minutes / 60;
            val m = minutes % 60;
            "${h}小时${m}分钟";
        } else {
            "${minutes}分钟";
        }
        return WriteCellData<Any>(real)
    }

}