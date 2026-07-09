package xyz.yhsj.ktor.entity.user


import org.ktorm.database.Database
import org.ktorm.entity.Entity
import org.ktorm.entity.sequenceOf
import org.ktorm.schema.Table
import org.ktorm.schema.long
import org.ktorm.schema.varchar
import xyz.yhsj.ktor.BaseDataBase

/**
 * 密码
 */
interface Password : Entity<Password> {
    companion object : Entity.Factory<Password>()

    val id: Long?
    var password: String?
}

object Passwords : Table<Password>("sys_password", schema = BaseDataBase) {

    val id = long("id").primaryKey().bindTo { it.id }
    val password = varchar("password").bindTo { it.password }

}

val Database.passwords get() = this.sequenceOf(Passwords)