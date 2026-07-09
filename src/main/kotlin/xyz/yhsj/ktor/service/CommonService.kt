package xyz.yhsj.ktor.service

import xyz.yhsj.ktor.entity.resp.CommonResp

/**
 * 通用接口
 */
interface CommonService {

    /**
     * 测试
     */
    suspend fun test(): CommonResp
}


