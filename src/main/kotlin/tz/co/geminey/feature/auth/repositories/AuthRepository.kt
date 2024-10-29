package tz.co.geminey.feature.auth.repositories

import io.ktor.http.*
import org.ktorm.dsl.*
import tz.co.geminey.components.*
import tz.co.geminey.components.GlobalState.twilio
import tz.co.geminey.config.DBHelper.database
import tz.co.geminey.config.JwtConfig.makeToken
import tz.co.geminey.feature.auth.LoginRequest
import tz.co.geminey.feature.auth.User
import tz.co.geminey.feature.auth.UserTable
import tz.co.geminey.feature.auth.UserTable.password
import tz.co.geminey.feature.auth.Username
import tz.co.geminey.models.Customer
import tz.co.geminey.models.CustomerTable
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit

class AuthRepository() {

    fun getLoginDetails(req: LoginRequest): Any {
        var response = ApiResponse(
            code = HttpStatusCode.Unauthorized.value,
            message = HttpStatusCode.Unauthorized.description,
            body = emptyMap<String, Any>()
        )
        try {
            //TODO inner join Customer table to reduce database calls
            val user = database.from(UserTable)
                .select()
                .where(UserTable.username eq req.username)
                .map { rowToUser(it) }
                .firstOrNull()?: return ApiResponseUtils.toJson(response)

            if ((user.changePassword || verifyPassword(req.password, user.password))) {
                if (user.isVerified){
                    val customer = database.from(CustomerTable)
                        .select()
                        .where(CustomerTable.userId eq user.id)
                        .map { rowToCustomer(it) }
                        .firstOrNull()?: return ApiResponseUtils.toJson(response)

                    val canEditName = checkCanEditName(customer.canEditName)
                    val responseMap = mapOf(
                        "id" to user.id,
                        "changePassword" to user.changePassword,
                        "username" to user.username,
                        "email" to user.email,
                        "canEditName" to canEditName,
                        "customer" to customer,
                        "token" to makeToken(user.id.toString())
                    )

                    response = ApiResponse(
                        code = 200,
                        message = "Success",
                        body = responseMap
                    )
                }else{
                    twilio.startVerification("+${req.username}")
                    response = ApiResponse(
                        code = 202,
                        message = "Your phone number was not verified. Otp is sent to you phone number, please check and validate",
                        body = mapOf(
                            "isVerified" to false,
                            "token" to makeToken(user.id.toString()),
                        )
                    )
                }
            }
        } catch (e: Throwable) {
            println("Error connecting to the database: ${e.message} -- ${e.cause}")
            response = ApiResponse(
                code = 500,
                message = HttpStatusCode.InternalServerError.description,
                body = emptyMap<String, Any>()
            )
        }
        return ApiResponseUtils.toJson(response)
    }

    private fun checkCanEditName(canEditName: Long): Boolean {
        val editDateTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(canEditName),
            ZoneOffset.UTC
        )
        val currentDateTime = LocalDateTime.now()
        val monthsDifference = ChronoUnit.MONTHS.between(editDateTime, currentDateTime)
        return monthsDifference >= 3
    }

    fun getUserByUserName(req: Username): Any {
        var response = ApiResponse(
            code = HttpStatusCode.NotFound.value,
            message = "User does not exist",
            body = emptyMap<String, Any>()
        )
        try {
            val user = database.from(UserTable)
                .select()
                .where(UserTable.username eq req.username)
                .map { rowToUser(it) }
                .firstOrNull()
            if (user != null) {
                twilio.startVerification("+${req.username}")
                response =  ApiResponse(
                    code = 200,
                    message = "Otp is sent to you phone number, please check and validate",
                    body = emptyMap<String, Any>()
                )
            }
        } catch (e: Exception) {
            println("Error connecting to the database: ${e.message}")
        }
        return ApiResponseUtils.toJson(response)
    }

    private fun rowToUser(row: QueryRowSet): User {
        return User(
            id = row[UserTable.id] ?: 0,
            username = row[UserTable.username] ?: "",
            password = row[password] ?: "",
            email = row[UserTable.email] ?: "",
            changePassword = row[UserTable.changePassword] ?: true,
            isVerified = row[UserTable.isVerified] ?: false
        )
    }

    private fun rowToCustomer(row: QueryRowSet): Customer {
        return Customer(
            id = row[CustomerTable.id] ?: 0,
            userId = row[CustomerTable.userId] ?: 0,
            fullName = row[CustomerTable.fullName] ?: "",
            phone = row[CustomerTable.phone] ?: "",
            dateOfBirth = row[CustomerTable.dob] ?: "",
            gender = row[CustomerTable.gender] ?: "",
            interests = row[CustomerTable.interests] ?: emptyList(),
            joinReasons = row[CustomerTable.joinReasons] ?: "",
            bio = row[CustomerTable.bio] ?: "",
            connectId = row[CustomerTable.connectId] ?: "",
            profilePic = row[CustomerTable.profileUrl] ?: "",
            canEditName = row[CustomerTable.lastEditName] ?: System.currentTimeMillis(),
            countryCode = row[CustomerTable.countryCode] ?: ""
        )
    }

    fun changePassword(req: LoginRequest): Any {
        var response: Any = ApiResponse(
            code = 500,
            message = "Internal Server Error",
            body = emptyMap<String, Any>()
        )
        try {
            val user = database.from(UserTable)
                .select()
                .where(UserTable.username eq req.username)
                .map { rowToUser(it) }
                .firstOrNull()

            if (user != null) {
                if (req.newPassword != null && req.newPassword.isNotEmpty()) {
                    response = onNewPassword(req.password, user.password, req.newPassword, user.id)
                } else {
                    val hashedPassword = hashPassword(req.password)
                    val rowCount = updatePassword(user.id, hashedPassword)
                    if (rowCount > 0) {
                        response = responseMessage(code = 200, message = "Success")
                    }
                }
            } else {
                println("Error User not found: ${req.username}")
                response = responseMessage(
                    code = HttpStatusCode.NotFound.value,
                    message = HttpStatusCode.NotFound.description,
                )
            }
        } catch (e: Exception) {
            println("Error connecting to the database: ${e.message}")
        }
        return ApiResponseUtils.toJson(response)
    }

    private fun onNewPassword(oldPass: String, pass: String, newPass: String, id: Int): ApiResponse {
        if (verifyPassword(oldPass, pass)) {
            val hashedNewPassword = hashPassword(newPass)
            val rowCount = updatePassword(id, hashedNewPassword)
            if (rowCount > 0) {
                return responseMessage(code = 200, message = "Success")
            }
        }
        return responseMessage(code = HttpStatusCode.NotAcceptable.value, message = "Invalid password.")
    }

    private fun updatePassword(id: Int, pass: String): Int {
        val rowCount = database.update(UserTable) {
            set(it.password, pass)
            set(it.changePassword, false)
            where {
                it.id eq id
            }
        }
        println("ROW COUNT:: $rowCount")
        return rowCount
    }

    private fun responseMessage(code: Int, message: String): ApiResponse {
        return ApiResponse(
            code = code,
            message = message,
            body = emptyMap<String, Any>()
        )
    }
}
