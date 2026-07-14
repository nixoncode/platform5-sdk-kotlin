package platform5.sdk

import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
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
                headers = headersOf("X-Request-ID" to "req-123"),
            )
        }
    }

    private fun clientWithEngine(engine: MockEngine) = Client(
        apiKey = "test-key",
        baseUrl = "http://localhost",
    ).also { client ->
        val field = Client::class.java.getDeclaredField("http")
        field.isAccessible = true
        field.set(client, io.ktor.client.HttpClient(engine) {
            install(ContentNegotiation) {
                json(json)
            }
            expectSuccess = false
        })
    }

    @Test
    fun `health returns successfully`() = runTest {
        val engine = mockEngine(HttpStatusCode.OK to """{"success":true,"message":"OK"}""")
        val client = clientWithEngine(engine)
        val result = client.request<String>(HttpMethod.Get, "/health")
        assertNull(result)
    }

    @Test
    fun `sms send returns message id`() = runTest {
        val engine = mockEngine(
            HttpStatusCode.OK to """
            {
                "success": true,
                "message": "SMS queued",
                "data": {
                    "message_id": "m1", "to": "+2547", "sender_name": "B",
                    "parts": 1, "cost": 1.0, "currency": "KES", "status": "queued"
                }
            }
            """.trimIndent(),
        )
        val client = clientWithEngine(engine)
        val result = client.sendMessage()
        assertEquals("m1", result.messageId)
    }

    @Test
    fun `email send returns message id`() = runTest {
        val engine = mockEngine(
            HttpStatusCode.OK to """
            {
                "success": true,
                "message": "Email queued",
                "data": { "message_id": "e1", "status": "queued" }
            }
            """.trimIndent(),
        )
        val client = clientWithEngine(engine)
        val p5 = object : Platform5("test") {
            private val client2 = client
            override val email = Email(client2)
        }
        val result = p5.email.send(to = "a@b.com", subject = "Hi", body = "Hello", from = "B")
        assertEquals("e1", result.messageId)
    }

    @Test
    fun `unauthorized throws`() = runTest {
        val engine = mockEngine(
            HttpStatusCode.Unauthorized to """{"success":false,"message":"Unauthorized","errors":"bad key"}""",
        )
        val client = clientWithEngine(engine)
        assertFailsWith<Platform5Exception.Unauthorized> {
            client.request<String>(HttpMethod.Get, "/health")
        }
    }

    @Test
    fun `rate limit has headers`() = runTest {
        val engine = MockEngine { _ ->
            respond(
                content = """{"success":false,"message":"Rate limited"}""",
                status = HttpStatusCode(429, "Too Many Requests"),
                headers = headersOf(
                    "X-Request-ID" to "req-123",
                    "X-RateLimit-Limit" to "50",
                    "X-RateLimit-Remaining" to "3",
                ),
            )
        }
        val client = clientWithEngine(engine)
        try {
            client.request<String>(HttpMethod.Get, "/health")
        } catch (e: Platform5Exception.RateLimit) {
            assertEquals(50, e.limit)
            assertEquals(3, e.remaining)
            return
        }
        fail("Expected RateLimit exception")
    }
}

fun <T> Client.sendMessage(): T {
    return this.request(HttpMethod.Post, "/v1/sms/send", SendSMSRequest("+2547", "Hi", "B"), this.uuid()) ?: error("empty")
}

fun runTest(block: suspend () -> Unit) {
    kotlinx.coroutines.runBlocking { block() }
}
