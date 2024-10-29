package tz.co.geminey.config

import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.server.application.*
import org.ktorm.database.Database
import org.ktorm.jackson.sharedObjectMapper
//import org.jetbrains.exposed.sql.Database
import org.ktorm.logging.ConsoleLogger
import org.ktorm.logging.LogLevel
import org.ktorm.schema.BaseTable
import org.ktorm.schema.Column
import org.ktorm.schema.SqlType
import org.ktorm.schema.typeOf
import java.sql.PreparedStatement
import java.sql.ResultSet
import java.sql.Types

object DBHelper {

    private var dbUrl = ""
    private var dbUser = ""
    private var dbPwd = ""

    private const val KEY_DB_URL = "DB_URL"
    private const val KEY_DB_USER = "DB_USER"
    private const val KEY_DB_PWD = "DB_PWD"

    fun Application.configureDbVariables() {
        dbUrl = environment.config.propertyOrNull(KEY_DB_URL)?.getString() ?: ""
        dbUser = environment.config.propertyOrNull(KEY_DB_USER)?.getString() ?: ""
        dbPwd = environment.config.propertyOrNull(KEY_DB_PWD)?.getString() ?: ""
    }

    val database = Database.connect(
        driver = "com.mysql.cj.jdbc.Driver",
       url = "jdbc:mysql://127.0.0.1:3306/connect",
        user = "cto",
//        user = "connect",
        password = "Fiqra@22!!",
//        password = "SAAdqca23#",
        logger = ConsoleLogger(threshold = LogLevel.INFO)
    )
}

//json datatype implementation
class JsonSqlType<T : Any>(
    private val objectMapper: ObjectMapper,
    private val javaType: JavaType
) : SqlType<T>(Types.VARCHAR, "json") {
    override fun doSetParameter(ps: PreparedStatement, index: Int, parameter: T) {
        ps.setString(index, objectMapper.writeValueAsString(parameter))
    }
    override fun doGetResult(rs: ResultSet, index: Int): T? {
        val json = rs.getString(index)
        return if (json.isNullOrBlank()) {
            null
        } else {
            objectMapper.readValue(json, javaType)
        }
    }
}

inline fun <reified C : Any> BaseTable<*>.json(
    name: String,
    mapper: ObjectMapper = sharedObjectMapper
): Column<C> {
    return registerColumn(name, JsonSqlType(mapper, mapper.constructType(typeOf<C>())))
}