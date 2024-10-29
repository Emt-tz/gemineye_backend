package tz.co.geminey.feature.compatibility

import kotlinx.serialization.Serializable
import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.varchar

object QuestionTable : Table<Nothing>("quiz_questions") {
    val id = int("id").primaryKey()
    val question = varchar("question")
    val version = int("version_id")
}


object AnswersTables : Table<Nothing>("quiz_answers") {
    val id = int("id").primaryKey()
    val answer = varchar("answer")
    val qnId = int("qn_id")
    val version = int("version_id")
}


@Serializable
data class QuestionAnswer(
    val id: Int,
    val question: String?,
    val version: Int?,
    val answer: List<Answers>,
)

@Serializable
data class Answers(
    val id: Int?,
    val answer: String?,
    val version: Int?
)


@Serializable
data class Result(
    val questionId: Int,
    val answerId: Int
)

@Serializable
data class Results(
    val resultList: List<Result>,
)