package tz.co.geminey.models

import org.ktorm.schema.Table
import org.ktorm.schema.int
import org.ktorm.schema.long
import org.ktorm.schema.varchar
import tz.co.geminey.config.json

object CustomerTable : Table<Nothing>("customers") {
    val id = int("id").primaryKey()
    val userId = int("user_id")
    val fullName = varchar("full_name")
    val phone = varchar("phone")
    val gender = varchar("gender")
    val dob = varchar("dob")
    val location = varchar("location_point")
    val bio = varchar("bio")
    val joinReasons = varchar("join_reasons")
    val interests = json<List<Int>>("interests")
    val connectId = varchar("connect_id")
    val profileUrl = varchar("profile_url")
    val createdAt = long("createdAt")
    val lastEditName = long("lastEditName")
    val countryCode = varchar("country_code")
    val lastQuestion = int("last_question")
    val answers = json<List<Int>>("quiz_answers")
}

data class Customer(
    val id: Int,
    val userId: Int,
    val fullName: String,
    val phone: String,
    val dateOfBirth: String,
    val gender: String,
    val interests: List<Int>,
    val location : String? = "",
    val profilePic : String? = "",
    val bio : String? = "",
    val joinReasons: String,
    val connectId: String,
    val canEditName: Long = System.currentTimeMillis(),
    val countryCode: String
)
