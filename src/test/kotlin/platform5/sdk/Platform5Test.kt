package platform5.sdk

import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import kotlin.test.*

class Platform5Test {
    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `creates client with services`() {
        val p5 = Platform5(apiKey = "test-key")
        assertNotNull(p5.sms)
        assertNotNull(p5.email)
        assertNotNull(p5.messages)
        assertNotNull(p5.account)
    }
}
