package xyz.yhsj.ktor.dao.entity.common

import com.fasterxml.jackson.annotation.JsonInclude
import org.ktorm.entity.Entity
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.long

interface BaseEntity<E : BaseEntity<E>> : Entity<E> {

    //是否删除
    var deleted: Int?

    //创建人 ID
    var creatorId: Long?

    //修改人 ID
    var editorId: Long?

    //删除人 ID
    var deleterId: Long?

    //创建时间
    var createTime: Long?

    //修改时间
    var editTime: Long?

    //删除时间
    var deleteTime: Long?
}

abstract class BaseTable<E : BaseEntity<E>>(
    tableName: String,
    schema: String? = null,
    alias: String? = null,
    bindUserReferences: Boolean = true,
) :
    Table<E>(tableName, schema = schema, alias = alias) {
    val deleted = int("deleted").bindTo { it.deleted }
    val createTime = long("create_time").bindTo { it.createTime }
    val editTime = long("edit_time").bindTo { it.editTime }
    val deleteTime = long("delete_time").bindTo { it.deleteTime }

    val creatorId = long("creator_id").bindTo { it.creatorId }
    val editorId = long("editor_id").bindTo { it.editorId }
    val deleterId = long("deleter_id").bindTo { it.deleterId }
}

