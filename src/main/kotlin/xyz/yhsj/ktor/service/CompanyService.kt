package xyz.yhsj.ktor.service


import xyz.yhsj.ktor.auth.AppSession
import xyz.yhsj.ktor.persistence.entity.company.SysCompany
import xyz.yhsj.ktor.persistence.entity.company.SysCompanyPermission
import xyz.yhsj.ktor.api.model.response.CommonResp
import xyz.yhsj.ktor.persistence.entity.user.SysUser

/**
 * 公司
 */
interface CompanyService {


    /**
     * 获取公司
     */
    suspend fun getCompany(params: SysCompany, sessions: AppSession): Any

    /**
     * 添加公司
     */
    suspend fun addCompany(params: SysCompany, sessions: AppSession): CommonResp

    /**
     * 修改公司
     */
    suspend fun editCompany(params: SysCompany, sessions: AppSession): Any

    /**
     * 删除公司
     */
    suspend fun deleteCompany(params: SysCompany, sessions: AppSession): Any

    /**
     * 获取管理员
     */
    suspend fun getCompanyAdmin(params: SysCompany, sessions: AppSession): Any

    /**
     * 设置管理员
     */
    suspend fun setCompanyAdmin(params: SysUser, sessions: AppSession): Any


    /**
     * 获取权限
     */
    suspend fun getCompanyPermission(params: SysCompany, sessions: AppSession): Any

    /**
     * 设置权限
     */
    suspend fun setCompanyPermission(params: SysCompanyPermission, sessions: AppSession): Any
}
