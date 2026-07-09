package xyz.yhsj.ktor.service


import xyz.yhsj.ktor.auth.AppSession
import xyz.yhsj.ktor.entity.resp.CommonResp
import xyz.yhsj.ktor.entity.user.SysUser

interface UserService {


    /**
     * 登录
     */
    suspend fun login(params: SysUser): CommonResp

    /**
     * 删除用户
     */
    suspend fun deleteUser(params: SysUser, session: AppSession): CommonResp

    /**
     * 注册
     */
    suspend fun register(params: SysUser, session: AppSession): Any

    /**
     * 获取所有用户
     */
    suspend fun getUsers(): Any

    suspend fun getUsersWithCompany(params: SysUser, sessions: AppSession): Any

    /**
     * 修改用户
     */
    suspend fun editUser(params: SysUser, session: AppSession): CommonResp

}

