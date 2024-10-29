package tz.co.geminey.getway

import io.ktor.client.*

class ApiServiceImplementation(client: HttpClient) : ApiService {

    private val httpHelper = HttpHelper(
        client = client,
    )

//    override suspend fun postSMS(data: SMSData): String {
//        return httpHelper.postSMS(data)
//    }
}