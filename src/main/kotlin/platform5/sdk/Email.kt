package platform5.sdk

import io.ktor.http.HttpMethod

class Email(private val client: Client) {
    suspend fun send(
        to: String,
        subject: String,
        body: String,
        from: String,
        bodyType: String? = null,
    ): SendEmailResponse {
        val req = SendEmailRequest(to, subject, body, bodyType, from)
        val data = client.request(HttpMethod.Post, "/v1/email/send", req, client.uuid())
        return client.decode<SendEmailResponse>(data) ?: error("empty response")
    }
}
