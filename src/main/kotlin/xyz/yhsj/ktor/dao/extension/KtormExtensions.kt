package xyz.yhsj.ktor.dao.extension

import org.ktorm.database.Database
import org.ktorm.database.asIterable
import org.ktorm.dsl.*
import org.ktorm.entity.*
import org.ktorm.expression.*
import org.ktorm.schema.*
import xyz.yhsj.ktor.dao.entity.common.BaseEntity
import xyz.yhsj.ktor.dao.entity.common.BaseTable
import xyz.yhsj.ktor.dao.entity.user.Users
import java.math.BigDecimal
import java.sql.Date
import kotlin.reflect.KMutableProperty1

private val DATABASE_IDENTIFIER = Regex("[A-Za-z0-9_$-]+")

/**
 * 创建数据库
 */
fun Database.createDatabase(database: String): Boolean {
    val safeDatabase = requireDatabaseIdentifier(database, "database")
    return this.useConnection { conn ->
        val databaseSql = "create database `$safeDatabase`"
        conn.prepareStatement(databaseSql).use { statement ->
            val size = statement.executeUpdate()
            size >= 1
        }

    }
}

/**
 * 判断数据库下面的表是否存在
 * @param tableName 表名
 * @param database 数据库名称，不传则为当前连接的数据库
 */
fun Database.tableExists(tableName: String, database: String? = null): Boolean {
    requireDatabaseIdentifier(tableName, "tableName")
    val schemaName = requireDatabaseIdentifier(database ?: name, "database")
    return this.useConnection { conn ->
        val databaseSql =
            "select count(*) count from information_schema.tables where table_schema=? and table_name=?"
        conn.prepareStatement(databaseSql).use { statement ->
            statement.setString(1, schemaName)
            statement.setString(2, tableName)
            val result = statement.executeQuery().asIterable().firstOrNull()
            if (result != null) {
                result.getInt("count") >= 1
            } else {
                false
            }
        }
    }
}


/**
 * 判断数据库是否存在
 * @param database 数据库名称
 */
fun Database.dataBaseExists(database: String): Boolean {
    val schemaName = requireDatabaseIdentifier(database, "database")
    return this.useConnection { conn ->
        val databaseSql =
            "select count(*) count from information_schema.schemata where schema_name=?"
        conn.prepareStatement(databaseSql).use { statement ->
            statement.setString(1, schemaName)
            val result = statement.executeQuery().asIterable().firstOrNull()
            if (result != null) {
                result.getInt("count") >= 1
            } else {
                false
            }
        }

    }
}

private fun requireDatabaseIdentifier(value: String, name: String): String {
    require(value.isNotBlank() && DATABASE_IDENTIFIER.matches(value)) {
        "$name 包含非法字符"
    }
    return value
}

/**
 * 实现一对多关系
 * entity下使用
 * val employees get() = lazyFetch("employees") { Employees.findList { it.departmentId eq id } }
 */
inline fun <reified T> Entity<*>.lazyFetch(name: String, loader: () -> T): T {
    return this[name] as? T ?: loader().also { this[name] = it }
}


inline fun <E : Entity<E>, T : Table<E>> EntitySequence<E, T>.filterIf(
    condition: Boolean,
    predicate: (T) -> ColumnDeclaring<Boolean>,
): EntitySequence<E, T> {

    return if (condition) {
        this.filter(predicate)
    } else {
        this
    }
}

/**
 * 根据条件groupBy
 */
fun Query.groupByIf(condition: Boolean, vararg columns: ColumnDeclaring<*>): Query {
    return if (condition) {
        return groupBy(columns.asList())
    } else {
        this
    }
}


inline fun <E : BaseEntity<E>, T : BaseTable<E>> EntitySequence<E, T>.filterOr(
    predicate: (T) -> ColumnDeclaring<Boolean>,
): EntitySequence<E, T> {
    return if (expression.where == null) {
        this.withExpression(expression.copy(where = predicate(sourceTable).asExpression()))
    } else {
        this.withExpression(expression.copy(where = expression.where!! or predicate(sourceTable)))
    }
}

