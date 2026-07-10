package xyz.yhsj.ktor.service.impl

import org.ktorm.dsl.*
import org.ktorm.entity.filter
import org.ktorm.entity.find
import org.ktorm.entity.toList
import org.ktorm.entity.update
import xyz.yhsj.ktor.auth.AppSession
import xyz.yhsj.ktor.persistence.database.mysql
import xyz.yhsj.ktor.persistence.entity.company.Company
import xyz.yhsj.ktor.persistence.entity.company.companies
import xyz.yhsj.ktor.persistence.entity.computer.Computer
import xyz.yhsj.ktor.persistence.entity.computer.computers
import xyz.yhsj.ktor.api.model.response.CommonResp
import xyz.yhsj.ktor.persistence.entity.user.*
import xyz.yhsj.ktor.persistence.extension.filterIf
import xyz.yhsj.ktor.persistence.extension.insertOrUpdate
import xyz.yhsj.ktor.common.util.pingYin
import xyz.yhsj.ktor.service.UserService
import java.util.*


/**
 * 用户
 */
class UserServiceImpl : UserService {
    /**
     * 登录
     */
    override suspend fun login(params: SysUser): CommonResp {

        val count = mysql().users.toList().count()
        if (count == 0) {
            val id = mysql().insertAndGenerateKey(Passwords) {
                set(it.password, "admin#@.")
            }

            mysql().insert(Users) {
                set(it.userName, "18612345678")
                set(it.nickName, "超级管理员")
                set(it.password, id.toString().toLong())
                set(it.type, -1)
            }

            return CommonResp.error(msg = "已创建默认账号，请重新登录")

        } else {
            val user = mysql().users.find {
                it.userName eq (params.userName ?: "") and (it.deleted eq 0)
            } ?: return CommonResp.error(msg = "用户不存在")
            mysql().passwords.find {
                it.id eq user.passwordId!! and (it.password eq (params.password ?: ""))
            } ?: return CommonResp.error(msg = "密码错误")


            val userCompany = mysql().companies.find { it.id eq (user.companyId ?: -1) }

            user.company = userCompany


            val computer = params.computer
            if (computer != null) {

                val oldComputer = mysql().computers.find {
                    it.deviceId eq (computer.deviceId
                        ?: "") and (if (userCompany == null) it.company.isNull() else it.company eq (userCompany.id
                        ?: -1)) and (it.deleted eq 0)
                }

                val data = Computer {
                    id = oldComputer?.id
                    deviceId = computer.deviceId
                    name = computer.name
                    version = computer.version
                    company = Company { id = userCompany?.id }
                    latitude = computer.latitude
                    longitude = computer.longitude
                    lastTime = Date().time
                }
                mysql().computers.insertOrUpdate(data)


                if (userCompany != null) {
                    if (userCompany.computerCheck == 1 && oldComputer?.register != 1) {
                        return CommonResp.error(msg = "该设备还未授权，请联系管理员处理")
                    }
                }
            }

            if (userCompany != null && (userCompany?.expirationTime ?: 0) < Date().time) {
                return CommonResp.error(msg = "该账号已经到期，请联系管理员处理")
            }


            return CommonResp.success(data = user)
        }
    }

    /**
     * 删除用户
     */
    override suspend fun deleteUser(params: SysUser, session: AppSession): CommonResp {

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
    override suspend fun register(params: SysUser, session: AppSession): Any {
        val oldData = mysql().users.find {
            it.userName eq (params.userName ?: "") and (it.deleted eq 0)
        }
        if (oldData != null && oldData.companyId != params.companyId) {
            return CommonResp.error(msg = "该用户已存在")
        }

        val id = mysql().insertAndGenerateKey(Passwords) {
            set(it.password, params.password)
        }.toString().toLong()


        mysql().insert(Users) {
            set(it.userName, params.userName)
            set(it.nickName, params.nickName)
            set(it.firstSpell, params.nickName?.pingYin(true))
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
    override suspend fun getUsersWithCompany(params: SysUser, sessions: AppSession): Any {
        val users = mysql().users

            .filterIf(params.nickName != null)
            { it.nickName like "%${params.nickName ?: ""}%" or (it.firstSpell like "%${params.nickName ?: ""}%") or (it.userName like "%${params.nickName ?: ""}%") }
            .filter {
                it.roleId.isNotNull()
            }
            .filter {
                it.companyId eq (sessions.user?.companyId ?: 0) and (it.deleted eq 0)
            }
            .toList()

        return CommonResp.success(data = users)
    }

    /**
     * 修改用户
     */
    override suspend fun editUser(params: SysUser, session: AppSession): CommonResp {
        val oldData = mysql().users.find { it.id eq (session.user?.id ?: -1) and (it.deleted eq 0) }

        if (params.password?.isNotEmpty() == true) {
            val pwd = mysql().passwords.find { it.id eq (oldData?.passwordId ?: -1) }
            if (params.oldPassword != pwd?.password) {
                return CommonResp.error(msg = "原密码错误")
            }
            pwd?.password = params.password
            mysql().passwords.update(pwd!!)
        }

        oldData?.nickName = params.nickName
        oldData?.firstSpell = params.nickName?.pingYin(true)
        oldData?.editor = session.user
        oldData?.editTime = Date().time

        mysql().users.update(oldData!!)
        return CommonResp.success()
    }


}


