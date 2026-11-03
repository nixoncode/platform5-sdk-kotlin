package platform5.sdk

import io.ktor.http.HttpMethod

class Airtime(private val client: Client) {
    suspend fun buy(phoneNumber: String, amount: Double): BuyAirtimeResponse {
        val req = BuyAirtimeRequest(phoneNumber, amount)
        val data = client.request(HttpMethod.Post, "/v1/airtime/buy", req, client.uuid())
        return client.decode<BuyAirtimeResponse>(data) ?: error("empty response")
    }
}
