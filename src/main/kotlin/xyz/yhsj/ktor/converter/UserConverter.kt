package xyz.yhsj.ktor.converter

import com.alibaba.excel.converters.Converter
import com.alibaba.excel.converters.ReadConverterContext
import com.alibaba.excel.converters.WriteConverterContext
import com.alibaba.excel.metadata.data.WriteCellData
import xyz.yhsj.ktor.entity.user.SysUser

open class UserConverter : Converter<SysUser> {
    override fun convertToJavaData(context: ReadConverterContext<*>): SysUser {
        return SysUser(id = context.readCellData.numberValue.toLong())
    }

    override fun convertToExcelData(context: WriteConverterContext<SysUser>): WriteCellData<*> {
        return WriteCellData<Any>(context.value.nickName ?: "")
    }
}
