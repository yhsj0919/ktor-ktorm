package xyz.yhsj.ktor.persistence.entity.company

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.validation.constraints.NotNull
import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import xyz.yhsj.ktor.infrastructure.config.BaseDataBase
import xyz.yhsj.ktor.persistence.entity.permission.Permission
import xyz.yhsj.ktor.persistence.entity.permission.Permissions
import xyz.yhsj.ktor.common.validation.VG

/**
 * 公司权限参数
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class SysCompanyPermission(
    @field:NotNull(message = "ID不可为空", groups = [VG.Update::class, VG.Delete::class])
    val id: Long? = null,
    //公司Id
    @field:NotNull(message = "公司ID不可为空", groups = [VG.Add::class])
    var companyId: Long? = null,

    var permission: List<Long>? = null,
    //备注
    var note: String? = null,
)


/**
 * 公司权限
 */
interface CompanyPermission : Entity<CompanyPermission> {
    companion object : Entity.Factory<CompanyPermission>()

    val id: Long?

    //公司Id
    var companyId: Long?

    //权限
    var permission: Permission?

    //备注
    var note: String?
}

/**
 * 公司权限
 */
object CompanyPermissions : Table<CompanyPermission>("sys_company_permission", schema = BaseDataBase) {
    val id = long("id").primaryKey().bindTo { it.id }

    val companyId = long("company_id").bindTo { it.companyId }
    val permission = long("permission_id").references(Permissions) { it.permission }

    val note = varchar("note").bindTo { it.note }

}


val Database.companyPermissions get() = this.sequenceOf(CompanyPermissions)

