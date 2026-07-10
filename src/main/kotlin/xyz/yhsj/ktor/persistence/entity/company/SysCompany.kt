package xyz.yhsj.ktor.persistence.entity.company

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.double
import org.ktorm.schema.int
import org.ktorm.schema.long
import org.ktorm.schema.varchar
import xyz.yhsj.ktor.infrastructure.config.BaseDataBase
import xyz.yhsj.ktor.persistence.entity.common.BaseEntity
import xyz.yhsj.ktor.persistence.entity.common.BaseTable
import xyz.yhsj.ktor.api.model.request.BaseParams
import xyz.yhsj.ktor.common.validation.VG

@JsonIgnoreProperties(ignoreUnknown = true)
data class SysCompany(
    @field:NotNull(message = "ID can not be null", groups = [VG.Update::class, VG.Delete::class])
    val id: Long? = null,

    @field:NotBlank(message = "name can not be blank", groups = [VG.Add::class])
    var name: String? = null,

    @field:NotBlank(message = "phone can not be blank", groups = [VG.Add::class])
    var phone: String? = null,

    @field:NotNull(message = "expirationTime can not be null", groups = [VG.Add::class])
    var expirationTime: Long? = null,

    var status: Int? = 0,
    var computerCheck: Int? = 0,
    var latitude: Double? = null,
    var longitude: Double? = null,
    var address: String? = null,
    var note: String? = null,
) : BaseParams()

interface Company : BaseEntity<Company> {
    companion object : Entity.Factory<Company>()

    var id: Long?
    var name: String?
    var phone: String?
    var expirationTime: Long?
    var status: Int?
    var computerCheck: Int?
    var latitude: Double?
    var longitude: Double?
    var key: String?
    var address: String?
    var note: String?
}

object Companies : BaseTable<Company>("sys_company", schema = BaseDataBase) {
    val id = long("id").primaryKey().bindTo { it.id }
    val name = varchar("name").bindTo { it.name }
    val phone = varchar("phone").bindTo { it.phone }
    val expirationTime = long("expiration_time").bindTo { it.expirationTime }
    val status = int("status").bindTo { it.status }
    val computerCheck = int("computer_check").bindTo { it.computerCheck }
    val latitude = double("latitude").bindTo { it.latitude }
    val longitude = double("longitude").bindTo { it.longitude }
    val address = varchar("address").bindTo { it.address }
    val note = varchar("note").bindTo { it.note }
}

val Database.companies get() = this.sequenceOf(Companies)
