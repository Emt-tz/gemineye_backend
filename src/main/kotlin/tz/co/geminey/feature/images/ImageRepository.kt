package tz.co.geminey.feature.images

import org.ktorm.dsl.insert
import tz.co.geminey.components.ApiResponse
import tz.co.geminey.components.FileRepository
import tz.co.geminey.config.DBHelper
import tz.co.geminey.models.FileTable

class ImageRepository(private val fileRepository: FileRepository) {

    fun uploadProfilePic(req: UploadProfileRequest): Any {
        return try {
            val relativePath = fileRepository.getFilePath(req.username)
            println("Check relative path: $relativePath")
//            val name = fileRepository.saveFile(req.profilePic, relativePath)
            val name = fileRepository.saveFile(req.bytes, relativePath)
            println("Check image path: $name")
            if (name == "") {
                throw Exception("Name is empty")
            }
            DBHelper.database.insert(FileTable) {
                set(FileTable.username, req.username)
                set(FileTable.name, name)
                set(FileTable.isProfile, req.isProfile)
            }
            ApiResponse(
                code = 200,
                message = "Success",
                body = mapOf("profileUrl" to "${req.username}/$name")
            )
        } catch (e: Exception) {
            println("Error saving image to the database: ${e.message}")
            ApiResponse(
                code = 500,
                message = "Internal Server Error",
                body = emptyMap<String, Any>()
            )
        }
    }

    fun serveImageForUser(username: String, filename: String): ByteArray? {
        return fileRepository.serveImageForUser(username, filename)
    }
}