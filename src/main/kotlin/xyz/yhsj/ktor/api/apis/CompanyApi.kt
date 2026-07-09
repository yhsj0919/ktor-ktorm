package xyz.yhsj.ktor.api.apis

import io.ktor.server.routing.*
import org.koin.ktor.ext.inject
import redis.clients.jedis.params.SetParams
import xyz.yhsj.ktor.entity.company.SysCompany
import xyz.yhsj.ktor.entity.company.SysCompanyPermission
import xyz.yhsj.ktor.entity.resp.CommonResp
import xyz.yhsj.ktor.entity.user.SysUser
import xyz.yhsj.ktor.ext.postExt
import xyz.yhsj.ktor.redis.Redis
import xyz.yhsj.ktor.service.CompanyService
import xyz.yhsj.ktor.validator.VG

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
