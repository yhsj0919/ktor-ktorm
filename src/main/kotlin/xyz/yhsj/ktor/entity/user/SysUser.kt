package xyz.yhsj.ktor.entity.user

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import xyz.yhsj.ktor.BaseDataBase


import xyz.yhsj.ktor.validator.VG
import java.util.UUID

data class SysUser(
    @field:NotBlank(message = "ID不可为空", groups = [VG.Update::class, VG.Delete::class])
    val id: Int? = null,
    @field: NotBlank(message = "用户名不可为空", groups = [VG.Login::class, VG.Add::class, VG.Admin::class])
    @field:Pattern(
        regexp = "^[1][3456789]\\d{9}\$",
        message = "账号只能为手机号",
        groups = [VG.Add::class, VG.Login::class]
    )
    var userName: String? = null,

    //公司Id
    @field:NotNull(message = "公司ID不可为空", groups = [VG.Admin::class])
    var companyId: String? = null,

    @field:NotNull(message = "角色ID不可为空", groups = [VG.Add::class])
    var roleId: String? = null,

    //昵称
    @field: NotBlank(message = "昵称不可为空", groups = [VG.Add::class])
    var nickName: String? = null,
    //Gson序列化反序列化忽略，
    //@Expose(serialize = true, deserialize = true)
    @Transient
    @field: NotBlank(message = "密码不可为空", groups = [VG.Login::class, VG.Add::class, VG.Admin::class])
    var passWord: String? = null,
    //-1系统管理员，0普通人员
    var type: Int? = 0,
    //备注
    var note: String? = null,
)


interface User : Entity<User> {
    companion object : Entity.Factory<User>()

    var id: Long
    var userName: String?

    //公司Id
    var companyId: String?
    var roleId: String?

    //昵称
    var nickName: String?

    //密码
    var passWord: String?

    //-1系统管理员，0普通人员
    var type: Int?

    //备注
    var note: String?
}

object Users : Table<User>("sys_user", schema = BaseDataBase) {

    val id = long("id").primaryKey().bindTo { it.id }
    val userName = varchar("user_name").bindTo { it.userName }
    val companyId = varchar("company_id").bindTo { it.companyId }
    val roleId = varchar("role_id").bindTo { it.roleId }
    val nickName = varchar("nick_name").bindTo { it.nickName }
    val passWord = varchar("pass_word").bindTo { it.passWord }
    val type = int("type").bindTo { it.type }
    val note = varchar("note").bindTo { it.note }

}

val Database.users get() = this.sequenceOf(Users)