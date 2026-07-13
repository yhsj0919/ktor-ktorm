package xyz.yhsj.ktor.api.model.request

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

/**
 * 基础参数
 */
@JsonIgnoreProperties(ignoreUnknown = true)
open class BaseParams(
    var page: Int = 0,
    var size: Int = 20,
    //是否删除
    var deleted: Int? = null,

    //创建时间
    var createTime: Long? = null,

    //修改时间
    var editTime: Long? = null,

    //删除时间
    var deleteTime: Long? = null,
)
