package xyz.yhsj.ktor.api.model.request

import com.alibaba.excel.annotation.ExcelProperty
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import xyz.yhsj.ktor.persistence.entity.user.SysUser
import xyz.yhsj.ktor.persistence.entity.user.User

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
    var creator: SysUser? = null,

    //修改人

    var editor: SysUser? = null,

    //删除人
    var deleter: SysUser? = null,

    //创建时间
    var createTime: Long? = null,

    //修改时间
    var editTime: Long? = null,

    //删除时间
    var deleteTime: Long? = null,
)
