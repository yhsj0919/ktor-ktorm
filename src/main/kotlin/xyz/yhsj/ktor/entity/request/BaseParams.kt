package xyz.yhsj.ktor.entity.request

import com.alibaba.excel.annotation.ExcelProperty
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import xyz.yhsj.ktor.converter.TimeConverter
import xyz.yhsj.ktor.converter.UserConverter
import xyz.yhsj.ktor.entity.user.SysUser
import xyz.yhsj.ktor.entity.user.User

/**
 * 基础参数
 */
@JsonIgnoreProperties(ignoreUnknown = true)
open class BaseParams(
    var page: Int = 0,
    var size: Int = 20,
    //是否删除
    var deleted: Int? = null,

    //创建人
    @ExcelProperty(converter = UserConverter::class)
    var creator: SysUser? = null,

    //修改人
    @ExcelProperty(converter = UserConverter::class)
    var editor: SysUser? = null,

    //删除人
    @ExcelProperty(converter = UserConverter::class)
    var deleter: SysUser? = null,

    //创建时间
    @ExcelProperty(converter = TimeConverter::class)
    var createTime: Long? = null,

    //修改时间
    @ExcelProperty(converter = TimeConverter::class)
    var editTime: Long? = null,

    //删除时间
    @ExcelProperty(converter = TimeConverter::class)
    var deleteTime: Long? = null,
)
