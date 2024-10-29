package tz.co.geminey.models

import org.ktorm.schema.Table
import org.ktorm.schema.boolean
import org.ktorm.schema.int
import org.ktorm.schema.varchar

object FileTable : Table<Nothing>("files") {
    val id = int("id").primaryKey()
    val username = varchar("username")
    val name = varchar("name")
    val isProfile = boolean("is_profile")
    val dateCreated = varchar("date_created")
}