package platform5.sdk

import io.ktor.http.HttpMethod

class Account(private val client: Client) {
    suspend fun getBalance(): BalanceResponse {
        val data = client.request(HttpMethod.Get, "/v1/balance")
        return client.decode<BalanceResponse>(data) ?: error("empty response")
    }
}
