package xyz.yhsj.ktor.excelEntity

import com.alibaba.excel.annotation.ExcelProperty
import com.alibaba.excel.annotation.write.style.ColumnWidth

data class TestExcel(
    @ExcelProperty("ID")
    @ColumnWidth(10)
    val id: String? = null,

    @ExcelProperty("用户名")
    @ColumnWidth(20)
    val name: String? = null,

    @ExcelProperty("数字")
    @ColumnWidth(20)
    val number: Double? = null,
)
