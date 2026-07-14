package platform5.sdk

import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlin.test.*

class ClientTest {
    private val json = Json { ignoreUnknownKeys = true }

    private fun mockEngine(vararg responses: Pair<HttpStatusCode, String>): MockEngine {
        val iterator = responses.iterator()
        return MockEngine { _ ->
            val (status, body) = iterator.next()
            respond(
                content = body,
                status = status,
                headers = headersOf("X-Request-ID" to listOf("req-123")),
            )
        }
    }

    private fun clientWithEngine(engine: MockEngine): Client {
        val customClient = io.ktor.client.HttpClient(engine) {
            install(ContentNegotiation) {
                json(json)
            }
            expectSuccess = false
        }

        return object : Client(apiKey = "test-key", baseUrl = "http://localhost") {
            override val httpClient = customClient
        }
    }

    @Test
    fun `health returns successfully`() = runTest {
        val engine = mockEngine(HttpStatusCode.OK to """{"success":true,"message":"OK"}""")
        val client = clientWithEngine(engine)
        val result = client.request(HttpMethod.Get, "/health")
        assertNull(result)
    }

    @Test
    fun `sms send returns message id`() = runTest {
        val engine = mockEngine(
            HttpStatusCode.OK to """
            {
                "success": true, "message": "SMS queued",
                "data": {
                    "message_id": "m1", "to": "+2547", "sender_name": "B",
                    "parts": 1, "cost": 1.0, "currency": "KES", "status": "queued"
                }
            }
            """.trimIndent(),
        )
        val client = clientWithEngine(engine)
        val data = client.request(
            HttpMethod.Post, "/v1/sms/send",
            SendSMSRequest("+2547", "Hi", "B"),
            client.uuid(),
        )
        assertNotNull(data)
        val result = client.decode<SendSMSResponse>(data)
        assertNotNull(result)
        assertEquals("m1", result!!.messageId)
    }

    @Test
    fun `unauthorized throws`() = runTest {
        val engine = mockEngine(
            HttpStatusCode.Unauthorized to """{"success":false,"message":"Unauthorized","errors":"bad key"}""",
        )
        val client = clientWithEngine(engine)
        assertFailsWith<Platform5Exception.Unauthorized> {
            client.request(HttpMethod.Get, "/health")
        }
    }

    @Test
    fun `rate limit has headers`() = runTest {
        val engine = MockEngine { _ ->
            respond(
                content = """{"success":false,"message":"Rate limited"}""",
                status = HttpStatusCode(429, "Too Many Requests"),
                headers = headersOf(
                    "X-Request-ID" to listOf("req-123"),
                    "X-RateLimit-Limit" to listOf("50"),
                    "X-RateLimit-Remaining" to listOf("3"),
                ),
            )
        }
        val client = clientWithEngine(engine)
        var caught: Platform5Exception.RateLimit? = null
        try {
            client.request(HttpMethod.Get, "/health")
        } catch (e: Platform5Exception.RateLimit) {
            caught = e
        }
        assertNotNull(caught)
        assertEquals(50, caught!!.limit)
        assertEquals(3, caught!!.remaining)
    }
}

fun runTest(block: suspend () -> Unit) {
    kotlinx.coroutines.runBlocking { block() }
}
