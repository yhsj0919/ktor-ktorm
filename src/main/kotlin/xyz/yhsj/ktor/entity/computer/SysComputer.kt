package xyz.yhsj.ktor.entity.computer

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull
import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.double
import org.ktorm.schema.int
import org.ktorm.schema.long
import org.ktorm.schema.varchar
import xyz.yhsj.ktor.entity.common.BaseEntity
import xyz.yhsj.ktor.entity.common.BaseTable
import xyz.yhsj.ktor.entity.company.Companies
import xyz.yhsj.ktor.entity.company.Company
import xyz.yhsj.ktor.entity.company.SysCompany
import xyz.yhsj.ktor.entity.request.BaseParams


import xyz.yhsj.ktor.validator.VG

@JsonIgnoreProperties(ignoreUnknown = true)
data class SysComputer(
    @field:NotNull(
        message = "ID不可为空", groups = [VG.Delete::class, VG.Update::class]
    ) val id: Long? = null,

    //设备
    @field:NotEmpty(message = "设备Id不可为空", groups = [VG.Add::class, VG.Admin::class])
    var deviceId: String? = null,

    //版本
    var version: String? = null,

    //名称
    var name: String? = null,

    //是否注册
    var register: Int? = null,

    //公司
    var company: SysCompany? = null,

    var lastTime: Long? = null,

    var latitude: Double? = null,
    var longitude: Double? = null,

    //备注
    var note: String? = null,

    ) : BaseParams()

/**
 * 值守设备
 */
interface Computer : BaseEntity<Computer> {
    companion object : Entity.Factory<Computer>()

    var id: Long?

    //设备ID
    var deviceId: String?
    var version: String?
    var name: String?

    //公司
    var company: Company?
    var register: Int?


    //校验时间，在线时间
    var lastTime: Long?

    //纬度
    var latitude: Double?

    //经度
    var longitude: Double?

    //备注
    var note: String?
}

/**
 * 电脑
 */
object Computers : BaseTable<Computer>("sys_computer") {
    val id = long("id").primaryKey().bindTo { it.id }

    //设备ID
    val deviceId = varchar("device_id").bindTo { it.deviceId }
    val version = varchar("version").bindTo { it.version }
    val name = varchar("name").bindTo { it.name }

    //公司
    val company = long("company_id").references(Companies) { it.company }

    //是否注册
    val register = int("register").bindTo { it.register }


    //校验时间，在线时间
    val lastTime = long("last_time").bindTo { it.lastTime }
    val latitude = double("latitude").bindTo { it.latitude }
    val longitude = double("longitude").bindTo { it.longitude }

    //备注
    val note = varchar("note").bindTo { it.note }

}

val Database.computers get() = this.sequenceOf(Computers)
