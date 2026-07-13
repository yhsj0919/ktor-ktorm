package xyz.yhsj.ktor.service.impl

import org.ktorm.dsl.*
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.toList
import org.ktorm.entity.update
import xyz.yhsj.ktor.auth.AppSession
import xyz.yhsj.ktor.auth.crypto.PasswordUtil
import xyz.yhsj.ktor.api.model.request.user.UserCreateRequest
import xyz.yhsj.ktor.api.model.request.user.UserDeleteRequest
import xyz.yhsj.ktor.api.model.request.user.UserLoginRequest
import xyz.yhsj.ktor.api.model.request.user.UserUpdateRequest
import xyz.yhsj.ktor.api.model.request.user.AdminSetupRequest
import xyz.yhsj.ktor.dao.database.mysql
import xyz.yhsj.ktor.api.model.response.CommonResp
import xyz.yhsj.ktor.api.model.response.ResponseCode
import xyz.yhsj.ktor.dao.entity.user.*
import xyz.yhsj.ktor.dao.extension.insertOrUpdate
import xyz.yhsj.ktor.common.util.pingYin
import xyz.yhsj.ktor.service.UserService
import java.util.*


/**
 * 用户
 */
class UserServiceImpl : UserService {
    override suspend fun initializeAdmin(params: AdminSetupRequest): CommonResp {
        return synchronized(UserServiceImpl::class.java) {
            val database = mysql()
            database.useTransaction {
                val hasUsers = database.users
                    .filter { it.deleted eq 0 }
                    .toList()
                    .isNotEmpty()
                if (hasUsers) {
                    return@useTransaction CommonResp.error(code = ResponseCode.CONFLICT, msg = "管理员已经初始化")
                }

                val passwordId = database.insertAndGenerateKey(Passwords) {
                    set(it.password, PasswordUtil.hash(params.password.orEmpty()))
                }
                database.insert(Users) {
                    set(it.userName, params.userName)
                    set(it.nickName, params.nickName)
                    set(it.roleId, null)
                    set(it.type, -1)
                    set(it.password, passwordId.toString().toLong())
                    set(it.deleted, 0)
                    set(it.createTime, Date().time)
                }
                CommonResp.success(msg = "管理员初始化成功")
            }
        }
    }

    /**
     * 登录
     */
    override suspend fun login(params: UserLoginRequest): CommonResp {

        val hasUsers = mysql().users
            .filter { it.deleted eq 0 }
            .toList()
            .isNotEmpty()
        if (!hasUsers) return CommonResp.systemNotInitialized()

        val user = mysql().users.find {
            it.userName eq (params.userName ?: "") and (it.deleted eq 0)
        } ?: return CommonResp.login(msg = "账号或密码错误")
        val password = user.passwordId?.let { passwordId ->
            mysql().passwords.find { it.id eq passwordId }
        }
        if (!PasswordUtil.matches(params.password.orEmpty(), password?.password)) {
            return CommonResp.login(msg = "账号或密码错误")
        }


        return CommonResp.success(data = user)
    }

    /**
     * 删除用户
     */
    override suspend fun deleteUser(params: UserDeleteRequest, session: AppSession): CommonResp {

        val data = User {
            id = params.id
            deleted = 1
        }
        mysql().users.update(data)

        return CommonResp.success()
    }

    /**
     * 注册
     */
    override suspend fun register(params: UserCreateRequest, session: AppSession): Any {
        val oldData = mysql().users.find {
            it.userName eq (params.userName ?: "") and (it.deleted eq 0)
        }
        if (oldData != null) {
            return CommonResp.error(msg = "该用户已存在")
        }

        val id = mysql().insertAndGenerateKey(Passwords) {
            set(it.password, PasswordUtil.hash(params.password.orEmpty()))
        }.toString().toLong()


        mysql().insert(Users) {
            set(it.userName, params.userName)
            set(it.nickName, params.nickName)
            set(it.firstSpell, params.nickName?.pingYin(true))
            set(it.roleId, params.roleId)
            set(it.type, 0)
            set(it.password, id.toString().toLong())
            set(it.createTime, Date().time)
        }

        return CommonResp.success()
    }

    /**
     * 获取所有用户
     */
    override suspend fun getUsers(): Any {
        val users = mysql().users
            .filter { it.deleted eq 0 }
            .toList()

        return CommonResp.success(data = users)
    }

    /**
     * 获取所有用户
     */
    /**
     * 修改用户
     */
    override suspend fun editUser(params: UserUpdateRequest, session: AppSession): CommonResp {
        val oldData = mysql().users.find { it.id eq (session.user?.id ?: -1) and (it.deleted eq 0) }

        if (params.password?.isNotEmpty() == true) {
            val pwd = mysql().passwords.find { it.id eq (oldData?.passwordId ?: -1) }
            if (!PasswordUtil.matches(params.oldPassword.orEmpty(), pwd?.password)) {
                return CommonResp.error(msg = "原密码错误")
            }
            pwd?.password = PasswordUtil.hash(params.password.orEmpty())
            mysql().passwords.update(pwd!!)
        }

        oldData?.nickName = params.nickName
        oldData?.firstSpell = params.nickName?.pingYin(true)
        oldData?.editorId = session.user?.id
        oldData?.editTime = Date().time

        mysql().users.update(oldData!!)
        return CommonResp.success()
    }


}


