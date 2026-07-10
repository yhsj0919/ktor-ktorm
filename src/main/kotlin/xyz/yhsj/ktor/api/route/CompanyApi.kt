package xyz.yhsj.ktor.api.route

import io.ktor.server.routing.Route
import io.ktor.server.routing.route
import org.koin.ktor.ext.inject
import redis.clients.jedis.params.SetParams
import xyz.yhsj.ktor.persistence.entity.company.SysCompany
import xyz.yhsj.ktor.persistence.entity.company.SysCompanyPermission
import xyz.yhsj.ktor.api.model.response.CommonResp
import xyz.yhsj.ktor.persistence.entity.user.SysUser
import xyz.yhsj.ktor.api.extension.postExt
import xyz.yhsj.ktor.infrastructure.cache.Redis
import xyz.yhsj.ktor.service.CompanyService
import xyz.yhsj.ktor.common.validation.VG

fun Route.companyApi() {
    val companyService by inject<CompanyService>()

    route("/company") {
        postExt<SysCompany>("/get") { params, session ->
            companyService.getCompany(params, session)
        }

        postExt<SysCompany>("/add", VG.Add::class.java) { params, session ->
            companyService.addCompany(params, session)
        }

        postExt<SysCompany>("/delete", VG.Delete::class.java) { params, session ->
            companyService.deleteCompany(params, session)
        }

        postExt<SysCompany>("/edit", VG.Update::class.java) { params, session ->
            companyService.editCompany(params, session)
        }

        postExt<SysCompany>("/admin/get", VG.Update::class.java) { params, session ->
            companyService.getCompanyAdmin(params, session)
        }

        postExt<SysUser>("/admin/set", VG.Admin::class.java) { params, session ->
            companyService.setCompanyAdmin(params, session)
        }

        postExt<SysCompany>("/permission/get", VG.Update::class.java) { params, session ->
            companyService.getCompanyPermission(params, session)
        }

        postExt<SysCompanyPermission>("/permission/set", VG.Admin::class.java) { params, session ->
            companyService.setCompanyPermission(params, session)
        }

        postExt("/redis") {
            Redis.set("testKey", "test", SetParams.setParams().ex(10))
            CommonResp.success()
        }
    }
}
