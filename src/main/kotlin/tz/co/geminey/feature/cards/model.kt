package tz.co.geminey.feature.cards

import kotlinx.serialization.Serializable
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar

object Cards : Table<Nothing>("cards") {
    val id = int("id").primaryKey()
    val question = varchar("question")
    val categoryId = int("category_id")
    val relationStatus = int("relation_status")
}

@Serializable
data class CardRequest(
    val category: Int = 0,
    val status: Int = 0,
)