inline fun <E : BaseEntity<E>, T : BaseTable<E>> EntitySequence<E, T>.filterIfOr(
    condition: Boolean,
    predicate: (T) -> ColumnDeclaring<Boolean>,
): EntitySequence<E, T> {
    return if (condition) {
        this.filterOr(predicate)
    } else {
        this
    }

}


/**
 * 更新属性，在框架中，只要对属性赋值就会被更新到数据库，即使值为null
 * @param name 属性名字 Entity::name
 * @param value 需要更新的值
 * @param updateNull 是否更新空属性
 * @param callBack 更新回调，用于返回更新日志,这个需要先查找
 */
fun <R, T : Entity<T>> T.update(
    name: KMutableProperty1<T, R>,
    value: R?,
    updateNull: Boolean = false,
    updateSame: Boolean = false,
    sameCheck: ((oldValue: R?, newValue: R?) -> Boolean)? = { oldValue, newValue -> oldValue == newValue },
    callBack: ((oldValue: R?, newValue: R?) -> Unit)? = null,
): T {

    if (value != null) {
        if (updateSame) {
            callBack?.invoke(this[name.name] as R?, value)

            this[name.name] = value
        } else if (sameCheck?.invoke(this[name.name] as R?, value) != true) {
            callBack?.invoke(this[name.name] as R?, value)

            this[name.name] = value
        }
    } else {
        if (updateNull) {
            if (updateSame) {
                callBack?.invoke(this[name.name] as R?, null)

                this[name.name] = null
            } else if (sameCheck?.invoke(this[name.name] as R?, value) != true) {
                callBack?.invoke(this[name.name] as R?, null)

                this[name.name] = null
            }
        }
    }

    return this
}


fun <E : Entity<E>, T : Table<E>> EntitySequence<E, T>.insertOrUpdate(entity: E): Int {

    val primaryKeys = sourceTable.primaryKeys
    return if (primaryKeys.map { entity[it.name] != null }.contains(true)) {
        update(entity)
    } else {
        add(entity)
    }

}


public fun random(): FunctionExpression<Long> {
    return FunctionExpression(functionName = "random", arguments = emptyList(), sqlType = LongSqlType)
}

/**
 * 扩展MySql函数，计算两个坐标之间的距离
 * val juli = st_distance_sphere(
 *             point(116.31182540812625,36.15174649888155),
 *             point(Companies.longitude,Companies.latitude)
 *         ).aliased("juli")
 * mysql()
 *      .from(Companies)
 *      .select(Companies.name, juli)
 *      .forEach {
 *           println("${it[Companies.name]}:${it[juli]}")
 *       }
 */
public fun st_distance_sphere(
    point1: FunctionExpression<Double>,
    point2: FunctionExpression<Double>,
): FunctionExpression<Double> {
    return FunctionExpression(
        functionName = "st_distance_sphere",
        arguments = listOf(point1.asExpression(), point2.asExpression()),
        sqlType = DoubleSqlType
    )
}

/**
 *  * 扩展MySql函数，计算两个坐标之间的距离
 */
public fun st_distance_sphere(
    longitude: Double,
    latitude: Double,
    longitude2: Column<Double>,
    latitude2: Column<Double>,
): FunctionExpression<Double> {
    return FunctionExpression(
        functionName = "st_distance_sphere",
        arguments = listOf(point(longitude, latitude), point(longitude2, latitude2).asExpression()),
        sqlType = DoubleSqlType
    )
}

/**
 * 坐标点
 */
public fun point(longitude: Column<Double>, latitude: Column<Double>): FunctionExpression<Double> {
    return FunctionExpression(
        functionName = "point",
        arguments = listOf(longitude.asExpression(), latitude.asExpression()),
        sqlType = DoubleSqlType
    )
}

/**
 * 坐标点
 */
