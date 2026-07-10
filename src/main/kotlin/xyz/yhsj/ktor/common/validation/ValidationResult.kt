package xyz.yhsj.ktor.common.validation

import xyz.yhsj.ktor.common.json.json
import java.io.Serializable

/**
 * 校验结果
 */
data class ValidationResult(

    //校验结果是否有错
    var hasErrors: Boolean = false,
    //校验错误信息
    var errorMsg: Map<String, String>? = null
) : Serializable {
    override fun toString() = this.json()
}
