package tz.co.geminey.components

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import org.mindrot.jbcrypt.BCrypt
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

@Serializable
data class ApiResponse(val code: Int, val message: String, @Contextual var body: Any? = null)

suspend fun ApplicationCall.respondJson(obj: Any, statusCode: HttpStatusCode = HttpStatusCode.OK) {
    val response = ApiResponse(
        code = statusCode.value,
        message = statusCode.description,
        body = obj
    )
    respond(HttpStatusCode.OK, response)
}

object ApiResponseUtils {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    fun toJson(obj: Any): String {
        return gson.toJson(obj)
    }
}

fun generateCustomUUID(userId: Int): String {
    val currentTimeInSeconds = System.currentTimeMillis() / 1000
    val randomAlphabet = ('a'..'z').random() // Generate a random lowercase alphabet character
    val uuidString = "$randomAlphabet$userId$currentTimeInSeconds"
    return UUID.fromString(uuidString).toString()
}

fun generateUUID(userId: Int): String {
    val currentTimeInSeconds = System.currentTimeMillis() / 1000
    val uuidString = buildString {
        for (i in 0 until 8) {
            if (i % 2 == 0) {
                append(('a'..'z').random())
            } else {
                append(('0'..'9').random())
            }
        }
        append(userId)
        append(currentTimeInSeconds)
    }
    return UUID.fromString(uuidString).toString()
}

fun generateUniqueString(length: Int): String {
    val allowedChars = "ABCDEFG@#HIJKLMN@#OPQRSTUVWXYZ@#\$abcdefghijkl@#mnopqrstuvwxyz@#0123456789@#$"
    val random = Random()
    return buildString {
        repeat(length) {
            val randomIndex = random.nextInt(allowedChars.length)
            append(allowedChars[randomIndex])
        }
    }
}

fun hashPassword(password: String): String {
    val salt = BCrypt.gensalt(12)
    return BCrypt.hashpw(password, salt)
}

fun verifyPassword(plainPassword: String, hashedPassword: String): Boolean {
    return BCrypt.checkpw(plainPassword, hashedPassword)
}

class FileRepository(private val rootDirectory: String) {
    fun getFilePath(username: String): String {
        val relativePath = "/uploads/files/${username}/"
        println("DIRECTORY: $rootDirectory$relativePath")
        val directory = File("$rootDirectory$relativePath")
        if (!directory.exists()) {
            directory.mkdirs() // Create directories, including parent directories if needed
        }
        return relativePath
    }
    fun saveFile(base64String: String, filePath: String):String {
        return try{
            val timestamp = System.currentTimeMillis()
            val decodedBytes = Base64.getDecoder().decode(base64String)
            val file = File("$rootDirectory$filePath/${timestamp}.png")
            file.writeBytes(decodedBytes).toString()
            "${timestamp}.png"
        }catch (e: FileSystemException){
            println("EXCEPTION WHILE SAVING FILE: ${e.message}")
            ""
        }
    }

    fun saveFile(bytes: ByteArray?, filePath: String):String {
        return try{
            if (bytes != null) {
            val timestamp = System.currentTimeMillis()
            val file = File("$rootDirectory$filePath/${timestamp}.png")
                file.writeBytes(bytes).toString()
                "${timestamp}.png"
            }else{
                ""
            }
        }catch (e: FileSystemException){
            println("EXCEPTION WHILE SAVING FILE: ${e.message}")
            ""
        }
    }

    private fun getFilePathForUser(username: String, filename: String): String {
        val relativePath = "/uploads/files/$username/$filename"
        return "$rootDirectory$relativePath"
    }

    fun serveImageForUser(username: String, filename: String): ByteArray? {
        try {
            val filePath = getFilePathForUser(username, filename)
            val file = File(filePath)

            if (file.exists()) {
                return file.readBytes()
            }
        } catch (e: Exception) {
            println("EXCEPTION WHILE SERVING IMAGE: ${e.message}")
        }
        return null
    }
}

fun formatDate(date: LocalDateTime): String {
    val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
    return date.format(formatter)
}

fun getCurrentTimestamp(): Long {
    return System.currentTimeMillis()
}

fun compareTimeStamp(lastTime: String): Long {
    return System.currentTimeMillis() - (3 * 60 * 1000)
}

fun timestampToDateTime(timestamp: Long): String {
    val dateTime = LocalDateTime.ofInstant(
        Instant.ofEpochMilli(timestamp),
        ZoneId.systemDefault()
    )
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    return dateTime.format(formatter)
}