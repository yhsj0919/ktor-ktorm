package xyz.yhsj.ktor.service.impl


import org.ktorm.dsl.and
import org.ktorm.dsl.eq
import org.ktorm.dsl.or
import org.ktorm.entity.add
import org.ktorm.entity.toList
import xyz.yhsj.ktor.dao.mysql
import xyz.yhsj.ktor.entity.user.User
import xyz.yhsj.ktor.entity.user.users
import xyz.yhsj.ktor.service.UserService

class UserServiceImpl : UserService {
    override suspend fun getAllUsers(): List<User> {

        return mysql().users.toList()
    }

    override suspend fun addUser(user: User): User {
        val data = User {
            userName = user.userName
            passWord = user.passWord
        }
        mysql().users.add(data)
        println(data)
        return data
    }

    override suspend fun getUserByEmail(email: String): User? {
        TODO("Not yet implemented")
    }

    override suspend fun getUserById(id: Long): User {
        TODO("Not yet implemented")
    }

    override suspend fun loginAndGetUser(email: String, password: String): User {
        TODO("Not yet implemented")
    }


}
