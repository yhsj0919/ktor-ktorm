package xyz.yhsj.ktor.persistence.entity.common

import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.long
import xyz.yhsj.ktor.persistence.entity.user.User
import xyz.yhsj.ktor.persistence.entity.user.Users


interface BaseEntity<E : BaseEntity<E>> : Entity<E> {

    //是否删除
    var deleted: Int?

    //创建人
    var creator: User?

    //修改人
    var editor: User?

    //删除人
    var deleter: User?

    //创建时间
    var createTime: Long?

    //修改时间
    var editTime: Long?

    //删除时间
    var deleteTime: Long?
}

abstract class BaseTable<E : BaseEntity<E>>(tableName: String, schema: String? = null, alias: String? = null) :
    Table<E>(tableName, schema = schema, alias = alias) {
    val deleted = int("deleted").bindTo { it.deleted }
    val createTime = long("create_time").bindTo { it.createTime }
    val editTime = long("edit_time").bindTo { it.editTime }
    val deleteTime = long("delete_time").bindTo { it.deleteTime }
    val creator = long("creator_id").references(Users) { it.creator }
    val editor = long("editor_id").references(Users) { it.editor }
    val deleter = long("deleter_id").references(Users) { it.deleter }
}

