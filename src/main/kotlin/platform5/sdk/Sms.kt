package platform5.sdk

import io.ktor.http.HttpMethod

class Sms(private val client: Client) {
    suspend fun send(to: String, message: String, from: String): SendSMSResponse {
        val req = SendSMSRequest(to, message, from)
        return client.request(HttpMethod.Post, "/v1/sms/send", req, client.uuid()) ?: error("empty response")
    }
}