public fun point(longitude: Double, latitude: Double): FunctionExpression<Double> {
    return FunctionExpression(
        functionName = "point",
        arguments = listOf(ArgumentExpression(longitude, DoubleSqlType), ArgumentExpression(latitude, DoubleSqlType)),
        sqlType = DoubleSqlType
    )
}


public fun fromUnixTime(timestamp: Column<Long>): FunctionExpression<Date> {
    return FunctionExpression(
        functionName = "FROM_UNIXTIME",
        arguments = listOf((timestamp / 1000).asExpression()),
        sqlType = DateSqlType
    )
}

public fun dateFormat(timestamp: Column<Long>, format: String): FunctionExpression<String> {
    return FunctionExpression(
        functionName = "DATE_FORMAT",
        arguments = listOf(fromUnixTime(timestamp), ArgumentExpression(format, TextSqlType)),
        sqlType = TextSqlType
    )
}

public fun ifNull(expr1: Column<Any>, expr2: Column<Any>): FunctionExpression<Any> {
    return FunctionExpression(
        functionName = "IFNULL",
        arguments = listOf(
            ArgumentExpression(expr1, expr1.sqlType),
            ArgumentExpression(expr2, sqlType = expr2.sqlType)
        ),
        sqlType = expr1.sqlType
    )
}

public fun ifNull(expr1: Column<Any>, expr2: Any): FunctionExpression<Any> {
    return FunctionExpression(
        functionName = "IFNULL",
        arguments = listOf(
            ArgumentExpression(expr1, expr1.sqlType),
            ArgumentExpression(expr2, sqlType = expr1.sqlType)
        ),
        sqlType = expr1.sqlType
    )
}


/**
 * 别名扩展，用于实现子查询
 */
fun QueryExpression.aliased(s: String): QueryExpression {
    return when (this) {
        is SelectExpression -> this.copy(tableAlias = s)
        is UnionExpression -> this.copy(tableAlias = s)
    }
}

public fun Database.from(table: QuerySourceExpression): QuerySource {
    return QuerySource(this, EmptyTable, table)
}


/**
 *左联实现子查询
 */
public fun QuerySource.leftJoin(right: QuerySourceExpression, on: ColumnDeclaring<Boolean>? = null): QuerySource {
    return this.copy(
        expression = JoinExpression(
            type = JoinType.LEFT_JOIN,
            left = expression,
            right = right,
            condition = on?.asExpression()
        )
    )
}
/**
 *内联实现子查询
 */
public fun QuerySource.innerJoin(right: QuerySourceExpression, on: ColumnDeclaring<Boolean>? = null): QuerySource {
    return this.copy(
        expression = JoinExpression(
            type = JoinType.INNER_JOIN,
            left = expression,
            right = right,
            condition = on?.asExpression()
        )
    )
}

/**
 *全连接实现子查询
 */
public fun QuerySource.fullJoin(right: QuerySourceExpression, on: ColumnDeclaring<Boolean>? = null): QuerySource {
    return this.copy(
        expression = JoinExpression(
            type = JoinType.FULL_JOIN,
            left = expression,
            right = right,
            condition = on?.asExpression()
        )
    )
}


/**
 * 根据子查询的列别名，表别名，生成子查询列
 */
public operator fun <T : Any> QueryExpression.get(column: ColumnDeclaringExpression<T>): ColumnExpression<T> {
    if (tableAlias == null || tableAlias?.isEmpty() == true) {
        error("子查询必须含有别名")
    }
    if (column.declaredName == null || column.declaredName?.isEmpty() == true) {
        error("子查询列必须含有别名")
    }
    return ColumnExpression(
        table = TableExpression(this.tableAlias ?: ""),
        name = column.declaredName ?: "",
        sqlType = column.sqlType
    )

}

interface EmptyEntity : Entity<EmptyEntity>

/**
 * 空白表，用于子查询
 */
object EmptyTable : Table<EmptyEntity>("empty_table")





