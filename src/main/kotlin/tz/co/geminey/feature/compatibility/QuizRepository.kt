package tz.co.geminey.feature.compatibility

import io.ktor.http.*
import org.ktorm.dsl.*
import tz.co.geminey.components.ApiResponse
import tz.co.geminey.config.DBHelper.database
import tz.co.geminey.models.CustomerTable

class QuizRepository {
    fun getQuestionsAndAnswerByVersion(username: String): ApiResponse {
        return try {
            var lastQn = database.from(CustomerTable)
                .select(CustomerTable.lastQuestion)
                .where(CustomerTable.userId eq username.toInt())
                .map { it[CustomerTable.lastQuestion] }
                .firstOrNull()

            if (lastQn == null) {
                lastQn = 1
            }

            database.useTransaction {
                val questions = database.from(QuestionTable)
                    .select()
                    .where(QuestionTable.id greater lastQn )
                    .map { qn ->
                        val id = qn[QuestionTable.id]
                        if (id != null) {
                            val answers = database.from(AnswersTables)
                                .select()
                                .where(AnswersTables.qnId eq id)
                                .map { answer ->
                                    Answers(
                                        id = answer[AnswersTables.id],
                                        answer = answer[AnswersTables.answer],
                                        version = answer[AnswersTables.version]
                                    )
                                }
                            QuestionAnswer(
                                id = id,
                                question = qn[QuestionTable.question],
                                version = qn[QuestionTable.version],
                                answer = answers
                            )
                        } else {
                            null
                        }
                    }.filterNotNull()
                ApiResponse( code = 200, message = "Success", body = mapOf("questions" to questions) )
            }
        } catch (e: Exception) {
            ApiResponse( code = 500, message = "Internal Server Error", body = emptyMap<String, Any>() )
        }
    }

    fun saveResults(username: String, resultList: Results): ApiResponse {
        var response = ApiResponse(code = 500, message = "Internal Server Error", body = emptyMap<String, Any>())
        return try {
            database.useTransaction {
                val id = database.from(CustomerTable)
                    .select()
                    .where(CustomerTable.userId eq username.toInt())
                    .map { it[CustomerTable.id] }
                    .firstOrNull()

                if (id != null) {
                    val qns = IntArray(resultList.resultList.size)
                    val answers = IntArray(resultList.resultList.size)
                    resultList.resultList.forEachIndexed { index, result ->
                        answers[index] = result.answerId
                        qns[index] = result.questionId
                    }
                    val n = qns.sorted().toIntArray().last()
                    val rowCount = database.update(CustomerTable) {
                        where { CustomerTable.id eq id }
                        set(CustomerTable.lastQuestion, n)
                        set(CustomerTable.answers, answers.toList())
                    }
                    if (rowCount > 0) {
                        response = ApiResponse( code = 200, message = "Success", body = emptyMap<String, Any>() )
                    }
                } else {
                    response = ApiResponse(HttpStatusCode.Unauthorized.value, HttpStatusCode.Unauthorized.description, body = emptyMap<String, Any>())
                }
            }
            return response
        } catch (e: Exception) {
           response
        }
    }

}