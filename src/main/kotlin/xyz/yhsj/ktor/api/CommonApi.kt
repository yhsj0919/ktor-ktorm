package xyz.yhsj.ktor.api

import io.ktor.server.application.*
import io.ktor.server.http.content.*
import io.ktor.server.http.content.resources
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.ktor.ext.inject
import xyz.yhsj.ktor.dao.mysql
import xyz.yhsj.ktor.entity.resp.CommonResp
import xyz.yhsj.ktor.entity.user.Company
import xyz.yhsj.ktor.entity.user.User
import xyz.yhsj.ktor.ext.*
import xyz.yhsj.ktor.service.CompanyService
import xyz.yhsj.ktor.service.UserService
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.*


fun Route.commonApi() {
    val userService: UserService by inject()
    val companyService: CompanyService by inject()
    /**
     * 静态资源
     */
    static("/") {
        resources("static")
    }
    /**
     * 测试页面
     */
    get("/") {
        call.respondRedirect("/index.html", permanent = true)
    }


    /**
     *   初始化
     */
    getExt("/init") { params, session ->
        val sql = resources("sql", "test.sql")
        if (sql != null)
            mysql("test").initWithSqlFile(sql.inputStream()!!)

        "测试"
    }

    /**
     *    所有用户
     */
    getExt("/users") { params, session ->
        userService.getAllUsers()
    }

    getExt("/addUser") { params, session ->
        val user = User {
            userName = "张三"
            passWord = "123456"
        }
        userService.addUser(user)
    }

    getExt("/addCompany") { params, session ->
        val company = (1..100).map {
            Company {
                name = "我是客户"
                user = User {
                    id = Random().nextLong(1, 11)
                }
            }
        }
        companyService.addCompany("test", company)
    }


    getExt("/addCompany2") { params, session ->
        val company = (1..100).map {
            Company {
                name = "我是客户2"
                user = User {
                    id = Random().nextLong(1, 11)
                }
            }
        }
        companyService.addCompany("test2", company)
    }


    getExt("/companies") { params, session ->
        companyService.getList("test")
    }

    getExt("/companies2") { params, session ->
        companyService.getList("test2")
    }

    getExt("/create") { params, session ->
        val databaseName = "test2" //准备创建的数据库名
        mysql().createDatabase(databaseName)
        "完成"
    }

}
