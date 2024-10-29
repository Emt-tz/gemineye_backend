package tz.co.geminey.feature.auth

import kotlinx.serialization.Serializable
import org.ktorm.schema.Table
import org.ktorm.schema.boolean
import org.ktorm.schema.int
import org.ktorm.schema.varchar


object UserTable : Table<Nothing>("users") {
    val id = int("id").primaryKey()
    val username = varchar("username")
    val email = varchar("email")
    val password = varchar("password")
    val active = boolean("active")
    val isFirstTime = boolean("is_first_time")
    val changePassword = boolean("change_password")
    val roleId = int("role_id")
    val isVerified = boolean("verified")
}



@Serializable
data class RegisterRequest(
    val username: String,
    val countryCode: String,
    val email: String,
    val location: String,
    val dob: String,
    val fullName: String,
    val gender: String,
    val bio: String? = "",
    val profilePic: String? = "",
    val joinReasons: String? = "",
    val interests: List<Int>? = null,
    val bytes: ByteArray? = null,
    val deviceType: String? = "android",
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as RegisterRequest

        if (bytes != null) {
            if (other.bytes == null) return false
            if (!bytes.contentEquals(other.bytes)) return false
        } else if (other.bytes != null) return false

        return true
    }

    override fun hashCode(): Int {
        return bytes?.contentHashCode() ?: 0
    }
}

@Serializable
data class User(
    val id: Int,
    val username: String,
    val email: String,
    val password: String,
    val changePassword: Boolean,
    val isVerified: Boolean = false
)


@Serializable
data class LoginRequest(
    val username: String,
    val password: String,
    val newPassword: String? = null
)

@Serializable
data class ChangePassword(
    val username: String,
    val password: String,
    val oldPassword: String
)
@Serializable
data class Username(
    val userId: Int? = null,
    val username: String,
)

@Serializable
data class UpdatePhone(
    val username: String,
    val countryCode: String
)

@Serializable
data class OTPValidation(
    val otp: String,
    val username: String,
)

@Serializable
data class ResendOtp(
    val username: String,
)