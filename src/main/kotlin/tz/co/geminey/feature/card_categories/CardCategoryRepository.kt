package tz.co.geminey.feature.card_categories

import org.ktorm.dsl.from
import org.ktorm.dsl.mapNotNull
import org.ktorm.dsl.select
import tz.co.geminey.components.ApiResponse
import tz.co.geminey.components.ApiResponseUtils
import tz.co.geminey.config.DBHelper.database

class CardCategoryRepository() {

    fun getCategories(): Any {
        var response: Any
        try {
            val categories = database.from(CardCategories).select()
                .mapNotNull { row ->
                    val id: Int? = row[CardCategories.id]
                    val name: String? = row[CardCategories.name]
                    val description: String? = row[CardCategories.description]
                    if (id != null && name != null && description != null) {
                        mapOf(
                            "id" to id,
                            "name" to name,
                            "description" to description
                        )
                    } else {
                        null
                    }
                }
            response = ApiResponse(
                code = 200,
                message = "Success",
                body = mapOf("categories" to categories)
            )
           return ApiResponseUtils.toJson(response)

        } catch (e: Exception) {
            println("Error connecting to the database: ${e.message}")
            response = ApiResponse(
                code = 500,
                message = "Internal Server Error",
                body = emptyMap<String, Any>()
            )
            return ApiResponseUtils.toJson(response)
        }
    }
}