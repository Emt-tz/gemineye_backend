package tz.co.geminey.feature.user

import io.ktor.http.*
import org.ktorm.dsl.*
import tz.co.geminey.components.ApiResponse
import tz.co.geminey.components.ApiResponseUtils
import tz.co.geminey.components.FileRepository
import tz.co.geminey.config.DBHelper.database
import tz.co.geminey.feature.auth.UpdatePhone
import tz.co.geminey.feature.auth.User
import tz.co.geminey.feature.auth.UserTable
import tz.co.geminey.models.Customer
import tz.co.geminey.models.CustomerTable
import tz.co.geminey.models.FileTable

class UserRepository(rootPath: String?) {
    private val fileRepository = rootPath?.let { FileRepository(it) }

    fun updateUserName(req: UpdatePhone, username: String): Any {

        var response: Any = ApiResponse(code = 500, message = "Internal Server Error", body = emptyMap<String, Any>())

        try {

            val user = database.from(UserTable)
                .select()
                .where(UserTable.id eq username.toInt())
                .map { it[CustomerTable.id] }
                .firstOrNull()

            response = if (user != null) {

                val existing = database.from(UserTable)
                    .select()
                    .where(UserTable.username eq req.username)
                    .map { it[CustomerTable.id] }
                    .firstOrNull()

                if (existing == null) {
                    updateExistingUser(req, username.toInt())
                } else {
                    ApiResponse(
                        code = HttpStatusCode.BadRequest.value,
                        message = "User already exist with the same phone number",
                        body = emptyMap<String, Any>()
                    )
                }

            } else {
                ApiResponse(code = HttpStatusCode.Unauthorized.value, message = HttpStatusCode.Unauthorized.description, body = emptyMap<String, Any>())
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error while saving new username: ${e.message}")
        }
        return ApiResponseUtils.toJson(response)
    }

    private fun updateExistingUser(req: UpdatePhone, username: Int): ApiResponse {
        database.useTransaction {
            val rowCount = database.update(UserTable) {
                set(it.username, req.username)
                where {
                    it.id eq username
                }
            }
            if (rowCount > 0) {
                val row = database.update(CustomerTable) {
                    set(it.phone, req.username)
                    set(it.countryCode, req.countryCode)
                    where {
                        it.userId eq username
                    }
                }
                if (row > 0) {
                    return ApiResponse(code = 200, message = "Success", body = emptyMap<String, Any>())
                }
            }
        }
        return ApiResponse(HttpStatusCode.InternalServerError.value, HttpStatusCode.InternalServerError.description, body = emptyMap<String, Any>())
    }

    fun updateUserProfile(req: EditProfile, username: String): Any {

        var response: Any =
            ApiResponse(code = HttpStatusCode.BadRequest.value, message = HttpStatusCode.BadRequest.description, body = emptyMap<String, Any>())

        try {
            val user = database.from(UserTable)
                .select()
                .where(UserTable.id eq username.toInt())
                .map { rowToUser(it) }
                .firstOrNull()

            if (user != null) {
                database.useTransaction {
                    if (req.fullName != null) {
                        database.update(CustomerTable) {
                            set(it.fullName, req.fullName)
                            where {
                                it.userId eq username.toInt()
                            }
                        }
                    }

                    if (req.email != "") {
                        database.update(UserTable) {
                            set(it.email, req.email)
                            where {
                                it.id eq username.toInt()
                            }
                        }
                    }

                    if ((req.bytes != null || req.profilePic != null) && fileRepository != null) {
                        saveProfilePic(req, user)
                    }

                    val userNew = database.from(UserTable)
                        .select()
                        .where(UserTable.id eq username.toInt())
                        .map { rowToUser(it) }
                        .firstOrNull()

                    val customer = database.from(CustomerTable)
                        .select()
                        .where(CustomerTable.userId eq user.id)
                        .map { rowToCustomer(it) }
                        .firstOrNull()

                    response = ApiResponse(
                        code = 200,
                        message = "Success",
                        body = mapOf("user" to userNew, "customer" to customer)
                    )
                }

            } else {
                println("Error User not found: $username")
                response = ApiResponse(
                    code = HttpStatusCode.BadRequest.value,
                    message = HttpStatusCode.BadRequest.description,
                    body = emptyMap<String, Any>()
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            println("Error while updating profile: ${e.message}")
        }
        return ApiResponseUtils.toJson(response)
    }

    private fun saveProfilePic(req: EditProfile, user: User): Int {
        try {
            if (fileRepository != null) {
                val relativePath = fileRepository.getFilePath(user.username)
                println("Check relative path: $relativePath")
                val name = if (req.bytes != null) {
                    fileRepository.saveFile(req.bytes, relativePath)
                } else if (req.profilePic != null) {
                    fileRepository.saveFile(req.profilePic, relativePath)
                } else {
                    ""
                }
                println("Check image path: $name")
                database.useTransaction {
                    database.update(FileTable) {
                        set(FileTable.username, user.username)
                        set(FileTable.name, name)
                        set(FileTable.isProfile, true)
                        where {
                            it.username eq user.username
                        }
                    }
                    val rowCount = database.update(CustomerTable) {
                        set(it.profileUrl, "${user.username}/$name")
                        where { it.userId eq user.id }
                    }
                    return rowCount
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return 0
    }

    private fun rowToUser(row: QueryRowSet): User {
        return User(
            id = row[UserTable.id] ?: 0,
            username = row[UserTable.username] ?: "",
            password = row[UserTable.password] ?: "",
            email = row[UserTable.email] ?: "",
            changePassword = row[UserTable.changePassword] ?: true
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
}