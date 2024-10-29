package tz.co.geminey.feature.card_categories

import kotlinx.serialization.Serializable
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar

//@Serializable
data class CardCategory(val id: Int, val name: String, val description: String)

object CardCategories : Table<Nothing>("card_categories") {
    val id = int("id").primaryKey()
    val name = varchar("name")
    val description = varchar("description")
}