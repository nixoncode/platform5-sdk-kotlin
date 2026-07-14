# platform5-sdk-kotlin

Kotlin SDK for the Platform5 Developer API.

## Install

**Step 1** — add JitPack to your repositories:

```kotlin
// build.gradle.kts
repositories {
    mavenCentral()
    maven("https://jitpack.io")
}
```

**Step 2** — add the dependency:

```kotlin
dependencies {
    implementation("com.github.nixoncode:platform5-sdk-kotlin:v0.1.0")
}
```

## Usage

```kotlin
import platform5.sdk.Platform5

suspend fun main() {
    val client = Platform5(apiKey = "p5_live_abc123")

    val sms = client.sms.send(
        to = "+254712345678",
        message = "Your appointment is confirmed.",
        from = "MyBrand",
    )
    println(sms.messageId)

    client.email.send(
        to = "user\@example.com",
        subject = "Welcome",
        body = "<h1>Hello!</h1>",
        from = "MyBrand",
        bodyType = "html",
    )

    val status = client.messages.get("msg-uuid")
    val balance = client.account.getBalance()
}
```

## Services

| Method | Endpoint |
|--------|----------|
| `client.sms.send(to, message, from)` | POST /v1/sms/send |
| `client.email.send(to, subject, body, from, bodyType?)` | POST /v1/email/send |
| `client.messages.get(id)` | GET /v1/messages/{id} |
| `client.account.getBalance()` | GET /v1/balance |
| `client.health()` | GET /health |

## Error Handling

```kotlin
import platform5.sdk.Platform5Exception

try {
    client.sms.send(...)
} catch (e: Platform5Exception.RateLimit) {
    println("Rate limited: ${e.remaining}/${e.limit}")
} catch (e: Platform5Exception) {
    println("API error ${e.statusCode}: ${e.message}")
}
```

## Requirements

- Kotlin 2.0+
- JVM 11+
