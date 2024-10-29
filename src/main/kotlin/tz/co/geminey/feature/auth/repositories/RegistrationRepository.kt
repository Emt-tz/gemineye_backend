package tz.co.geminey.feature.auth.repositories

import io.ktor.http.*
import io.ktor.util.*
import org.ktorm.dsl.*
import tz.co.geminey.components.*
import tz.co.geminey.components.GlobalState.twilio
import tz.co.geminey.config.DBHelper.database
import tz.co.geminey.config.JwtConfig
import tz.co.geminey.feature.auth.OTPValidation
import tz.co.geminey.feature.auth.RegisterRequest
import tz.co.geminey.feature.auth.UserTable
import tz.co.geminey.feature.matches.MatchesRepository
import tz.co.geminey.models.CustomerTable
import tz.co.geminey.models.FileTable

class RegistrationRepository(rootPath: String?) {

    private val fileRepository = rootPath?.let { FileRepository(it) }
    fun registerUser(req: RegisterRequest): Any {
        var response = ApiResponse(
            code = HttpStatusCode.BadRequest.value,
            message = HttpStatusCode.BadRequest.description,
            body = emptyMap<String, Any>()
        )
        try {
            database.useTransaction {

                val id = database.insertAndGenerateKey(UserTable) {
                    set(UserTable.username, req.username)
                    set(UserTable.email, req.email)
                    set(UserTable.active, true)
                    set(UserTable.isFirstTime, true)
                    set(UserTable.changePassword, true)
                    set(UserTable.roleId, 2)
                }

                val connectId = generateUniqueString(6).toUpperCasePreservingASCIIRules()
                val time = System.currentTimeMillis()
                database.insert(CustomerTable) {
                    set(CustomerTable.userId, id as Int)
                    set(CustomerTable.fullName, req.fullName)
                    set(CustomerTable.location, req.location)
                    set(CustomerTable.phone, req.username)
                    set(CustomerTable.gender, req.gender.uppercase())
                    set(CustomerTable.dob, req.dob)
                    set(CustomerTable.joinReasons, req.joinReasons)
                    set(CustomerTable.interests, req.interests)
                    set(CustomerTable.connectId, connectId)
                    set(CustomerTable.createdAt, time)
                    set(CustomerTable.lastEditName, time)
                    set(CustomerTable.countryCode, req.countryCode)
                    set(CustomerTable.lastQuestion, 0)
                }

                if ((req.bytes != null || req.profilePic != null) && fileRepository != null) {
                    val relativePath = fileRepository.getFilePath(req.username)
                    val name = if (req.bytes != null) {
                        fileRepository.saveFile(req.bytes, relativePath)
                    } else if (req.profilePic != null) {
                        fileRepository.saveFile(req.profilePic, relativePath)
                    } else {
                        ""
                    }

                    database.insert(FileTable) {
                        set(FileTable.username, req.username)
                        set(FileTable.name, name)
                        set(FileTable.isProfile, true)
                    }

                    database.update(CustomerTable) {
                        set(it.profileUrl, "${req.username}/$name")
                        where { it.userId eq id as Int }
                    }
                }
                MatchesRepository().matchAndStoreMatches()
                val tw = twilio.startVerification("+${req.username}")
                if (tw.valid) {
                    response = ApiResponse(
                        code = 200,
                        message = "Otp is sent to you phone number, please check and validate",
                        body = mapOf(
                            "token" to JwtConfig.makeToken(id.toString()),
                            "connectId" to connectId
                        )
                    )
                }
            }
        } catch (e: Exception) {
            println("Error : ${e.message}")
            when {
                e.message?.contains("Duplicate entry") == true ->
                    response = ApiResponse(
                        code = HttpStatusCode.Found.value,
                        message = "User with ${req.username} exist, please login or reset password.",
                        body = emptyMap<String, Any>()
                    )

                else -> {
                    println("Error connecting to the database: ${e.message}")
                    response = ApiResponse(
                        code = 500,
                        message = "Internal Server Error",
                        body = emptyMap<String, Any>()
                    )
                }
            }
        }
        return ApiResponseUtils.toJson(response)
    }

    fun onResendOTP(username: String): Any {
        var res = ApiResponse(
            HttpStatusCode.BadRequest.value,
            HttpStatusCode.BadRequest.description,
            emptyMap<String, Any>()
        )
        try {
            val phone = database.from(UserTable)
                .select(UserTable.username)
                .where { UserTable.username eq username }
                .map {
                    it[UserTable.username]
                }.firstOrNull() ?: return res

            twilio.startVerification("+$phone")
            res = ApiResponse(
                code = 200,
                message = "Otp is sent to you phone number, please check and validate",
                body = emptyMap<String, Any>()
            )
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return res
    }

    fun validateOTP(otp: OTPValidation): Any {
        var res = ApiResponse(
            400,
            "Invalid details",
            emptyMap<String, Any>()
        )
        try {
            val phone = database.from(UserTable)
                .select(UserTable.username)
                .where { UserTable.username eq otp.username }
                .map {
                    it[UserTable.username]
                }.firstOrNull() ?: return res

            val twilio = twilio.checkVerification("+$phone", otp.otp)
            if (twilio.valid) {
                database.update(UserTable) {
                    where { UserTable.username eq otp.username }
                    set(UserTable.isVerified, true)
                }
                res = ApiResponse(
                    HttpStatusCode.OK.value,
                    HttpStatusCode.OK.description,
                    emptyMap<String, Any>()
                )
            }
        } catch (e: Throwable) {
            e.printStackTrace()
        }
        return res
    }
}