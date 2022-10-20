package xyz.yhsj.ktor.service


import xyz.yhsj.ktor.entity.user.User

interface UserService {

    suspend fun getAllUsers(): List<User>
    suspend fun addUser(user:User): User

    suspend fun getUserByEmail(email: String): User?

    suspend fun getUserById(id: Long): User

    suspend fun loginAndGetUser(email: String, password: String): User

}