package xyz.yhsj.ktor.service.impl

import org.ktorm.dsl.*
import org.ktorm.entity.*
import org.ktorm.support.mysql.insertOrUpdate
import xyz.yhsj.ktor.auth.AppSession
import xyz.yhsj.ktor.persistence.database.mysql
import xyz.yhsj.ktor.persistence.entity.company.*
import xyz.yhsj.ktor.persistence.entity.permission.Permission
import xyz.yhsj.ktor.persistence.entity.permission.permissions
import xyz.yhsj.ktor.api.model.response.CommonResp
import xyz.yhsj.ktor.api.model.response.PageUtil
import xyz.yhsj.ktor.persistence.entity.user.*
import xyz.yhsj.ktor.auth.crypto.DESCrypt
import xyz.yhsj.ktor.service.CompanyService
import java.util.Date

class CompanyServiceImpl : CompanyService {
    override suspend fun getCompany(params: SysCompany, sessions: AppSession): Any {
        val page = params.page
        val size = params.size
        val data = mysql().companies
            .sortedByDescending { it.createTime }
            .drop(page * size).take(size)

        val pageUtil = PageUtil(page, size, data.totalRecordsInAllPages.toLong(), data.map {
            it.key = DESCrypt.encodeCompanyId(it.id)
            it
        }.toList())

        return CommonResp.success(data = pageUtil)
    }

    override suspend fun addCompany(params: SysCompany, sessions: AppSession): CommonResp {
        val key = mysql().insertAndGenerateKey(Companies) {
            set(it.name, params.name)
            set(it.phone, params.phone)
            set(it.expirationTime, params.expirationTime)
            set(it.status, params.status ?: 0)
            set(it.computerCheck, params.computerCheck ?: 0)
            set(it.longitude, params.longitude)
            set(it.latitude, params.latitude)
            set(it.address, params.address)
            set(it.note, params.note)
            set(it.deleted, 0)
            set(it.createTime, Date().time)
            set(it.creator, sessions.user?.id)
        }
        return CommonResp.success(data = Company { id = key.toString().toLong() })
    }

    override suspend fun editCompany(params: SysCompany, sessions: AppSession): Any {
        val data = Company {
            id = params.id
            name = params.name
            phone = params.phone
            expirationTime = params.expirationTime
            status = params.status
            computerCheck = params.computerCheck
            longitude = params.longitude
            latitude = params.latitude
            address = params.address
            note = params.note
            editTime = Date().time
            editor = sessions.user
        }
        mysql().companies.update(data)
        return CommonResp.success()
    }

    override suspend fun deleteCompany(params: SysCompany, sessions: AppSession): Any {
        val data = mysql().companies.find { it.id eq (params.id ?: -1) } ?: return CommonResp.success()
        data.deleted = 1
        mysql().companies.update(data)
        return CommonResp.success()
    }

    override suspend fun getCompanyAdmin(params: SysCompany, sessions: AppSession): Any {
        val admin = mysql().users.find {
            it.companyId eq (params.id ?: -1) and (it.type eq -1) and (it.deleted eq 0)
        }
        if (admin != null) {
            val psd = mysql().passwords.find { it.id eq admin.passwordId!! }
            admin.password = psd?.password
        }
        return CommonResp.success(data = admin)
    }

    override suspend fun setCompanyAdmin(params: SysUser, sessions: AppSession): Any {
        val oldData = mysql().users.find {
            it.companyId eq (params.companyId ?: -1) and (it.deleted eq 0) and (it.type eq -1)
        }
        val oldUser = mysql().users.find {
            it.userName eq (params.userName ?: "") and (it.deleted eq 0) and (it.type eq -1)
        }

        if (oldUser != null && oldUser.companyId != params.companyId) {
            return CommonResp.error(msg = "user already exists")
        }

        val id = if (oldData != null) {
            mysql().update(Passwords) {
                set(it.password, params.password)
                where { it.id eq (oldData.passwordId ?: -1) }
            }
            oldData.passwordId!!
        } else {
            mysql().insertAndGenerateKey(Passwords) {
                set(it.password, params.password)
            }.toString().toLong()
        }

        mysql().insertOrUpdate(Users) {
            set(it.id, oldData?.id)
            set(it.userName, params.userName)
            set(it.nickName, "admin")
            set(it.type, -1)
            set(it.companyId, params.companyId)
            set(it.password, id)
            set(it.createTime, Date().time)
            set(it.creator, sessions.user?.id)

            onDuplicateKey {
                set(it.id, oldData?.id)
                set(it.userName, params.userName)
                set(it.password, id)
                set(it.editTime, Date().time)
                set(it.editor, sessions.user?.id)
            }
        }

        return CommonResp.success()
    }

    override suspend fun getCompanyPermission(params: SysCompany, sessions: AppSession): Any {
        val selected = mysql().companyPermissions.filter {
            it.companyId eq (params.id ?: -1)
        }.toList().map { it.permission }

        val permissions = mysql().permissions.toList().filter { it.type == 0 && it.enable == 1 }
        return CommonResp.success(data = getPermissionTree(permissions, null, selected))
    }

    override suspend fun setCompanyPermission(params: SysCompanyPermission, sessions: AppSession): Any {
        mysql().delete(CompanyPermissions) {
            it.companyId eq (params.companyId ?: -1)
        }

        if (params.permission?.isNotEmpty() == true) {
            mysql().batchInsert(CompanyPermissions) {
                for (permission in params.permission ?: emptyList()) {
                    item {
                        set(it.companyId, params.companyId)
                        set(it.permission, permission)
                    }
                }
            }
        }
        return CommonResp.success()
    }

    private fun getPermissionTree(
        list: List<Permission?>,
        parent: Permission? = null,
        selects: List<Permission?>? = null,
    ): List<Permission?> {
        return list
            .filter { it?.parent == parent?.id }
            .map { item ->
                item?.parentName = parent?.name
                item?.select = selects?.find { it?.id == item?.id } != null
                item?.children = getPermissionTree(list, item, selects)
                item
            }
            .sortedBy { it?.weight }.toList()
    }
}
