package tz.co.geminey.feature.cards

import org.ktorm.dsl.*
import org.ktorm.schema.ColumnDeclaring
import tz.co.geminey.components.ApiResponse
import tz.co.geminey.config.DBHelper.database

class CardRepository {

    private fun getCardsByCondition(condition: ColumnDeclaring<Boolean>): Any {
        return try {
            val cards = database.from(Cards).select().where { condition }
                .mapNotNull { row ->
                    val id: Int? = row[Cards.id]
                    val question: String? = row[Cards.question]
                    val categoryId: Int? = row[Cards.categoryId]
                    val relationStatus: Int? = row[Cards.relationStatus]
                    mapOf(
                        "id" to id,
                        "question" to question,
                        "categoryId" to categoryId,
                        "relationStatus" to relationStatus
                    )
                }

            ApiResponse(
                code = 200,
                message = "Success",
                body = mapOf("cards" to cards)
            )

        } catch (e: Exception) {
            println("Error connecting to the database: ${e.message}")
            ApiResponse(
                code = 500,
                message = "Internal Server Error",
                body = emptyMap<String, Any>()
            )
        }
    }

    fun getAllCards(): Any {
        return getCardsByCondition(Cards.id notEq 0)
    }

    fun getCardsByRelationStatus(status: Int): Any {
        return getCardsByCondition(Cards.relationStatus eq status)
    }

    fun getCardsByRelationStatusAndCategory(status: Int, category: Int): Any {
        return getCardsByCondition((Cards.relationStatus eq status) and (Cards.categoryId eq category))
    }

    fun getCardsByCategory(category: Int): Any {
        return getCardsByCondition(Cards.categoryId eq category)
    }

}
