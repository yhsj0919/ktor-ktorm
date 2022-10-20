package xyz.yhsj.ktor.service.impl


import org.ktorm.dsl.batchInsert
import org.ktorm.dsl.eq
import org.ktorm.entity.find
import org.ktorm.entity.toList
import xyz.yhsj.ktor.dao.mysql
import xyz.yhsj.ktor.entity.user.Companies
import xyz.yhsj.ktor.entity.user.Company
import xyz.yhsj.ktor.entity.user.companies
import xyz.yhsj.ktor.service.CompanyService

class CompanyServiceImpl : CompanyService {
    /**
     * @param db 数据库名称
     */
    override suspend fun getList(db: String): List<Company> {
        return mysql(db).companies.toList()
    }

    override suspend fun getById(id: Long): Company? {
        return mysql("test").companies.find { it.id eq id }
    }

    override suspend fun addCompany(db: String, company: List<Company>): Company {

        val size = mysql(db).batchInsert(Companies) {
            for (col in company) {
                item {
                    set(it.name, col.name)
                    set(it.user, col.user.id)
                }
            }
        }
        println(size.size)
        return Company()
    }


}
