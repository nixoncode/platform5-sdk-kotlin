package platform5.sdk

import io.ktor.http.HttpMethod

class Messages(private val client: Client) {
    suspend fun get(id: String): MessageStatusResponse {
        return client.request(HttpMethod.Get, "/v1/messages/$id") ?: error("empty response")
    }
}
