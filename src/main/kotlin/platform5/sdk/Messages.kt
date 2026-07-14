package platform5.sdk

import io.ktor.http.HttpMethod

class Messages(private val client: Client) {
    suspend fun get(id: String): MessageStatusResponse {
        val data = client.request(HttpMethod.Get, "/v1/messages/$id")
        return client.decode<MessageStatusResponse>(data) ?: error("empty response")
    }
}
