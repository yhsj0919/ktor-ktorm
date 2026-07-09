package xyz.yhsj.ktor.entity.user

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.long
import org.ktorm.schema.varchar
import xyz.yhsj.ktor.BaseDataBase
import xyz.yhsj.ktor.entity.company.Company
import xyz.yhsj.ktor.entity.computer.SysComputer
import xyz.yhsj.ktor.validator.VG

@JsonIgnoreProperties(ignoreUnknown = true)
data class SysUser(
    @field:NotNull(message = "ID can not be null", groups = [VG.Update::class, VG.Delete::class])
    val id: Long? = null,

    @field:NotBlank(message = "userName can not be blank", groups = [VG.Login::class, VG.Add::class, VG.Admin::class])
    @field:Pattern(
        regexp = "^[1][3456789]\\d{9}$",
        message = "userName must be a phone number",
        groups = [VG.Add::class, VG.Login::class]
    )
    var userName: String? = null,

    @field:NotNull(message = "companyId can not be null", groups = [VG.Admin::class])
    var companyId: Long? = null,

    @field:NotNull(message = "roleId can not be null", groups = [VG.Add::class])
    var roleId: Long? = null,

    @field:NotBlank(message = "nickName can not be blank", groups = [VG.Add::class])
    var nickName: String? = null,

    var firstSpell: String? = null,

    @Transient
    @field:NotBlank(message = "password can not be blank", groups = [VG.Login::class, VG.Add::class, VG.Admin::class])
    var password: String? = null,

    var oldPassword: String? = null,
    var type: Int? = 0,
    var note: String? = null,
    var computer: SysComputer? = null,
)

interface User : Entity<User> {
    companion object : Entity.Factory<User>()

    var id: Long?
    var userName: String?
    var companyId: Long?
    var company: Company?
    var roleId: Long?
    var nickName: String?
    var firstSpell: String?
    var password: String?
    var passwordId: Long?
    var type: Int?
    var note: String?
    var deleted: Int?
    var creator: User?
    var editor: User?
    var deleter: User?
    var createTime: Long?
    var editTime: Long?
    var deleteTime: Long?
}

open class Users(alias: String?) : Table<User>("sys_user", alias, schema = BaseDataBase) {
    companion object : Users(null)

    override fun aliased(alias: String) = Users(alias)

    val id = long("id").primaryKey().bindTo { it.id }
    val userName = varchar("user_name").bindTo { it.userName }
    val companyId = long("company_id").bindTo { it.companyId }
    val roleId = long("role_id").bindTo { it.roleId }
    val nickName = varchar("nick_name").bindTo { it.nickName }
    val firstSpell = varchar("firstSpell").bindTo { it.firstSpell }
    val type = int("type").bindTo { it.type }
    val password = long("password_id").bindTo { it.passwordId }
    val note = varchar("note").bindTo { it.note }
    val deleted = int("deleted").bindTo { it.deleted }
    val createTime = long("create_time").bindTo { it.createTime }
    val editTime = long("edit_time").bindTo { it.editTime }
    val deleteTime = long("delete_time").bindTo { it.deleteTime }
    val creator = long("creator_id")
    val editor = long("editor_id")
    val deleter = long("deleter_id")
}

val Database.users get() = this.sequenceOf(Users)
