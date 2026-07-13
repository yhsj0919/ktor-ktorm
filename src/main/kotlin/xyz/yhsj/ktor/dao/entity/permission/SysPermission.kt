package xyz.yhsj.ktor.dao.entity.permission


import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import xyz.yhsj.ktor.common.validation.VG
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.*
import xyz.yhsj.ktor.base.config.BaseDataBase

@JsonIgnoreProperties(ignoreUnknown = true)
data class SysPermission(
    @field:NotNull(message = "ID不可为空", groups = [VG.Update::class, VG.Delete::class])

    var id: Long? = null,
    //父级
    var parent: Long? = null,

    @Transient
    var parentName: String? = null,

    @Transient
    var select: Boolean = false,

    @field: NotBlank(message = "名称不可为空", groups = [VG.Add::class])
    var name: String? = null,

    //类型，0，菜单，1，接口
    @field: NotNull(message = "类型不可为空", groups = [VG.Add::class])
    var type: Int? = null,

    @field: NotNull(message = "级别不可为空", groups = [VG.Add::class])
    var level: Int? = null,

    @field: NotNull(message = "权重不可为空", groups = [VG.Add::class])
    var weight: Int? = null,

    @field: NotBlank(message = "路径不可为空", groups = [VG.Add::class])
    var path: String? = null,

    var icon: Int? = 0xe867,

    var enable: Boolean? = true,

    @Transient
    var children: List<SysPermission>? = null,
    //备注
    var note: String? = null,
)

interface Permission : Entity<Permission> {
    companion object : Entity.Factory<Permission>()

    var id: Long?

    //父级
    var parent: Long?

    var parentName: String?

    var select: Boolean

    var name: String?

    //类型，0，菜单，1，接口
    var type: Int?

    //级别0系统，1普通
    var level: Int?
    //权重
    var weight: Int?

    var path: String?


    var enable: Int?

    var children: List<Permission?>?

    //备注
    var note: String?
}


object Permissions : Table<Permission>("sys_permission", schema = BaseDataBase) {
    val id = long("id").primaryKey().bindTo { it.id }
    val parent = long("parent_id").bindTo { it.parent }
    val name = varchar("name").bindTo { it.name }
    val type = int("type").bindTo { it.type }
    val level = int("level").bindTo { it.level }
    val weight = int("weight").bindTo { it.weight }
    val path = varchar("path").bindTo { it.path }
    val enable = int("enable").bindTo { it.enable }
    val note = varchar("note").bindTo { it.note }

}

val Database.permissions get() = this.sequenceOf(Permissions)
