package xyz.yhsj.ktor.entity.user

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import xyz.yhsj.ktor.BaseDataBase
import xyz.yhsj.ktor.dao.mysql


import xyz.yhsj.ktor.validator.VG
import java.util.UUID

data class SysCompany(
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


interface Company : Entity<Company> {
    companion object : Entity.Factory<Company>()

    val id: Long
    var name: String?

    var user: User

}

object Companies : Table<Company>("sys_company") {

    val id = long("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
    val user = long("user_id").references(Users) { it.user }
}


val Database.companies get() = this.sequenceOf(Companies)