package platform5.sdk

import kotlin.test.*

class Platform5Test {
    @Test
    fun `creates client with services`() {
        val p5 = Platform5(apiKey = "test-key")
        assertNotNull(p5.sms)
        assertNotNull(p5.email)
        assertNotNull(p5.messages)
        assertNotNull(p5.account)
    }
}
