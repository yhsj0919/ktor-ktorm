package xyz.yhsj.ktor.service


import xyz.yhsj.ktor.auth.AppSession
import xyz.yhsj.ktor.api.model.response.CommonResp
import xyz.yhsj.ktor.api.model.request.user.UserCreateRequest
import xyz.yhsj.ktor.api.model.request.user.UserDeleteRequest
import xyz.yhsj.ktor.api.model.request.user.UserLoginRequest
import xyz.yhsj.ktor.api.model.request.user.UserUpdateRequest
import xyz.yhsj.ktor.api.model.request.user.AdminSetupRequest

interface UserService {


    /**
     * 登录
     */
    suspend fun login(params: UserLoginRequest): CommonResp

    suspend fun initializeAdmin(params: AdminSetupRequest): CommonResp

    /**
     * 删除用户
     */
    suspend fun deleteUser(params: UserDeleteRequest, session: AppSession): CommonResp

    /**
     * 注册
     */
    suspend fun register(params: UserCreateRequest, session: AppSession): Any

    /**
     * 获取所有用户
     */
    suspend fun getUsers(): Any

    /**
     * 修改用户
     */
    suspend fun editUser(params: UserUpdateRequest, session: AppSession): CommonResp

}

