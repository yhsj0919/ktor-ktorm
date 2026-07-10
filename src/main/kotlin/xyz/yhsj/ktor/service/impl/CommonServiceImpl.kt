package xyz.yhsj.ktor.service.impl

import xyz.yhsj.ktor.api.model.response.CommonResp
import xyz.yhsj.ktor.service.CommonService


class CommonServiceImpl : CommonService {
    /**
     * 测试
     */
    override suspend fun test(): CommonResp {

        return CommonResp.success()
    }


}


