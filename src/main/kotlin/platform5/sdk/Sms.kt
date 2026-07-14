package platform5.sdk

import io.ktor.http.HttpMethod

class Sms(private val client: Client) {
    suspend fun send(to: String, message: String, from: String): SendSMSResponse {
        val req = SendSMSRequest(to, message, from)
        val data = client.request(HttpMethod.Post, "/v1/sms/send", req, client.uuid())
        return client.decode<SendSMSResponse>(data) ?: error("empty response")
    }
}
