package platform5.sdk

import io.ktor.http.HttpMethod

class Account(private val client: Client) {
    suspend fun getBalance(): BalanceResponse {
        return client.request(HttpMethod.Get, "/v1/balance") ?: error("empty response")
    }
}
