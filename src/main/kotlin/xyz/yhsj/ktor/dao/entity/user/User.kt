package xyz.yhsj.ktor.dao.entity.user

import com.fasterxml.jackson.annotation.JsonIgnore
import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.int
import org.ktorm.schema.long
import org.ktorm.schema.varchar
import xyz.yhsj.ktor.base.config.BaseDataBase
import xyz.yhsj.ktor.dao.entity.common.BaseEntity
import xyz.yhsj.ktor.dao.entity.common.BaseTable

/** 用户数据库实体。请求参数请使用 api.model.request.user 下的对象。 */
interface User : BaseEntity<User> {
    companion object : Entity.Factory<User>()

    var id: Long?
    var userName: String?
    var roleId: Long?
    var nickName: String?
    var firstSpell: String?
    var password: String?
    @get:JsonIgnore
    var passwordId: Long?
    var type: Int?
    var note: String?
}

open class Users(alias: String?) :
    BaseTable<User>("sys_user", schema = BaseDataBase, alias = alias, bindUserReferences = false) {
    companion object : Users(null)

    override fun aliased(alias: String) = Users(alias)

    val id = long("id").primaryKey().bindTo { it.id }
    val userName = varchar("user_name").bindTo { it.userName }
    val roleId = long("role_id").bindTo { it.roleId }
    val nickName = varchar("nick_name").bindTo { it.nickName }
    val firstSpell = varchar("firstSpell").bindTo { it.firstSpell }
    val type = int("type").bindTo { it.type }
    val password = long("password_id").bindTo { it.passwordId }
    val note = varchar("note").bindTo { it.note }
}

val Database.users get() = this.sequenceOf(Users)
