package tz.co.geminey.feature.images

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import tz.co.geminey.components.ApiResponseUtils
import tz.co.geminey.components.FileRepository


fun Route.imageRoutes(rootDirectory: String) {

    val fileRepository = FileRepository(rootDirectory)
    val imageRepository = ImageRepository(fileRepository)

    get("/image/{user}") {

    }

    get("/images/{user}") {

    }

    get("/getFile/{username}/{filename}") {
        val username = call.parameters["username"] ?: ""
        val filename = call.parameters["filename"] ?: ""

        if (username.isNotEmpty() && filename.isNotEmpty()) {
            println("CHECK DATA: $username => $filename")
            // Serve the image as a response
            val imageBytes = imageRepository.serveImageForUser(username, filename)

            if (imageBytes != null) {
                call.respondBytes(
                    byteArrayOf(*imageBytes),
                    contentType = ContentType.Image.PNG
                )
            } else {
                call.respondText("Image not found", status = HttpStatusCode.NotFound)
            }
        } else {
            call.respondText("Invalid request", status = HttpStatusCode.BadRequest)
        }
    }

//    authenticate("auth-jwt") {
        post("/upload/profilePic") {
            val req = call.receive<UploadProfileRequest>()
            println("Check Image -> $req")
            val response = imageRepository.uploadProfilePic(req)
            call.respondText(ApiResponseUtils.toJson(response), ContentType.Application.Json)
        }
//    }
    post("/upload/images") {

    }

}