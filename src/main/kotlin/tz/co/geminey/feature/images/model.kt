package tz.co.geminey.feature.images

import kotlinx.serialization.Serializable

@Serializable
data class UploadProfileRequest(
    val username: String,
    val userId: Int,
    val profilePic: String,
    val isProfile: Boolean? = false,
    val bytes: ByteArray? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UploadProfileRequest

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