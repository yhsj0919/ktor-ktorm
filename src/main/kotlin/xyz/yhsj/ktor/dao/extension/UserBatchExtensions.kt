package xyz.yhsj.ktor.dao.extension

import org.ktorm.database.Database
import org.ktorm.dsl.inList
import org.ktorm.entity.filter
import org.ktorm.entity.toList
import xyz.yhsj.ktor.api.model.response.UserInfo
import xyz.yhsj.ktor.api.model.response.toUserInfo
import xyz.yhsj.ktor.dao.database.mysql
import xyz.yhsj.ktor.dao.entity.user.User
import xyz.yhsj.ktor.dao.entity.user.users

/**
 * 批量加载关联用户信息。
 *
 * 调用方只提供需要提取的用户 ID，方法会去重后执行一次查询，并返回安全的用户信息对象。
 */
fun <T> Iterable<T>.loadUserInfoMap(
    vararg userIdSelectors: (T) -> Long?,
    database: Database? = null,
): Map<Long, UserInfo> {
    require(userIdSelectors.isNotEmpty()) { "至少需要一个用户 ID 提取器" }

    val userIds = flatMap { item ->
        userIdSelectors.mapNotNull { selector -> selector(item) }
    }.toSet()
    if (userIds.isEmpty()) return emptyMap()

    val users: List<User> = (database ?: mysql()).users
        .filter { it.id inList userIds }
        .toList()

    return users.mapNotNull { user ->
        user.id?.let { it to user.toUserInfo() }
    }.toMap()
}

/** 批量加载用户信息并转换为响应对象。适合 data class 等不可变对象。 */
fun <T, R> Iterable<T>.mapWithUserInfo(
    vararg userIdSelectors: (T) -> Long?,
    database: Database? = null,
    transform: (T, Map<Long, UserInfo>) -> R,
): List<R> {
    val items = toList()
    val userInfoMap = items.loadUserInfoMap(*userIdSelectors, database = database)
    return items.map { item -> transform(item, userInfoMap) }
}

/** 批量加载用户信息并回写对象。适合可变的响应对象。 */
fun <T> Iterable<T>.fillUserInfo(
    vararg userIdSelectors: (T) -> Long?,
    database: Database? = null,
    fill: T.(Map<Long, UserInfo>) -> Unit,
): List<T> {
    val items = toList()
    val userInfoMap = items.loadUserInfoMap(*userIdSelectors, database = database)
    items.forEach { item -> item.fill(userInfoMap) }
    return items
}
