package xyz.yhsj.ktor.service


import xyz.yhsj.ktor.entity.user.Company


interface CompanyService {

    suspend fun getList(db: String): List<Company>


    suspend fun getById(id: Long): Company?

    suspend fun addCompany(db: String, company: List<Company>): Company

}