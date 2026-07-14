package platform5.sdk

class Platform5(
    apiKey: String,
    baseUrl: String = "http://localhost:8084",
) {
    private val client = Client(apiKey, baseUrl)
    val sms = Sms(client)
    val email = Email(client)
    val messages = Messages(client)
    val account = Account(client)

    suspend fun health() {
        client.request<Unit>(io.ktor.http.HttpMethod.Get, "/health")
    }
}
