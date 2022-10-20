package xyz.yhsj.ktor.dao

import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

class CreateDataSource {
    var mysqlDriver = "com.mysql.jdbc.Driver"
    var url = "jdbc:mysql://rm-bp1bw0m9qmmq65551po.mysql.rds.aliyuncs.com:3306/test" //当前可以连接的本地数据库
    var newUrl = "jdbc:mysql://rm-bp1bw0m9qmmq65551po.mysql.rds.aliyuncs.com:3306/"
    var username = "yhsj"
    var password = "Yhsj0919"
    lateinit var conn: Connection
    var newConn: Connection? = null
    fun getConn(database: String): Connection {
        try {
            Class.forName(mysqlDriver)
        } catch (e: ClassNotFoundException) {
            e.printStackTrace()
        }
        try {
            val tableSql =
                "create table t_user (username varchar(50) not null primary key,password varchar(20) not null ); "
            val databaseSql = "create database $database"
            conn = DriverManager.getConnection(url, username, password)
            val smt = conn.createStatement()
            if (conn != null) {
                println("数据库连接成功!")
                var size = smt.executeUpdate(databaseSql)
                println("Sql执行完成$size")
                newConn = DriverManager.getConnection(
                    newUrl + database,
                    username, password
                )
                if (newConn != null) {
                    println("已经连接到新创建的数据库：$database")
                    val newSmt = newConn!!.createStatement()
                    val i = newSmt.executeUpdate(tableSql) //DDL语句返回值为0;
                    if (i == 0) {
                        println(tableSql + "表已经创建成功!")
                    }
                }
            }
        } catch (e1: SQLException) {
            e1.printStackTrace()
        }
        return conn
    }

    companion object {
        /**
         * @param args
         */
        @JvmStatic
        fun main(args: Array<String>) {
            val database = "ydb2" //准备创建的数据库名
            CreateDataSource().getConn(database)
        }
    }
}